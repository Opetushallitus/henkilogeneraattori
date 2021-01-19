# Henkilögeneraattori

Tähän gitrepoon on poimittu pieni pala koodia vanhasta datamigraatio-reposta. Tämä palanen on ohjelma, jota käytetään
anonyymin henkilötiedon generointiin. Muut osat datamigraatio-reposta ovat vanhentuneet ja eivät enää käytössä, ja tämä
käytössä oleva osa saa uuden elämän tässä uudessa repossa.

Generaattoria käytetään opintopolun datantuontiautomaatiosta käsin. Jos ohjelman CLI-rajapintaan tulee muutoksia, pitää
päivittää myös sitä käyttävät skriptit datantuontiautomaation puolella.

Ohjelmaa ajetaan mavenin avulla näin:

```
mvn clean install
mvn exec:java -Dexec.mainClass=fi.vm.sade.conversion.hakemus.henkilogenerator.HenkiloGenerator \
-Dexec.args="<lkm> <ensimmäinen syntymävuosi> <viimeinen syntymävuosi> <AES key> <AES salt> <SHA salt>"
```

Argumentteina annetaan generoitavien henkilöiden lukumäärä, syntymävuosi-väli, ja kolme vanhan haku-appin hakemustietojen
kryptaamisessa käytettävää avainta/suolaa. (Vanhan haku-appin jutut, kolme viimeistä parameteria, pitäisi voida poistaa 
tästä kevään 2021 jälkeen.)

Generoitu data tulee tiedostoon `henkilot.json`, joka sisältää yhden json-objektin per rivi, kussakin objektissa yhden
generoidun henkilön tiedot.

Syntymävuosivälissä sekä ylä- että alaraja ovat inklusiivisia. Esim. 1999-2001 generoi henkilöitä jotka ovat syntyneet
vuosina 1999, 2000 ja 2001.

Henkilöitä pystyy generoimaan enintään 100 / päivä eli n. 35 000 / vuosi. Aivan maksimimäärää 36 500 / vuosi ei kannata
yrittää, koska generointi hidastuu jyrkästi kun mennään lähelle absoluuttista ylärajaa.

Syntymävuosirajausta käytetään siihen, että vältetään törmäyksiä testiympäristössä jo olevien hetujen kanssa. Esim. jos
tiedetään että aiemmin tuotu data sisältää henkilöitä syntymävuosilla 1920-2020, voidaan seuraava satsi generoida
vaikka syntymävuosilla 1915-1919.
