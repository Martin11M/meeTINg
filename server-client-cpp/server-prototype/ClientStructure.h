#ifndef SERVER_PROTOTYPE_CLIENTSTRUCTURE_H
#define SERVER_PROTOTYPE_CLIENTSTRUCTURE_H

#include "PackageSizeParser.h"
#include <string.h>
#include <arpa/inet.h>
#include <iostream>


class ClientStructure {

    char header[4]; // bufor na naglowek
    int bytes_received; // ile bajtow juz otrzymalem
    int message_size; // na poczatku -1, jak przeczytam naglowek to wtedy wiem ile wiadomosc ma bajtow
    int whole_package_size; // ile zawiera bajtow caly pakiet, -1 -nieznane
    int bytes_needed; // ile potrzebuje jeszcze bajtow
    char *buffer_message; // bufor ktory zbiera wiadomosc
    char *receive_buffer; // bufor do ktorego laduje to co otrzymalismy z receive
    int my_fd; // deskryptor klienta ktory jest wlascicielem struktury

public:

    ClientStructure(int fd) {
       // init();
        memset(header, 0, 4);

        // jak bytes needed na poczatku dam np 2, to nie zaalokuje nowej, a ta bedzie tylko [2], i przepelnie przy drugiej probie zapisania
        bytes_needed = 128; // ile chcemy za pierwszym zamahem pobrac bajtow
        buffer_message = new char[bytes_needed]; // robie bufor zeby pomiescil ten pierwszy rzut
        message_size = -1;
        bytes_received = 0;
        whole_package_size = -1;
        my_fd = fd;
    }

    ClientStructure() {}

    void init() {
        memset(header, 0, 4);

        // jak bytes needed na poczatku dam np 2, to nie zaalokuje nowej, a ta bedzie tylko [2], i przepelnie przy drugiej probie zapisania
        bytes_needed = 128; // ile chcemy za pierwszym zamahem pobrac bajtow
        buffer_message = new char[bytes_needed]; // robie bufor zeby pomiescil ten pierwszy rzut
        message_size = -1;
        bytes_received = 0;
        whole_package_size = -1;
    }

    void set_part_message(int bytes) {
        //std::cout << "-- CLI_STRUCT --- OTRZYMALEM: " << bytes << std::endl;

        memcpy(&buffer_message[bytes_received], receive_buffer, bytes);
        bytes_received += bytes;
        bytes_needed -= bytes;

        // jesli mamy juz naglowek a jeszcze nie wyliczylismy rozmiaru, to go wyliczmy
        if (bytes_received >= 4 && message_size == -1) {
            memcpy(header, buffer_message, 4);
            message_size = PackageSizeParser::parse_int_32(header);
            whole_package_size = message_size + 4;

            // naglowek juz mam wiec potrzebuje jeszcze tyle ile ma cala paczka minus to co juz odebralem
            bytes_needed = whole_package_size - bytes_received;

            // jak juz wiem ile bajtow ma cala paczka, to alokuje bufor i przerzucam tam dotychczasowe dane, tamten bufor kasuje
            char *tmp = buffer_message;
            buffer_message = new char[whole_package_size];
            memcpy(buffer_message, tmp, bytes_received);
            delete[] tmp;
        }

        // dealokacja bufora receive_buffer, bo zakonczylismy odbior pakietu
        delete [] receive_buffer;

        //std::cout << "-- CLI_STRUCT --- MAM LACZNIE: " << bytes_received << std::endl;
        //std::cout << "-- CLI_STRUCT --- A CALY PAKIET MA: " << whole_package_size << std::endl;
    }

    int receive_part_message() {
        // przygotowuje bufor, wiadomosc moze byc mniejsza, jest on dealokowany w set_part_message
        receive_buffer = new char[bytes_needed];

        int nbytes_rec = recv(my_fd, receive_buffer, bytes_needed, 0);

        // jesli rozlaczyl sie lub sa bledy, to daje znac connection managerowi
        if(nbytes_rec  <= 0)
        {
            if(nbytes_rec  == 0) // koniec lacznosci
                return -1;
            else                 // blad
                return -2;
        }

        // wrzuc do bufora odebrana porcje
        set_part_message(nbytes_rec);

        // daje znac connection managerowi ze odebralem caly pakiet
        if(bytes_needed <= 0)
            return 1;

        // jesli wczesniej nie zwrocilem -1/-2 - blad lub 1 - caly pakiet odebrany, to daje znac ze po prostu odebralem porcje
        return 0;
    }

    void dealloc() {
        delete[] buffer_message;
    }

    int get_bytes_needed() {
        return bytes_needed;
    }

    int get_whole_package_size() {
        return whole_package_size;
    }

    char *get_buffer_message() {
        return buffer_message;
    }
};


#endif //SERVER_PROTOTYPE_CLIENTSTRUCTURE_H
