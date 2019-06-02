//
// Created by michal on 06.05.19.
//

#ifndef SERVER_PROTOTYPE_SERVERCONTROLLER_H
#define SERVER_PROTOTYPE_SERVERCONTROLLER_H

#include <string>
#include "DataBaseConnection.h"
#include "ClientStructure.h"
#include "nlohmann/json.hpp"


using namespace std;
using namespace nlohmann;

class ConnectionManager;
class DataBaseConnection;
class ServerController {



public:

    ServerController();
    static void selectAction(int fd, json messageJson, ConnectionManager &cm, DataBaseConnection &dbc);
    static void sendResponse(int fd, string response, ConnectionManager &cm);

    static string userLogin(string, string, DataBaseConnection &dbc);
    static string userRegistration(string, string, DataBaseConnection &dbc);
    static string userGroups(int userId, DataBaseConnection &dbc);
    static string allGroups(int userId, DataBaseConnection &dbc);
    static string makeGroup(int userId, string groupName, DataBaseConnection &dbc);
    static string applyGroup(int userId, int groupId, DataBaseConnection &dbc);
    static string userRequest(int leaderId,  DataBaseConnection &dbc);
    static string userAccept(int userId, int groupId, DataBaseConnection &dbc);
    static string userDecline(int userId, int groupId, DataBaseConnection &dbc);
    static string groupEvents(int groupId, DataBaseConnection &dbc);
    static string makeEvent(int groupId, string eventName, DataBaseConnection &dbc);
    static string showEventOffer(int eventId, int userId, DataBaseConnection &dbc);
    static string makeOffer(int eventId, int userId, string dateTime, DataBaseConnection &dbc);
    static string makePropOffer(int eventId, int userId, string dateTime, DataBaseConnection &dbc);
    static string offerAccept(int offerId, DataBaseConnection &dbc);
    static string makeComment(int userId, int eventId, string message, string dateTime, DataBaseConnection &dbc);
    static string makeVote(int offerId, int userId, DataBaseConnection &dbc);


};


#endif //SERVER_PROTOTYPE_SERVERCONTROLLER_H
