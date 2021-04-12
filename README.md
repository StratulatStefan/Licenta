# LAST DONE
- thread separat la managerul general pe care se solicita de la nodurile interne, adaugarea crc-ului in hearthbeat, astfel incat sa se verifice daca este
  nevoie de replicare; astfel, evitam calcularea crc-ului la fiecare beat.
___

# TO DO
- refactorizare cod; 
    - adaugare parametri in fisierul de configurare
    - adaugare comentarii
- creare si integrare server in spring pentru gestiunea utilizatorilor; adaugarea cantitatii de stocare disponibile pentru fiecare user
    - Calcularea si afisarea cantitatii de stocare disponibile pentru un user
- ~~thread separat la managerul general pe care se solicita de la nodurile interne, adaugarea crc-ului in hearthbeat, astfel incat sa se verifice daca este
nevoie de replicare; astfel, evitam calcularea crc-ului la fiecare beat~~
- ~~Fiecare nod sa trimita informatii despre statusul memoriei sale; informatiile vor fi mentionate in hearthbeat-uri~~
- ~~Nodul general sa aiba un alt thread care verifica daca se pastreaza numarul de replici si sa dea comenzi de replicare la nevoie~~
- ~~Trimitere status operatie inapoi catre frontend (la editare si redenumire)~~
- ~~Coada pentru sync~~
- ~~Probleme CRC~~
- ~~In storagestatustable, crc tre sa fie per nod, nu per fisier~~
- ~~Replicarea unui fisier daca nu mai corespunde CRC-u~~
- ~~Feedback status operatie de la client la frontend~~
- ~~reparare probleme socket-uri~~
- la operatia de redenumire, daca am un nod inchis care contine fisierul, la deschidere sa ne dam seama pe baza crc-ului ca este acelasi fisier
    - <b>Intrebare : </b> Daca am un fisier in care a fost schimbat un octet.. Mecanismul de replicare va vedea ca nu mai corespunde CRC-ul, si va solicita replicare; 
    dar, daca se face undo la operatie (se schimba la loc octetul), eliminam o replica, nu?
- ~~Calcularea si afisarea cantitatii de stocare disponibile pentru un nod~~
- ~~Mecanism de selectare a nodurilor la care sa se stocheze un fisier~~
- Mirror (anycast) pentru managerul general
    - <b>Intrebare : </b>Deci daca am mirror la GM, nu mai am nevoie de refacerea starii, nu?
- Componenta de versionare
- Tratare eficienta exceptii
- Informatii in heartbeat care sa ma ajute sa evidentiez progresul trimiterii fisierului (progress bar in interfata)
- ~~Culoare diferentiatoare mesaj exceptii~~