#include <iostream>
#include <arpa/inet.h>
#include <unistd.h>
#include <cstring>
#include <fcntl.h>
#include "ConnectionManager.h"
#include "ServerController.h"
#include "MessageParser.h"


using namespace std;


ConnectionManager::ConnectionManager(DataBaseConnection &dbc, void* args, int BACKLOG)
    :dbc(dbc)
{
    int* casted_args = (int*) (intptr_t) args;
    pipe_fd[0] = casted_args[0];
    pipe_fd[1] = casted_args[1];
    pipe_fd2[0] = casted_args[2];
    pipe_fd2[1] = casted_args[3];
    // zainicjalizuje listenerfd, listeneraddr
    create_listener(casted_args[4], BACKLOG);

    // tworze waitera i daje mu deskryptory ktorych ma nie usunac
    waiter = Waiter(pipe_fd[0], pipe_fd[1], pipe_fd2[0], pipe_fd2[1], listenerfd);

    waiter.add_descr(listenerfd);
    waiter.add_descr(pipe_fd[0]);
    waiter.add_descr(pipe_fd2[0]);
}

int ConnectionManager::send_all(int fd, char *buf, int *len) {

    int total = 0; // ile juz sie udalo wyslac
    int left = *len; // ile jeszcze do wyslania
    int nbytes = 0;

    while(total < *len)
    {
        nbytes = send(fd, buf+total, left, 0);
        if(nbytes == -1)
            break;

        total += nbytes;
        left -= nbytes;
    }

    *len = total; // ile udalo sie wyslac faktycznie

    if(nbytes == -1)
        return -1;
    else
        return 0;
}

void ConnectionManager::handle_client_request(int fd) {

    // mowie klienckiej strukturze zeby sobie odebrala pakiet
    int status = cli_struct[fd].receive_part_message();

    // struct cli mowi mi ze rozlaczyl sie lub sa bledy, to zamykam jego deskryptor
    if(status  < 0)
    {
        if(status == -1) // koniec lacznosci
            printf("~~ Socket %d hung up \n", fd);
        else
            perror("recv");

        // mowie waiterowi zeby usunal deskryptor ze zbioru i go zamknal
        waiter.close_descr(fd);

        // usuwam miejsce w mapie
        cli_struct.erase(fd);
    }

    // struct cli mowi ze odebral cala wiadomosc
    if(status == 1) {
        //cout << "Mam juz cala wiadomosc, moge ja zwrocic" << endl;

        //parsowanie wiadmosci
        json messageJson = MessageParser::parseRequest(cli_struct[fd]);

        //obsluga zadania klienta
        ServerController::selectAction(fd, messageJson, *this, dbc);

        // przywracam domyslne ustawienia, bo wyslalem, zostaje czyste na nastepny raz
        cli_struct[fd].dealloc();
        cli_struct[fd].init();
    }

}

int ConnectionManager::handle_console_request() {
    if(read(pipe_fd[0], &buf, 1) == -1)
    {
        perror("read_readfd_pipe");
        exit(EXIT_FAILURE);
    }

//    if(buf == 'x' || buf == 'q')
//    {
//        waiter.close_all_descr();
//
//        if(buf == 'q')
//        {
//            dbc.closeConnection();
//            work = false;
//        }
//
//        return 1;
//    }
    int id;
    char response;

    if(buf=='x'){
        waiter.close_all_descr();
    }else if(buf=='q'){
        dbc.closeConnection();
        work = false;
    }else if(buf=='r') {
        response = dbc.showAllGroups();

    }else if(buf=='t'){
        response = dbc.showAllUsers();
    }else if(buf=='y'){
        response = dbc.showLeaders();
    }else if(buf=='u'){
        id = showUserMenu();
        // system("clear");

        //cout << "Podaj id uzytkownika do usuniecia: ";
        //cin >> id;
        cout << "ID to delete: " << id;
        response = dbc.deleteUser(id);
    }else if(buf=='i') {
        id = showGroupMenu();
        //system("clear");
//        cout << "Podaj id grupy do usuniecia: ";
//        cin >> id;
        cout << "ID to delete: " << id;
        response = dbc.deleteGroup(id);
    }

    if(write(pipe_fd2[1], &response, 1) == -1)
    {
        perror("write_writefd_pipe");
        exit(EXIT_FAILURE);
    }
//        cout << "wyslalem" << endl;
    return 1;
}

int ConnectionManager::showGroupMenu() {

    int choice;
    int limit = dbc.groupCount();
    do{
        char response = dbc.showAllGroups();
        cin.clear();
        cout <<endl<< "Wybierz opcje: ";
        cin >> choice;
    }while(choice < limit || choice > limit);

    int id = dbc.getGroupID(choice);

    char response = dbc.deleteGroup(id);
}

