//
// Created by marjan on 06.05.19.
//

#ifndef SERVER_PROTOTYPE_RESPONSEFLAG_H
#define SERVER_PROTOTYPE_RESPONSEFLAG_H

using namespace std;

enum ResponseFlag {

    LOGGING, // logowanie
    REGISTR, // rejestracja

    USERGRP, // wyświetl grupy użytkownika
    GRPLIST, // pokaż wszystkie dostepne grupy
    MAKEGRP, // stwórz nową grupę
    MEMBREQ, // aplikuj o dolaczenie do grupy

    USERREQ, // wyświetl requesty użytkowników
    USERDEC, // odrzuć użytkownika
    USERACC, // zaakceptuj użytkownika

    GRPEVNT, // wyświetl eventy grupy
    MAKEEVT, // stwórz nowy event


    EVNTOFR, // pokaż terminy eventu
    CFRMOFR, // zatwierdź termin
    MAKEOFR, // stwórz nowy termin
    PROPOFR, // proponuj nowy termin
    OFRACPT, // zaakceptuj termin

    NEWVOTE, // zagłosuj na termin
    COMMENT,  // dodaj komentarz

    __ERROR  // błąd


};

#endif //SERVER_PROTOTYPE_RESPONSEFLAG_H
