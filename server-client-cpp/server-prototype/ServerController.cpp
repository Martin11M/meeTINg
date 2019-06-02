//
// Created by michal on 06.05.19.
//

#include "ServerController.h"
#include "nlohmann/json.hpp"
#include "ClientStructure.h"
#include "DataBaseConnection.h"
#include "ConnectionManager.h"
#include "PackageSizeParser.h"
#include "ResponseFlag.h"
#include <string>
#include <iostream>
#include <cstring>
#include <boost/config.hpp>


using namespace nlohmann;
using namespace std;


ResponseFlag convert(const std::string& str)
{
    if(str == "LOGGING") return LOGGING;
    else if(str == "REGISTR") return REGISTR;
    else if(str == "USERGRP") return USERGRP;
    else if(str == "GRPLIST") return GRPLIST;
    else if(str == "MAKEGRP") return MAKEGRP;
    else if(str == "MEMBREQ") return MEMBREQ;
    else if(str == "USERREQ") return USERREQ;
    else if(str == "USERACC") return USERACC;
    else if(str == "USERDEC") return USERDEC;
    else if(str == "GRPEVNT") return GRPEVNT;
    else if(str == "MAKEEVT") return MAKEEVT;
    else if(str == "EVNTOFR") return EVNTOFR;
    else if(str == "MAKEOFR") return MAKEOFR;
    else if(str == "PROPOFR") return PROPOFR;
    else if(str == "OFRACPT") return OFRACPT;
    else if(str == "COMMENT") return COMMENT;

}


ServerController::ServerController() {

}

void ServerController::selectAction(int fd, json messageJson, ConnectionManager &cm, DataBaseConnection &dbc) {
    string response;

    ResponseFlag enumFlag = convert(messageJson["flag"]);

    switch(enumFlag) {
        case LOGGING:
            response = userLogin(messageJson["username"], messageJson["password"] , dbc);
            break;
        case REGISTR:
            response = userRegistration(messageJson["username"], messageJson["password"], dbc);
            break;
        case USERGRP:
            response = userGroups(messageJson["userId"], dbc);
            break;
        case GRPLIST:
            response = allGroups(messageJson["userId"], dbc);
            break;
        case MAKEGRP:
            response = makeGroup(messageJson["leaderId"], messageJson["groupName"],  dbc);
            break;
        case MEMBREQ:
            response = applyGroup(messageJson["userId"], messageJson["groupId"],  dbc);
            break;
        case USERREQ:
            response = userRequest(messageJson["leaderId"],  dbc);
            break;
        case USERACC:
            response = userAccept(messageJson["userId"], messageJson["groupId"], dbc);
            break;
        case USERDEC:
            response = userDecline(messageJson["userId"], messageJson["groupId"], dbc);
            break;
        case GRPEVNT:
            response = groupEvents(messageJson["groupId"], dbc);
            break;
        case MAKEEVT:
            response = makeEvent(messageJson["groupId"],messageJson["eventName"], dbc);
            break;
        case EVNTOFR:
            response = showEventOffer(messageJson["eventId"], messageJson["userId"], dbc);
            break;
        case MAKEOFR:
            response = makeOffer(messageJson["eventId"],messageJson["userId"], messageJson["date"], dbc);
            break;
        case PROPOFR:
            response = makePropOffer(messageJson["eventId"],messageJson["userId"], messageJson["date"], dbc);
            break;
        case OFRACPT:
            response = offerAccept(messageJson["offerId"], dbc);
            break;
        case COMMENT:
            response = makeComment(messageJson["userId"],messageJson["eventId"], messageJson["message"], messageJson["postDate"], dbc);
            break;
        default:
            cout << "default switch" << endl;
            break;
    }

    sendResponse(fd, response, cm);
}

void ServerController::sendResponse(int fd, string response, ConnectionManager &cm){
    cout << response << endl;
    char header[4];
    PackageSizeParser::serialize_int_32(header, response.size());

    char messageResponse[response.size()];
    memcpy(messageResponse, response.c_str(), response.size());

    char whole_pack[response.size()+4];
    memcpy(whole_pack, header, 4);
    memcpy(&whole_pack[4], messageResponse, response.size());

    int bytes = response.size()+4;

    if (cm.send_all(fd, whole_pack, &bytes) == -1) {
        perror("send");
        printf("~~ Sent only %d bytes (error) \n", bytes);
    }

}