int ConnectionManager::showUserMenu(){

    int choice=-1;
    int limit = dbc.userCount();
    //cout << "limit: " << limit << endl;
    do{
        cin.clear();
        char response = dbc.showAllUsers();

        cout <<endl<< "Wybor: ";
        cin >> choice;
       // cout << "---------------choice: " << choice<<endl;
    }while(choice < 1 || choice > limit);

    return dbc.getUserID(choice);
    //char response = dbc.deleteUser(id);
}

void ConnectionManager::handle_new_connection() {
    socklen_t addrlength;
    int clientfd; // deskryptor nowego clienta
    sockaddr_in clientaddr; // adres klienta
    addrlength = sizeof(clientaddr);
    int attempts = 0;

    while(attempts < 20) {
        if ((clientfd = accept(listenerfd, (sockaddr*)&clientaddr, &addrlength)) == -1)
        {
            attempts++;
            delay();
            continue; // na wszelki wypadek nie wychodze jakby sie nie udalo accept
        }
        else
        {
            int flags = fcntl(clientfd, F_GETFL);
            if (flags == -1) {
                attempts++;
                perror("F_GETFL clientfd");
                printf("~~ Connection from %s on socket %d refused. Retrying... \n", inet_ntoa(clientaddr.sin_addr), clientfd);
                close(clientfd);
                delay();
                continue;
            }
            if (fcntl(clientfd, F_SETFL, flags | O_NONBLOCK) == -1) {
                attempts++;
                perror("F_SETFL clientfd");
                printf("~~ Connection from %s on socket %d refused. Retrying... \n", inet_ntoa(clientaddr.sin_addr), clientfd);
                close(clientfd);
                delay();
                continue;
            }

            waiter.add_descr(clientfd);

            // tworze w mapie miejsce na strukture klienta
            cli_struct[clientfd] = ClientStructure(clientfd);

            printf("~~ New connection from %s on socket %d \n", inet_ntoa(clientaddr.sin_addr), clientfd);
            return;
        }
    }
    perror("handle_new_connection() failed");
    exit(EXIT_FAILURE);
}

void ConnectionManager::delay()
{
    sleep(2);
}

void ConnectionManager::create_listener(int PORT, int BACKLOG) {

    bool listener_set = false;

    listeneraddr.sin_family = AF_INET;
    listeneraddr.sin_port = htons(PORT);
    inet_pton(AF_INET, "0.0.0.0", &listeneraddr.sin_addr);
    int attempts = 0;

     while (attempts < 20 && !listener_set) {

        if((listenerfd = socket(AF_INET, SOCK_STREAM, 0)) == -1)
        {
            attempts++;
            delay();
            continue;
        }

        int flags = fcntl(listenerfd, F_GETFL);
        if (flags == -1) {
            perror("F_GETFL listenerfd");
            close(listenerfd);
            delay();
            continue;
        }
        if (fcntl(listenerfd, F_SETFL, flags | O_NONBLOCK) == -1) {
            perror("F_SETFL listenerfd");
            close(listenerfd);
            attempts++;
            delay();
            continue;
        }

        if (bind(listenerfd, (sockaddr*)&listeneraddr, sizeof(listeneraddr)) == -1)
        {
            perror("bind");
            close(listenerfd);
            attempts++;
            delay();
            continue;
        }

        if (listen(listenerfd, BACKLOG) == -1)
        {
            perror("listen");
            close(listenerfd);
            attempts++;
            delay();
            continue;
        }

        listener_set = true;
        break;
    }

     if(!listener_set) {
         perror("create_listener() failed");
         exit(EXIT_FAILURE);
     }
}

void ConnectionManager::manage_connections() {

    // glowna petla, czyli obsluguj wszystkie deskryptory
    while(work)
    {
        std::vector<int> ready_descr = waiter.make_select();

        // patrze ktory deskryptor obudzil selecta (jak select zwroci -1 to vector pusty, jak select wyskoczy to nic do niego nie wlozy wiec tez pusty)
        for(auto &descr : ready_descr)
        {
            if(descr == listenerfd) { // to nasluchiwacz, wiec przyszlo nowe polaczenie od klienta
                handle_new_connection();
            }
            else if(descr == pipe_fd[0]) { // watek nadrzedny cos chce
                if (handle_console_request() == 1) break;
            }
            else { // ktorys klient cos napisal
                handle_client_request(descr);
            }
        }
    }

    close(pipe_fd[0]);
    close(pipe_fd[1]);
    close(listenerfd);
}

