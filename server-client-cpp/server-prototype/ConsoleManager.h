#ifndef SERVER_PROTOTYPE_CONSOLEMANAGER_H
#define SERVER_PROTOTYPE_CONSOLEMANAGER_H

#include "DataBaseConnection.h"
#include <cstdlib>
class ConsoleManager {

private:
    DataBaseConnection dbc;

public:
    static void runConsole(int readfd, int writefd, int readfd2, int writefd2);
};



#endif //SERVER_PROTOTYPE_CONSOLEMANAGER_H