string ServerController::userLogin(string userName, string userPassword, DataBaseConnection &dbc) {
    string returnMessage;

    if (!dbc.correctLogon(userName, userPassword)) {
        returnMessage = "{\"flag\":\"__ERROR\"}";
    } else {
        returnMessage = "{\"flag\":\"LOGGING\"";
        returnMessage += dbc.userLoginData(userName);
    }
    return returnMessage;
}

string ServerController::userRegistration(string userName, string userPassword, DataBaseConnection &dbc) {
    string returnMessage;

    if (!dbc.correctRegistration(userName, userPassword)) {
        returnMessage = "{\"flag\":\"__ERROR\"}";
    } else {
        returnMessage = "{\"flag\":\"REGISTR\"}";
    }
    return returnMessage;
}

string ServerController::userGroups(int userId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"USERGRP\",";
    returnMessage += dbc.userGroupsList(userId);

    return returnMessage;
}

string ServerController::allGroups(int userId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"GRPLIST\",";
    returnMessage += dbc.allGroups(userId);

    return returnMessage;
}

string ServerController::makeGroup(int userId, string groupName, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"MAKEGRP\",";
    returnMessage += dbc.makeGroup(userId, groupName);

    return returnMessage;
}

string ServerController::applyGroup(int userId, int groupId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"MEMBREQ\"}";
    if (dbc.applyGroup(userId, groupId)) return returnMessage;

    returnMessage = "{\"flag\":\"__ERROR\"}";
    return returnMessage;
}

string ServerController::userRequest(int leaderId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"USERREQ\",";
    returnMessage += dbc.userRequest(leaderId);

    return returnMessage;
}

string ServerController::userAccept(int userId, int groupId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"USERACC\"}";
    if (dbc.userAccept(userId, groupId)) return returnMessage;

    returnMessage = "{\"flag\":\"__ERROR\"}";
    return returnMessage;
}

string ServerController::userDecline(int userId, int groupId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"USERDEC\"}";
    if (dbc.userDecline(userId, groupId)) return returnMessage;

    returnMessage = "{\"flag\":\"__ERROR\"}";
    return returnMessage;
}

string ServerController::groupEvents(int groupId, DataBaseConnection &dbc)  {
    string returnMessage;

    returnMessage = "{\"flag\":\"GRPEVNT\",";
    returnMessage += dbc.groupEvents(groupId);

    return returnMessage;
}

string ServerController::makeEvent(int groupId, string eventName, DataBaseConnection &dbc)  {
    string returnMessage;

    returnMessage = "{\"flag\":\"MAKEEVT\", \"id\": \"" + to_string(groupId) + "\", \"name\": \"" + eventName + "\" }";
    if (dbc.makeEvent(groupId, eventName)) return returnMessage;

    returnMessage = "{\"flag\":\"__ERROR\"}";
    return returnMessage;
}

string ServerController::showEventOffer(int eventId, int userId, DataBaseConnection &dbc)  {
    string returnMessage;

    returnMessage = "{\"flag\":\"EVNTOFR\",";
    returnMessage += dbc.showEventOffer(eventId) + dbc.showEventComment(eventId) + dbc.showUserVotes(eventId, userId);

    return returnMessage;
}


string ServerController::makeOffer(int eventId, int userId, string dateTime , DataBaseConnection &dbc)  {
    string returnMessage = dbc.makeOffer(eventId, userId, dateTime);

    if (returnMessage == "_ERROR") return "{\"flag\":\"__ERROR\"}";

    return returnMessage;
}

string ServerController::makePropOffer(int eventId, int userId, string dateTime , DataBaseConnection &dbc)  {
    string returnMessage = dbc.makePropOffer(eventId, userId, dateTime);

    if (returnMessage == "_ERROR") return "{\"flag\":\"__ERROR\"}";

    return returnMessage;
}

string ServerController::offerAccept(int offerId, DataBaseConnection &dbc) {
    string returnMessage;

    returnMessage = "{\"flag\":\"OFRACPT\"}";
    if (dbc.offerAccept(offerId)) return returnMessage;

    returnMessage = "{\"flag\":\"__ERROR\"}";
    return returnMessage;
}

string ServerController::makeComment(int userId, int eventId, string message, string dateTime , DataBaseConnection &dbc)  {
    string returnMessage = dbc.makeComment(userId, eventId, message, dateTime);

    if (returnMessage == "_ERROR") return "{\"flag\":\"__ERROR\"}";

    return returnMessage;
}


