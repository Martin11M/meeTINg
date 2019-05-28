//
// Created by marjan on 06.05.19.
//

#ifndef SERVER_PROTOTYPE_RESPONSEFLAG_H
#define SERVER_PROTOTYPE_RESPONSEFLAG_H

using namespace std;

enum ResponseFlag {



    LOGGING, // pomyślne logowanie
    REGISTR, // pomyślna rejestracja

    USERGRP, // grupy użytkownika
    SHOWGRP, // konkretna grupa
    GRPLIST, // wszystkie mozliwe grupy
    MAKEGRP, // pomyślne stworzenie grupy
    RMGROUP, // pomyślne usunięcie grupy
    LEAVGRP, // pomyślne wyjście z grupy
    LEADREQ, // zaaplikowano o status lidera
    LEADRSP, // decyzja o nadaniu statusu lidera
    MEMBREQ, // decyzja o dodaniu do grupy

    USERREQ, // requesty użytkowników
    USERACC, // zaakceptuj zytkownika
    USERDEC, // odrzuc decyzje

    GRPEVNT, // eventy grupy
    SHOWEVT, // konkretny event
    MAKEEVT, // pomyślne stworzenie eventu
    RMEVENT, // pomyślne usunięcie eventu

    EVNTOFR, // terminy eventu
    SHOWAPT, // konkretny termin
    MAKEAPT, // pomyślne stworzenie nowego terminu
    PROPAPT, // potwierdzenie wysłania propozycji
    RMAPTMT, // pomyślne usunięcie terminu
    APTMDEC, // potwierdzenie decyzji

    NEWVOTE, // pomyślnie zagłosowano
    COMMENT, // pomyślnie dodano komentarz


    __ERROR  // błąd


};

#endif //SERVER_PROTOTYPE_RESPONSEFLAG_H
