package fi.vm.sade.conversion.hakemus.henkilogenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Generoi halutun määrän testihenkilöitä. Huom! Tarkista että alla olevat salausavaimet vastaavat ko. ympäristön salausavaimia.
 * Eli mikäli hakemuksia ollaan anonymisoimassa esim. QA-ympäristöön, pitää salausavaimet vastata QA:n haku-app -sovelluksen avaimia.
 *
 * main()-metodi luo projektin juuritasolle "henkilos.json"-tiedoston, joka mongoimportin ymmärtämässä muodossa.
 */
public class HenkiloGenerator {
    // reppu
    private static final String AES_KEY = "tahantuleelaittaajotainfiksua";
    private static final String AES_SALT = "tassapitaaolla32merkkiapitkajutt";
    private static final String SHA_SALT = "tassajotainkummaakanssa";
    private static final int GENEROITTAVIEN_HETUJEN_MAARA = 100;
    private static final boolean ONLY_UNIQUE_HETUS = true;

    private static AESEncrypter aesEncrypter;
    private static SHA2Encrypter sha2Encrypter;
    final String[] lahiosoitteet = new String[] {
            "Amerikkakuja", "Egyptinkorventie", "Haisuvaarankatu", "Hippitie", "Hohokatu", "Horontaipaleenpolku",
            "Horonpohjantie", "Hyväneulankatu", "Jänishaikulankuja", "Kakadunkatu", "Kilontie", "Kökköpolku",
            "Maailmannavanaukio", "Matomäenranta", "Onpahanvaanlammensammal", "Puujalkalanlaituri",
            "Ronkelinraitti", "Verhonkulmala", "Vitsinraitti", "Volttipelto", "Vihavuodentie", "Halivaarankatu",
            "Hankalankuja", "Hiljaisten miesten laakso", "Hunningonpolku", "Jalanluiskahtamavaarankuja", "Junttilantie",
            "Kalmankaltionkuja", "Kalmarinraitti", "Kalsaari", "Katinhännänraitti", "Katumatie", "Ketunperänpolku",
            "Kinkkuaukio", "Kouralehto", "Kumiakatu", "Kuolionkuja", "Kuoppamäki", "Kurinraitti", "Kuumakallio",
            "Käpälämäki", "Kärpäsenkuja", "Laholanaukio", "Leipeekorpi", "Livohkankuja", "Löytöpolku", "Metelitie",
            "Monniraitti", "Muhkuripolku", "Nakertajanraitti", "Palatkannotko", "Pentinmäki", "Polvivaara", "Pomovaara",
            "Punkkapolku", "Risteysaukio", "Rämsöönranta", "Sveitsinpyhtää", "Takapajulanraitti", "Umpilampi",
            "Veneheittoaukio", "Venetsianpolku", "Vääkiönsammal", "Yläpääntie", "Öllölänraitti",
            "Äteritsiputeritsipuolilautatsijänkä"
    };
    final String[] etunimet_miehet = mkString("src/main/resources/etunimet-miehet.txt");
    final String[] etunimet_naiset = mkString("src/main/resources/etunimet-naiset.txt");
    final String[] toisetnimet = new String[] {
            "Testi"
    };
    final String[] sukunimet = mkString("src/main/resources/sukunimet.txt");

    private final File output = new File("henkilot.json");

