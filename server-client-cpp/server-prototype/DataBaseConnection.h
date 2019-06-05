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

    sql::Driver *driver;
    sql::Statement *stmt;
    sql::Connection *con;

public:


    DataBaseConnection(string userName, string password);
    bool testConnection();
    bool createConnection();
    bool correctLogon(string userName, string password);
    void usersList();
    bool closeConnection();
    string userLoginData(string userName);
    bool correctRegistration(string userName, string password, bool isLeader);
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
    string showEventOffer(int eventId);
    string showEventComment(int eventId);
    string showUserVotes(int eventId, int userId);
    string makeOffer(int groupId, int userId, string dateTime);
    string makePropOffer(int groupId, int userId, string dateTime);
    bool offerAccept(int offerId);
    bool offerConfirm(int offerId);
    string makeComment(int userId, int eventId, string message, string dateTime);
    bool makeVote(int offerId, int userId);

    char showAllGroups();
    char showAllUsers();
    char showLeaders();
    char deleteGroup(int);
    char deleteUser(int);
    int groupCount();
    int userCount();
    int getGroupID(int choice);
    int getUserID(int choice);
};


#endif //TESTDB_DATABASECONNECTION_H