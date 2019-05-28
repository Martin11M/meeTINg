//
// Created by michal on 06.05.19.
//

#ifndef TESTDB_DATABASECONNECTION_H
#define TESTDB_DATABASECONNECTION_H

#include <string>
#include <mysql_connection.h>
#include "DataBaseConnection.h"

using namespace std;
using namespace sql;


class DataBaseConnection {

    int userId = 0;    //id zalogowanego uzytkownika
    sql::Connection *con;
    sql::Driver *driver;
    sql::Statement *stmt;
public:

    DataBaseConnection(string userName, string password);
    bool correctLogon(string userName, string password);
    void usersList();
    void closeConnection();
    string userLoginData(string userName);
    bool correctRegistration(string userName, string password);
    //ResultSet groupsList();
    string userGroupsList(int userId);
    string allGroups(int userId);
    string makeGroup(int userId, string groupName);
    int freeID(string tableName, string idName);
    bool applyGroup(int userId, int groupId);
    string userRequest(int leaderId);
    bool userAccept(int userId, int groupId);
    bool userDecline(int userId, int groupId);
    string groupEvents(int groupId);
    bool makeEvent(int groupId, string eventName);

};


#endif //TESTDB_DATABASECONNECTION_H
