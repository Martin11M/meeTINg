#include <iostream>
#include <unistd.h>
#include "ConsoleManager.h"

using namespace std;

void ConsoleManager::runConsole(int readfd, int writefd, int readfd2, int writefd2) {
    char message;

    while(message != 'q')
    {
        int choice;
        do
        {
            choice = -1;
            cin.clear();
            cout << "---- KONSOLA ADMINA ----"<<endl;
            cout << "1. Zamknij wszystkie polaczenia (wysyla komunikat x)"<<endl;
            cout << "2. Zamknij polaczenia i serwer (wysyla komunikat q)"<<endl;
            cout << "3. Wyswietl wszystkie grupy"<< endl;
            cout << "4. Wyswietl wszystkich uzytkownikow"<< endl;
            cout << "5. Wyswietl uztkownikow ze statusem Lider"<< endl;
            cout << "6. Usun uzytkownika"<< endl;
            cout << "7. Usun grupe" << endl;

            cout <<endl<< "Wybierz opcje: ";
            cin >> choice;
        } while(choice < 1 || choice > 7);

        switch(choice)
        {
            case 1:
                message = 'x';
                break;
            case 2:
                message = 'q';
                break;
            case 3:
                message = 'r';
                break;
            case 4:
                message = 't';
                break;
            case 5:
                message = 'y';
                break;
            case 6:
                message = 'u';
                break;
            case 7:
                message = 'i';
                break;
            default:
                message = 'd';
                break;
        }

        if(write(writefd, &message, sizeof(message)) == -1)
        {
            perror("write");
            exit(EXIT_FAILURE);
        }

  //      if (message == 's' || message == 'e')
//        {
            char receive;
            if(read(readfd2, &receive, 1) == -1)
            {
                perror("read_readfd_pipe 2");
                exit(EXIT_FAILURE);
            }
 //       }
    }
}
