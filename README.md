# LAST DONE
- Mecanismul de sincronizare (cu mentiunea ca s-a adaugat o coada in plus)
    - <span style="color:red"><b>Intrebare : </b> trimitem inapoi la frontend ok la stocarea fisierului ?</span>
- La stergerea unui fisier, factorul de replicare devine 0 si starea fisierului devine DELETED; daca cumva invie vreo replica, este stearsa; 
- Daca userul face cerere sa stocheze din nou fisierul, se inlocuieste inregistrarea (factorul de replicare devine noul factor si starea devine valid
- CRC : validarea statusului unui fisier pe baza CRC-ului; daca am un fisier modificat (nu mai corespunde crc-ul), momentam il sterg.
- A trebuit sa adaug la nodul intern o logica suplimentara de evitare a calcularii a crc-ului atunci cand fisierul este
in pending , adica in procesul de salvare; evitam doar calcularea crc-ului deoarece pentru fisiere mari este o operatie foarte
costisitoare; ne asiguram la nodul general ca nu bagam in seama acest crc cand timp fisierul este in pending
    - <span style="color:red"><b>Intrebare</b> : Mare parte din sistem imi este dat peste cap de calcularea crc-ului.</span>
- Feedback de la nodul intern la frontend (daca de ex solicit o redenumire, nodul general asteapta feedback de la toate fisierele care trebuie
sa faca redenumire; si daca cel putin unul a reusit, intoarcem raspuns valid catre frontend)
- afisarea statusului stocarii nodurilor interne (se adauga la fiecare heartbeat si se afiseaza)
    - <span style="color:red"><b>Intrebare : </b> Unde sa tin evidenta de cantitatea de memorie disponibila pentru un user ? (server useri sau contenttable ?)</span>

___

# TO DO

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
    - <span style="color:red"><b>Intrebare : </b> Daca am un fisier in care a fost schimbat un octet.. Mecanismul de replicare va vedea ca nu mai corespunde CRC-ul, si va solicita replicare; 
    dar, daca se face undo la operatie (se schimba la loc octetul), eliminam o replica, nu?</span>
- ~~Calcularea si afisarea cantitatii de stocare disponibile pentru un nod~~
- Calcularea si afisarea cantitatii de stocare disponibile pentru un user
- Mecanism de selectare a nodurilor la care sa se stocheze un fisier
- Mirror (anycast) pentru managerul general
    - <span style="color:red"><b>Intrebare : </b>Deci daca am mirror la GM, nu mai am nevoie de refacerea starii, nu?</span>
- Componenta de versionare
- Tratare eficienta exceptii
- Informatii in heartbeat care sa ma ajute sa evidentiez progresul trimiterii fisierului (progress bar in interfata)
- Culoare diferentiatoare mesaj exceptii