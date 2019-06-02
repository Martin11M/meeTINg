//
// Created by michal on 06.05.19.
//

#include "DataBaseConnection.h"
#include <stdlib.h>
#include <iostream>
#include <mysql_connection.h>
#include <cppconn/driver.h>
#include <cppconn/exception.h>
#include <cppconn/resultset.h>
#include <cppconn/statement.h>
#include <string>


using namespace sql;

DataBaseConnection::DataBaseConnection(string userName, string password) {

    try {

        /* Create a connection */
        driver = get_driver_instance();
        con = driver->connect("tcp://127.0.0.1:3306", "root", "marcin11");
        /* Connect to the MySQL test database */
        con->setSchema("meeting");


    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }

}


bool DataBaseConnection::correctLogon(string userName, string password) {

    try {
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery("select count(*) from USER where username = \"" + userName + "\" and hashed_password = \"" + password + "\"" );
        while (res->next()) {

            if (res->getInt("count(*)") == 1) return 1;

        }
        stmt->close();
        res->close();
        delete res;
        delete stmt;


    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

string DataBaseConnection::userLoginData(string userName) {

    try {
        sql::ResultSet *res;
        string response = ",";
        string rola;

        stmt = con->createStatement();
        res = stmt->executeQuery("select * from USER where username = \'" + userName + "\'");
        while (res->next()) {

            response += "\"id\":\"" + res->getString("user_id") + "\",";
            response += "\"username\":\"" + res->getString("username") + "\",";
            response += "\"password\":\"" + res->getString("hashed_password") + "\",";
            if (res->getString("system_role") == "0") {
                rola = "USER";
            } else rola = "TEAM_LEADER";
            response += "\"systemRole\":\"" + rola + "\"}";

        }
        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }


}

bool DataBaseConnection::correctRegistration(string userName, string password, bool isLeader) {
    int count;
    int indeks = freeID("USER", "user_id");

    try {
        sql::ResultSet *res;

        stmt = con->createStatement();
        res = stmt->executeQuery("select count(*) from USER where username = \'" + userName + "\'");

        while (res->next()) {

            count = res->getInt(1);
        }

        if (count == 0) {

            stmt->executeUpdate(
                    "INSERT INTO USER VALUES(\"" + to_string(indeks) + "\",\"" + userName + "\",\"" + password +
                    "\",\"" + to_string(isLeader) + "\")");

            cout << "INSERT INTO USER VALUES(\"" + to_string(indeks) + "\",\"" + userName + "\",\"" + password +
                    "\",\"" + to_string(isLeader) + "\")";

            return 1;
        }
        stmt->close();
        res->close();
        delete res;
        delete stmt;


    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;

    }

}


void DataBaseConnection::closeConnection() {
    cout << "Close connection" << endl;
    con->close();

    delete con;
}

string DataBaseConnection::userGroupsList(int userId) {

    int iterator = 0;
    string response = "\"items\": [";

    try {
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "select g.group_id, g.name, (select USER.username from USER where user_id = g.leader_id) as leader_name from GROUP_USER gu join GROUPS g on gu.group_id = g.group_id  join USER u on u.user_id = gu.user_id where gu.status = 1 and u.user_id = " +
                to_string(userId));
        while (res->next()) {
            iterator++;
            response += "{\"id\":\"" + res->getString("group_id") + "\",";

            response += "\"name\":\"" + res->getString("name") + "\",";
            response += "\"leader\":\"" + res->getString("leader_name") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "]}";


        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::allGroups(int userId) {

    string response = "\"items\": [";
    int iterator = 0;
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "select g.group_id, g.name, (select USER.username from USER where user_id = g.leader_id) as leader_name from GROUPS g where (select count(*) from GROUP_USER where GROUP_USER.group_id = g.group_id and GROUP_USER.user_id = " + to_string(userId) + " and GROUP_USER.status = 1) = 0 and (select count(*) from GROUP_USER where GROUP_USER.group_id = g.group_id and GROUP_USER.user_id = \"" + to_string(userId) + "\" and GROUP_USER.status = 0) = 0");
        while (res->next()) {
            iterator++;
            response += "{\"id\":\"" + res->getString("group_id") + "\",";
            response += "\"name\":\"" + res->getString("name") + "\",";
            response += "\"leader\":\"" + res->getString("leader_name") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "]}";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::makeGroup(int userId, string groupName) {

    string userName;
    int indeks = freeID("GROUPS", "group_id");
    try {


        sql::ResultSet *res;

        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO GROUPS VALUES(\"" + to_string(indeks) + "\",\"" + to_string(userId) + "\",\"" + groupName + "\")");

        stmt->executeUpdate(
                "INSERT INTO GROUP_USER VALUES(\"" + to_string(indeks) + "\",\"" + to_string(userId) + "\", 1)");

        res = stmt->executeQuery(
                "select username from USER where user_id = " + to_string(userId));
        while (res->next()) {

            userName =  res->getString("username");
        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        string response = " \"id\": \"" + to_string(indeks) + "\",\n  \"name\": \"" + groupName + "\",\n  \"leader\": \"" + userName + "\"\n}\n";
        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }


}

int DataBaseConnection::freeID(string tableName, string idName) {

    int id;

    try {

        sql::ResultSet *res1;

        stmt = con->createStatement();

        res1 = stmt->executeQuery(
                "SELECT min(g." + idName + ") + 1 as 'id' \n" "from " + tableName + " g\n" "left outer join " +
                tableName + " b\n" "on b." +
                idName + "= g." + idName + " +1\n" "where b." + idName + " is null");
        while (res1->next()) {
            id = stoi(res1->getString("id"));

        }

        stmt->close();
        res1->close();
        delete res1;
        delete stmt;

        return id;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
    }
}

bool DataBaseConnection::applyGroup(int userId, int groupId) {
    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO GROUP_USER VALUES(\"" + to_string(groupId) + "\",\"" + to_string(userId) + "\", 0)");


    stmt->close();
    delete stmt;
    return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

string DataBaseConnection::userRequest(int leaderId) {

    string response = "\"items\": [";
    int iterator = 0;
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();
        res = stmt->executeQuery(
                "SELECT g.group_id, g.name, u.username, u.user_id FROM GROUP_USER gu JOIN GROUPS g on g.group_id = gu.group_id join USER u on u.user_id = gu.user_id"
                " WHERE g.leader_id =" + to_string(leaderId) + " and gu.status = 0");
        while (res->next()) {
            iterator++;
            response += "{\"groupId\":\"" + res->getString("group_id") + "\",";
            response += "\"groupName\":\"" + res->getString("name") + "\",";
            response += "\"userName\":\"" + res->getString("username") + "\",";
            response += "\"userId\":\"" + res->getString("user_id") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "]}";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

bool DataBaseConnection::userAccept(int userId, int groupId) {
    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "UPDATE GROUP_USER SET status = 1 WHERE group_id = " + to_string(groupId) + " and user_id = " + to_string(userId));

        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

bool DataBaseConnection::userDecline(int userId, int groupId) {
    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "DELETE FROM GROUP_USER WHERE group_id = " + to_string(groupId) + " and user_id = " + to_string(userId));

        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

string DataBaseConnection::groupEvents(int groupId) {

    string response = "\"items\": [";
    int iterator = 0;

    try {
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT event_id, name FROM EVENT where group_id = " + to_string(groupId));
        while (res->next()) {
            iterator ++;
            response += "{\"id\":\"" + res->getString("event_id") + "\",";
            response += "\"name\":\"" + res->getString("name") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "]}";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }
}

bool DataBaseConnection::makeEvent(int groupId, string eventName) {
    int indeks = freeID("EVENT", "event_id");

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO EVENT VALUES(\"" + to_string(indeks) + "\",\"" + eventName + "\",\"" + to_string(groupId) + "\")");


        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

string DataBaseConnection::showEventOffer(int eventId) {

    string response = "\"offers\": [";
    int iterator = 0;
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();
        res = stmt->executeQuery(
                "SELECT offer_id, start_date, votes_count, accepted_offer, confirmed_offer FROM OFFER WHERE event_id = " + to_string(eventId));
        while (res->next()) {
            iterator++;
            string acceptedOffer = (res->getString("accepted_offer") == "1")? "true" : "false";
            string confirmedOffer = (res->getString("confirmed_offer") == "1")? "true" : "false";

            response += "{\"id\":\"" + res->getString("offer_id") + "\",";
            response += "\"startDate\":\"" + res->getString("start_date") + "\",";
            response += "\"votesCount\":\"" + res->getString("votes_count") + "\",";
            response += "\"acceptedOffer\":\"" + acceptedOffer + "\",";
            response += "\"confirmedOffer\":\"" + confirmedOffer + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "],";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::showEventComment(int eventId) {

    string response = "\"comments\": [";
    int iterator = 0;
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();
        res = stmt->executeQuery(
                "SELECT comment_id, USER.username, message, post_date FROM COMMENT join USER on COMMENT.user_id = USER.user_id WHERE event_id = " + to_string(eventId));
        while (res->next()) {
            iterator++;

            response += "{\"id\":\"" + res->getString("comment_id") + "\",";
            response += "\"username\":\"" + res->getString("username") + "\",";
            response += "\"message\":\"" + res->getString("message") + "\",";
            response += "\"postDate\":\"" + res->getString("post_date") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "],";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::showUserVotes(int eventId, int userId) {

    string response = "\"votes\": [";
    int iterator = 0;
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();
        res = stmt->executeQuery(
                "SELECT VOTE.vote_id, VOTE.offer_id FROM VOTE join OFFER on VOTE.offer_id = OFFER.offer_id where OFFER.event_id = \"" + to_string(eventId) + "\" and VOTE.user_id = " + to_string(userId));
        while (res->next()) {
            iterator++;

            response += "{\"id\":\"" + res->getString("vote_id") + "\",";
            response += "\"offerId\":\"" + res->getString("offer_id") + "\"},";

        }
        if(iterator != 0) response.pop_back();
        response += "]}";

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return response;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::makeOffer(int eventId, int userId, string dateTime) {
    int indeks = freeID("OFFER", "offer_id");

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO OFFER VALUES(\"" + to_string(indeks) + "\",\"" + to_string(eventId) + "\",\"" + to_string(userId) + "\", \"" + dateTime + "\", \"0\" , \" 1 \", \"0\")");


        stmt->close();
        delete stmt;
        return "{\"flag\":\"MAKEOFR\", \" offerId\" : \"" + to_string(indeks) + "\" }";

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

string DataBaseConnection::makePropOffer(int eventId, int userId, string dateTime) {
    int indeks = freeID("OFFER", "offer_id");

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO OFFER VALUES(\"" + to_string(indeks) + "\",\"" + to_string(eventId) + "\",\"" + to_string(userId) + "\", \"" + dateTime + "\", \"0\" , \"0\", \"0\")");


        stmt->close();
        delete stmt;
        return "{\"flag\":\"PROPOFR\", \" offerId\" : \"" + to_string(indeks) + "\" }";

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

bool DataBaseConnection::offerAccept(int offerId) {

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "UPDATE OFFER SET accepted_offer = 1 WHERE offer_id = " + to_string(offerId));

        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

string DataBaseConnection::makeComment(int userId, int eventId, string message, string dateTime) {
    int indeks = freeID("COMMENT", "comment_id");

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO COMMENT VALUES(\"" + to_string(indeks) + "\",\"" + to_string(userId) + "\",\"" +
                to_string(eventId) + "\", \"" + message + "\",\"" + dateTime + "\")");


        stmt->close();
        delete stmt;
        return "{\"flag\":\"COMMENT\", \" commentId\" : \"" + to_string(indeks) + "\" }";

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return "__ERROR";
    }

}

bool DataBaseConnection::makeVote(int offerId, int userId) {
    int indeks = freeID("VOTE", "vote_id");
    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "INSERT INTO VOTE VALUES(\"" + to_string(indeks) + "\",\"" + to_string(offerId) + "\",\"" + to_string(userId) + "\")");

        stmt->executeUpdate("UPDATE OFFER SET votes_count = votes_count + 1 WHERE offer_id = " + to_string(offerId));
        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}

bool DataBaseConnection::offerConfirm(int offerId) {

    try {
        stmt = con->createStatement();
        stmt->executeUpdate(
                "UPDATE OFFER SET confirmed_offer = 1 WHERE offer_id = " + to_string(offerId));
        stmt->executeUpdate(
                "UPDATE OFFER SET confirmed_offer = 0 WHERE OFFER.offer_id != " + to_string(offerId)  + " and event_id IN (SELECT * FROM (select OFFER.event_id from OFFER where offer_id = " + to_string(offerId) + ") AS X)");

        stmt->close();
        delete stmt;
        return 1;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 0;
    }

}
char DataBaseConnection::showAllGroups() {

    string response;

    try {
        int count = 1;

        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT name FROM GROUPS;");
        while (res->next()) {

            cout <<endl << count++ << ".  " << res->getString("name");

        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return 's';

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 'e';
    }
}

char DataBaseConnection::showAllUsers() {

    try {
        int count = 1;

        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT username FROM USER WHERE system_role=0;");
        while (res->next()) {

            cout <<endl << count++ << ".  " << res->getString("username");

        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return 's';

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 'e';
    }
}


char DataBaseConnection::showLeaders() {

    try {
        int count = 1;

        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT username FROM USER where system_role='1';");
        while (res->next()) {

            cout <<endl << count++ << ".  " << res->getString("username");

        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;

        return 's';

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 'e';
    }
}

char DataBaseConnection::deleteUser(int id) {
    try {
        stmt = con->createStatement();

        stmt->executeUpdate(
                "DELETE FROM GROUP_USER WHERE user_id = '" + to_string(id)+"'");
        stmt->executeUpdate(
                "DELETE FROM COMMENT WHERE user_id = '" + to_string(id)+"'");
        stmt->executeUpdate(
                "DELETE FROM VOTE WHERE user_id = '" + to_string(id)+"'");
        stmt->executeUpdate(
                "DELETE FROM OFFER WHERE user_id = '" + to_string(id)+"'");
        stmt->executeUpdate(
                "DELETE FROM USER WHERE user_id = '" + to_string(id)+"'");

        stmt->close();
        delete stmt;
        return 's';

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 'e';
    }
}

char DataBaseConnection::deleteGroup(int id) {
    try {
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT event_id FROM EVENT where group_id='" + to_string(id)+"'");

        while(res->next()){
            sql::ResultSet *res1;
            res = stmt->executeQuery(
                    "SELECT offer_id FROM offer where event_id='" + res->getString("event_id"));
            while(res1->next()){
                stmt->executeUpdate(
                        "DELETE FROM VOTE WHERE offer_id = " + res1->getString("event_id"));
            }
            stmt->executeUpdate(
                    "DELETE FROM OFFER WHERE event_id = " + res->getString("event_id"));
        }
        stmt->executeUpdate(
                "DELETE FROM EVENT WHERE froup_id = " + to_string(id));

        stmt->executeUpdate(
                "DELETE FROM GROUPS WHERE group_id = " + to_string(id));

        stmt->close();
        delete stmt;
        return 's';

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return 'e';
    }
}

int DataBaseConnection::groupCount() {

    try {
        int count = 0;

        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT name FROM GROUPS;");
        while (res->next()) {
            ++count;
        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return count;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return -1;
    }
}

int DataBaseConnection::userCount() {

    try {
        int count = 0;

        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT username FROM USER WHERE system_role=0;");
        while (res->next()) {
            ++count;
        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return count;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return -1;
    }
}


int DataBaseConnection::getGroupID(int choice) {

    try {
        int count = 0;
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT group_id FROM GROUPS;");
        while (res->next()) {
            count++;
            if(count == choice){
                cout <<"UWAGA ID:  "<< stoi(res->getString("group_id"));
                return stoi(res->getString("group_id"));
            }
        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return count;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return -1;
    }
}
int DataBaseConnection::getUserID(int choice) {

    try {
        int count = 0;
        sql::ResultSet *res;

        stmt = con->createStatement();

        res = stmt->executeQuery(
                "SELECT user_id FROM USERS where system_role=0;");
        while (res->next()) {
            ++count;
            if(count == choice){
                cout << "id!!!!!  "<< std::stoi(res->getString("user_id"));
                return  std::stoi(res->getString("user_id"));
            }
        }

        stmt->close();
        res->close();
        delete res;
        delete stmt;
        return count;

    } catch (sql::SQLException &e) {
        cout << "# ERR: SQLException in " << __FILE__;
        cout << "(" << __FUNCTION__ << ") on line " << __LINE__ << endl;
        cout << "# ERR: " << e.what();
        cout << " (MySQL error code: " << e.getErrorCode();
        cout << ", SQLState: " << e.getSQLState() << " )" << endl;
        return -1;
    }
}
