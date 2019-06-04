#ifndef SERVER_PROTOTYPE_WAITER_H
#define SERVER_PROTOTYPE_WAITER_H

#include <arpa/inet.h>
#include <vector>
#include <algorithm>


class Waiter {
    fd_set master; // glowna lista deskryptorow
    fd_set ready; // pomocnicza lista deskryptorow dla selecta
    fd_set errors;
    int fdmax; // numer najwiekszego deskryptora

    int pipe_read; // zeby przy zamykaniu deskryptorow ominac
    int pipe_write; // zeby przy zamykaniu deskryptorow ominac
    int listener; // zeby przy zamykaniu deskryptorow ominac

    int pipe_read2;
    int pipe_write2;

    std::vector<int> descr_in_use; // deskryptory bedace aktualnie w uzyciu

public:
    Waiter(int pr, int pw, int l, int pr2, int pw2) : pipe_read(pr), pipe_write(pw), listener(l), pipe_read2(pr2), pipe_write2(pw2) {
        FD_ZERO(&master);
        FD_ZERO(&ready);
        FD_ZERO(&errors);
        fdmax = 0;
    }

    Waiter() {}

    std::vector<int> make_select() {
        ready = master; // przekopiuj oryginalny set, bo select zmienia seta
        errors = master;
        // TODO sprawdzenie, czy nie wywalilo klienta - errors powinien w jakis sposob zwracac deskryptor, gdzie wystapil blad

        std::vector<int> ready_descr;

        if(select(fdmax+1, &ready, (fd_set *)0, &errors, NULL) == -1) {
            perror("select");
            return ready_descr;
        }

        for(auto &descr : descr_in_use)
        {
            if(FD_ISSET(descr, &ready)) // sprawdzanie czy deskryptor faktcznie jest w zbiorze zmienionych
                ready_descr.push_back(descr);
        }

        return ready_descr;
    }

    void close_descr(int fd) {
        descr_in_use.erase(std::remove(descr_in_use.begin(), descr_in_use.end(), fd), descr_in_use.end()); // niepewny czy to bedzie dzialac
        close(fd);
        FD_CLR(fd, &master);

        if(fd == fdmax)
            fdmax --;
    }

    void add_descr(int fd) {
        descr_in_use.push_back(fd);
        FD_SET(fd, &master);
        set_if_higher_fd(fd);
    }

    void close_all_descr() {
        for(auto &descr : descr_in_use)
        {
            // nie zamykam pipe do lacznosci z watkiem i swojego socketa nasluchujacego
            if(descr != pipe_read && descr != pipe_write && descr != listener && descr != pipe_read2 && descr != pipe_write2)
                close_descr(descr);
        }

        // fdmax ustawiam na najnizsze mozliwe, bo zamknalem wszystkich klientow
        fdmax = pipe_read2;
        set_if_higher_fd(listener);
    }

    void set_if_higher_fd(int fdnew) {
        if(fdnew > fdmax) // jesli ten numer jest wiekszy od aktualnego to zapamietuje
            fdmax = fdnew;
    }
};

#endif //SERVER_PROTOTYPE_WAITER_H