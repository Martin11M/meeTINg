//
// Created by marjan on 06.05.19.
//

#ifndef SERVER_PROTOTYPE_RESPONSEFLAG_H
#define SERVER_PROTOTYPE_RESPONSEFLAG_H

using namespace std;

enum ResponseFlag {



    LOGGING, // logowanie
    SIGNOUT, // wylogowanie - zastanowić się, czy musimy wysyłać requesta, żeby się wylogować
    REGISTR, // rejestracja

    USERGRP, // wyświetl grupy użytkownika
    SHOWGRP, // pokaż konkretną grupę <-----czym sie rozni od: GRPEVNT, // wyświetl eventy grupy
    GRPLIST, // pokaż wszystkie dostepne grupy
    MAKEGRP, // stwórz nową grupę
    RMGROUP, // usuń grupę
    LEAVGRP, // wyjdź z grupy
    LEADREQ, // aplikuj o status lidera
    MEMBREQ, // aplikuj o dolaczenie do grupy

    USERREQ, // wyświetl requesty użytkowników
    USERDEC, // odrzuć użytkownika
    USERACC, // zaakceptuj użytkownika

    GRPEVNT, // wyświetl eventy grupy
    SHOWEVT, // pokaż konkretny event
    MAKEEVT, // stwórz nowy event
    RMEVENT, // usuń event

    EVNTOFR, // pokaż terminy eventu
    CFRMOFR, // zatwierdź termin
    SHOWOFR, // wybierz konkretny termin
    MAKEOFR, // stwórz nowy termin
    PROPOFR, // proponuj nowy termin
    RMOFFER, // usuń termin
    OFRACPT, // zaakceptuj termin

    NEWVOTE, // zagłosuj na termin
    COMMENT,  // dodaj komentarz


    __ERROR  // błąd


};

#endif //SERVER_PROTOTYPE_RESPONSEFLAG_H
