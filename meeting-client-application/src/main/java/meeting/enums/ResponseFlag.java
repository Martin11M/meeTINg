package meeting.enums;

public enum ResponseFlag {
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
    USERDEC, // potwierdzenie decyzji o nie przyjeciu
    USERACC, // potwierdzenie decyzji o przyjeciu

    GRPEVNT, // eventy grupy
    SHOWEVT, // konkretny event
    MAKEEVT, // pomyślne stworzenie eventu
    RMEVENT, // pomyślne usunięcie eventu

    EVNTOFR, // terminy eventu
    CFRMOFR, // zatwierdź termin
    MAKEOFR, // pomyślne stworzenie nowego terminu
    PROPOFR, // potwierdzenie wysłania propozycji
    RMOFFER, // pomyślne usunięcie terminu
    OFRACPT, // potwierdzenie akceptacji

    NEWVOTE, // pomyślnie zagłosowano
    COMMENT, // pomyślnie dodano komentarz


    __ERROR  // błąd
}
