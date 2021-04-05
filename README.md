# TODO
### Urgent
- ~~Fiecare nod sa trimita informatii despre statusul memoriei sale; informatiile vor fi mentionate in hearthbeat-uri~~
- ~~Nodul general sa aiba un alt thread care verifica daca se pastreaza numarul de replici si sa dea comenzi de replicare la nevoie~~
- ~~Trimitere status operatie inapoi catre frontend (la editare si redenumire)~~
- ~~Coada pentru sync~~
- ~~Probleme CRC~~
- ~~In storagestatustable, crc tre sa fie per nod, nu per fisier~~
- ~~Replicarea unui fisier daca nu mai corespunde CRC-u~~

### Future work
- Mirror  (anycast) pentru managerul general
- Componenta de versionare
- Tratare eficienta exceptii

### Mofturi
- Informatii in heartbeat care sa ma ajute sa evidentiez progresul trimiterii fisierului (progress bar in interfata)
- Culoare diferentiatoare mesaj exceptii


# LASTADDED
- Mecanismul de sincronizare
- La stergerea unui fisier, factorul de replicare devine 0 si starea fisierului devine DELETED; daca cumva invie vreo replica, este stearsa; Daca userul face cerere sa stocheze din nou fisierul, se inlocuieste inregistrarea (factorul de replicare devine noul factor si starea devine valid
- CRC : validarea statusului unui fisier pe baza CRC-ului

# TOASK
- ~~Cum sa tratam refacerea starii nodului general ?~~
- ~~Cum sa se faca stergerea unui fisier de la un nod ? Cine sa o initieze ? Nodul general sau frontendul pe baza tokenului primit de la nodul general?  (in cazul 1, cum trimitem clientului statusul cererii ?)~~
- ~~Cum rezolvam problema de sincronizare, generata la adaugarea unui nou fisier ? (nodul general inregistreaza noul fisier dar nu primeste fisierul in heat-ul de la nodul intern la timp, si genereaza comanda de replicare)~~
- In momentul in care vrem sa adaugam un nou fisier, unde verificam daca acesta exista deja ? In tabela de status, sau cea de content?
- Unde si cum sa stochez statusul stocarii utilizatorilor si al nodurilor interne?
- Este ok sa am mai multe tabele (status stocare, ce fisiere tre sa am, cantitate memorie) iar fiecare sa fie la baza un hashmap in care sa am ca si cheie id ul userului ? Sau ar fi mai ok sa am o clasa cu toate datele necesare fiecarui utilizator ? 
- Cum ar fi mai eficient sa pastrez datele unui fisier ? De ex, dimensiunea ? (ne gandim la cazul initializatii nodului general, in care stim doar numele fisierului, nu si dimensiunea)
- Sa criptam token-ul?
- Cum sa procedez cu partea de versionare?
- Sa trimit CRC-ul la fiecare beat ? La unele fisiere mai mari, dureaza destul de mult.
- Daca am un fisier in care a fost schimbat un octet.. Mecanismul de replicare va vedea ca nu mai corespunde CRC-ul, si va solicita replicare; dar, daca se face undo la operatie (se schimba la loc octetul), eliminam o replica, nu?