    private String[] mkString(final String file) {
        FileInputStream inputStream = null;
        String contents = "";
        try {
            inputStream = new FileInputStream(file);
            contents = IOUtils.toString(inputStream);
        } catch (final IOException e) {
            System.err.println(String.format("Error reading file %s", file));
            e.printStackTrace();
        } finally {
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    System.err.println(String.format("Error closing file %s", file));
                    e.printStackTrace();
                }
        }
        return contents.split("\n");
    }

    public static void main(final String[] args) throws Exception {
        final int maxAesKeySize = Cipher.getMaxAllowedKeyLength("AES");
        if (maxAesKeySize <= 128) {
            throw new RuntimeException("Max AES key size: " + maxAesKeySize + ". Oletko asentanut \"Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files\"?");
        }

        int generoitavienHetujenMaara = Integer.parseInt(args[0]),
                minBirthYear = Integer.parseInt(args[1]),
                maxBirthYear = Integer.parseInt(args[2]);

        String aesKey = args[3],
                aesSalt = args[4],
                shaSalt = args[5];

        final HenkiloGenerator henkiloGenerator = new HenkiloGenerator(aesKey, aesSalt, shaSalt);
        henkiloGenerator.generate(generoitavienHetujenMaara, minBirthYear, maxBirthYear);
    }

    public HenkiloGenerator(final String aesKey, final String aesSalt, final String shaSalt) throws Exception {
        aesEncrypter = new AESEncrypter(aesKey, aesSalt);
        sha2Encrypter = new SHA2Encrypter(shaSalt);
        FileUtils.deleteQuietly(output);
    }

    public void generate(final int generoitavienHetujenMaara, final int minBirthYear, final int maxBirthYear) throws Exception {
        System.out.println("Aloitus: " + new Date());
        System.out.println("Generoitavien henkilöiden määrä: " + generoitavienHetujenMaara);
        int generoidut = 0;
        while (generoidut < generoitavienHetujenMaara) {
            FileUtils.writeStringToFile(output, new RandomHenkilo(minBirthYear, maxBirthYear).toString() + "\n", true);
            generoidut++;
            if(generoidut % 50000 == 0) {
                int percentage = (generoidut * 100 / generoitavienHetujenMaara);
                System.out.println(String.format("Henkilöitä generoitu: %s (%s%%, maxRetry: %s, retryCount: %s)", generoidut, percentage, maxRetry, retryCount));
            }
        }
        System.out.println("Henkilöitä generoitiin yhteensä: " + generoidut);
        System.out.println("Lopetus: " + new Date());
    }

    String getRandomValue(final String[] arvot) {
        return arvot[new SecureRandom().nextInt(arvot.length)];
    }

    // Montako kertaa henkilölle on enimmillään jouduttu generoimaan hetu
    private int maxRetry = 0;
    // Montako kertaa henkilöitä luodessa hetu on jouduttu generoimaan uudelleen kosks se ei ole uniikki
    private int retryCount = 0;
    private Set<String> generatedHetus = new HashSet<>();
    private String generateUniqueHetu(final int minBirthYear, final int maxBirthYear) {
        int count = 0;
        boolean isUnique = false;
        String hetu = null;
        while(!isUnique) {
            hetu = HetuUtils.generateHetu(minBirthYear, maxBirthYear);
            if(count > 1) retryCount++;
            count++;
            if(count > maxRetry) maxRetry = count;
            if(!generatedHetus.contains(hetu)) {
                generatedHetus.add(hetu);
                isUnique = true;
            }
        }
        //if(count > 1) System.out.println("Count: " + count);
        return hetu;
    }

    class RandomHenkilo {
        String etunimi;
        String toinennimi;
        String sukunimi;

        String lahiosoite;
        String sahkopostiosoite;
        String puhelinnumero;

        String syntymaaika;
        String hetu;
        String hetu_aes;
        String hetu_sha;
        String sukupuoli;

        public RandomHenkilo(final int minBirthYear, final int maxBirthYear) throws Exception {
            this.toinennimi = getRandomValue(toisetnimet);
            this.sukunimi = String.format("%s-%s", getRandomValue(sukunimet), "Testi");

            this.lahiosoite = String.format("%s %d", getRandomValue(lahiosoitteet), new SecureRandom().nextInt(1000) + 1);
            this.sahkopostiosoite = String.format("hakija-%d@oph.fi", new SecureRandom().nextInt(2000000) * 31);
            this.puhelinnumero = String.format("050 %s", StringUtils.rightPad("" + new SecureRandom().nextInt(9999999) + 1, 7, '0'));

            final String hetu = generateUniqueHetu(minBirthYear, maxBirthYear);
            this.hetu = hetu;
            this.hetu_aes = aesEncrypter.encrypt(hetu);
            this.hetu_sha = sha2Encrypter.encrypt(hetu);
            if (hetu.charAt(6) == 'A') {
                this.syntymaaika = String.format("%s.%s.20%s", hetu.substring(0, 2), hetu.substring(2, 4), hetu.substring(4, 6));
            } else if (hetu.charAt(6) == '-') {
                this.syntymaaika = String.format("%s.%s.19%s", hetu.substring(0, 2), hetu.substring(2, 4), hetu.substring(4, 6));
            } else if (hetu.charAt(6) == '+') {
                this.syntymaaika = String.format("%s.%s.18%s", hetu.substring(0, 2), hetu.substring(2, 4), hetu.substring(4, 6));
            } else {
                throw new Exception("Ei järkevä hetu");
            }
            this.sukupuoli = CharUtils.toIntValue(hetu.charAt(9)) % 2 == 0 ? "2" : "1";
            this.etunimi = this.sukupuoli.equals("1") ? getRandomValue(etunimet_miehet) : getRandomValue(etunimet_naiset);
        }

        public String toString() {
            return String.format("{" +
                    "\"etunimi\": \"%s\", " +
                    "\"toinennimi\": \"%s\", " +
                    "\"sukunimi\": \"%s\", " +
                    "\"lahiosoite\": \"%s\", " +
                    "\"sahkopostiosoite\": \"%s\", " +
                    "\"puhelinnumero\": \"%s\", " +
                    "\"syntymaaika\": \"%s\", " +
                    "\"hetu\": \"%s\", " +
                    "\"hetu_aes\": \"%s\", " +
                    "\"hetu_sha\": \"%s\", " +
                    "\"sukupuoli\": \"%s\"}",
                    etunimi, toinennimi, sukunimi, lahiosoite, sahkopostiosoite, puhelinnumero, syntymaaika, hetu, hetu_aes, hetu_sha, sukupuoli);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final RandomHenkilo that = (RandomHenkilo) o;

            if (hetu != null ? !hetu.equals(that.hetu) : that.hetu != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return hetu != null ? hetu.hashCode() : 0;
        }
    }
}

