# LAST DONE
- thread separat la managerul general pe care se solicita de la nodurile interne, adaugarea crc-ului in hearthbeat, astfel incat sa se verifice daca este
  nevoie de replicare; astfel, evitam calcularea crc-ului la fiecare beat.
- cand invie o replica, sa trimita filestoragestatus complet (cu tot cu crc) la primul beat
  
___

# TO DO
- ~~rezolvare problema redenumire; cand un nod moare si, intre timp, unul dintre fisierele existente la acel nod este redenumit; daca nodul invie, va declara fosta
versiune a fisierului, insa in content table nu va mai exista.. deci tre <b>cumva</b> sters..~~
    - ~~la bucla de replicare tre avut grija..~~
- ~~cand invie o replica, sa trimita filestoragestatus complet (cu tot cu crc) la primul beat~~
- trimitere sms la administrator atunci cand un nod nu mai are capacitate de stocare sau avem prea multe erori la un harddisk
- ~~creare si integrare server in spring pentru gestiunea utilizatorilor; adaugarea cantitatii de stocare disponibile pentru fiecare user~~
    - ~~Calcularea si afisarea cantitatii de stocare disponibile pentru un user~~
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
- ~~Calcularea si afisarea cantitatii de stocare disponibile pentru un nod~~
- ~~Mecanism de selectare a nodurilor la care sa se stocheze un fisier~~
- Mirror (anycast) pentru managerul general
    - <b>Intrebare : </b>Deci daca am mirror la GM, nu mai am nevoie de refacerea starii, nu?
- ~~Componenta de versionare~~
- Informatii in heartbeat care sa ma ajute sa evidentiez progresul trimiterii fisierului (progress bar in interfata)
- ~~Culoare diferentiatoare mesaj exceptii~~

___

# La final
- Tratare eficienta exceptii
- Refactorizare
    - Adaugare/completare comentarii
    - Adaugare parametrii in fisierul de configurare
    - Restructurare cod
    - Restructurare pachete si clase
___

# Controlul versiunilor

- ~~adaugare descriere in versiune~~
    - <b>Pentru FRONTEND</b>
        - daca descrierea primita de la client/nodul general este nula, se foloseste o descriere generica
        - aceasta descriere generica este generata la client; (ca la git la update readme.md)
- ~~in heartbeat sa nu se trimita si fisierele de metadate, adica nodul intern sa ignore fisierele cu .metadata in getStorageStatus~~
- ~~verificare daca crc-ului ia in calcul si numele unui document~~
    - <b>se pastreaza crc la redenumire; deci crc se face doar pe content</b>
- ~~pentru fiecare fisier nou creat, sa se creeze un fisier aditional de forma <nume_fisier>.metadata;~~
    - ~~in acest fisier se va stoca data la care a fost salvat, numarul versiunii, crc-ul si descrierea~~
    - ~~de fiecare data cand se modifica ceva in continutul fisierului (fisiere text), se actualizeaza acest fisier de .metadata cu noua versiune si noul crc~~
    - ~~la stergerea unui fisier, se elimina si fisierul de metadate~~
    - ~~la redenumirea unui fisier, crc-ul se pastreaza, deci se schimba tot inafara de crc; se redenumeste si fisierul de metadate~~
    - ~~cand se face replicarea, se copie si fisierul de metadate~~
- ~~La cererea de incarcare a unui nou fisier care exista deja, verificarea existentei sa se faca pe baza numelui <b>si a crc-ului</b>~~
        - ~~daca avem un fisier cu acelasi nume si crc, sa nu putem sa il incarcam..~~
        - ~~daca are acelasi nume dar crc diferit sa se poata incarca;~~
        - ~~daca avem nume diferit,il incarcam fara probleme~~
- ~~sa se trimita numarul versiunii in beat~~
- ~~sa se adauge versiunea in storagestatus si in contenttable~~
- ~~sa se tina cont, atat de crc, cat si de versiune~~

___

# Frontend / Backend
- aplicatia are 2 tipuri de utilizatori
    - utilizator obisnuit al aplicatiei
    - admin : exista un cont predefinit al adminului, creat manual; 
        - in meniul de create account, nu se poate crea un admin; in schimb, un admin are posibilitatea de a adauga alt admin in sistem
- daca se inregistreaza coruperi ale sistemului de stocare la un anumit nod (un fisier devine corupt din senin), nodul general ca inregistra aceasta corupere intr-o tabela;
mai departe, in interfata, admin-ul va avea disponibila o fereastra unde va putea vedea aceste fail-uri ale nodurilor.
- cand testezi pe target, sa tii minte sa implementezi (sa termini) cautarea in log dupa date