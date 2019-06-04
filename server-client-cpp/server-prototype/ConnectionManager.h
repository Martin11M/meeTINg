#ifndef SERVER_PROTOTYPE_CONNECTIONMANAGER_H
#define SERVER_PROTOTYPE_CONNECTIONMANAGER_H

#include <map>
#include "ClientStructure.h"
#include "ServerController.h"
#include "Waiter.h"
#include <netinet/in.h>


class ConnectionManager {

private:
    std::map<int, ClientStructure> cli_struct; // mapuje deskryptor klienta -> struktura klienta, w niej gromadzone sa otrzymane bajty
    Waiter waiter; // obiekt zarzadzajacy zbiorami deskryptorow i selectem

    int listenerfd; // deskryptor gniazda nasluchujacego
    sockaddr_in listeneraddr; // adres nasluchujacego
    int pipe_fd[2];
    int pipe_fd2[2];

    char buf; // jednobajtowe komunikaty od watka nadrzednego
    bool work = true;

    ServerController sc;
    DataBaseConnection &dbc;
public:

    ConnectionManager(DataBaseConnection &dbc, void*, int);
    void manage_connections();
    void create_listener(int, int);
    void handle_new_connection();
    int handle_console_request();
    void handle_client_request(int);
    int send_all(int, char*, int*);
    void delay();
    int showGroupMenu();
    int showUserMenu();
};

#endif //SERVER_PROTOTYPE_CONNECTIONMANAGER_H