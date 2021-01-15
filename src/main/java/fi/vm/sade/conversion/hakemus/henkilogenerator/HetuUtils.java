package fi.vm.sade.conversion.hakemus.henkilogenerator;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class HetuUtils {
    private static final String CHECKSUM_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";
    private static volatile Map<Integer, Character> separators = new HashMap<Integer, Character>();
    private static volatile Map<Character, Integer> invertedSeparators = new HashMap<Character, Integer>();

    static {
        separators.put(18, '+');
        separators.put(19, '-');
        separators.put(20, 'A');
        separators.put(21, 'B');

        @SuppressWarnings("unchecked")
        final Map<Character, Integer> inverted = MapUtils.invertMap(separators);
        invertedSeparators = inverted;
    }

    private HetuUtils() {
    }

    public static String generateHetu() {
        final Calendar now = Calendar.getInstance();
        final int currentYear = now.get(Calendar.YEAR);
        final int startYear = 1920;
        final Random rand = new Random();
        final int day = rand.nextInt(28) + 1;
        final int month = rand.nextInt(12) + 1;
        final int year = startYear + rand.nextInt(currentYear - startYear);
        final int gender = rand.nextInt(2); // 0 = female, 1 = male
        return generateHetuWithArgs(day, month, year, gender);
    }

    public static String generateHetu(final int[] possibleBirthYears) {
        final Calendar now = Calendar.getInstance();
        final int currentYear = now.get(Calendar.YEAR);
        final Random rand = new Random();
        final int day = rand.nextInt(28) + 1;
        final int month = rand.nextInt(12) + 1;
        final int year = possibleBirthYears[rand.nextInt(possibleBirthYears.length)];
        final int gender = rand.nextInt(2); // 0 = female, 1 = male
        return generateHetuWithArgs(day, month, year, gender);
    }

    private  static String generateHetuWithArgs(final int day, final int month, final int year, final int gender) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            sdf.parse(String.format("%s%s%s",
                    StringUtils.leftPad(String.valueOf(day), 2, '0'),
                    StringUtils.leftPad(String.valueOf(month), 2, '0'),
                    String.valueOf(year)));
        } catch (final Exception e) {
            throw new IllegalArgumentException("error parsing birthday", e);
        }

        if (year < 1800 || year > 2199) {
            throw new IllegalArgumentException("year is invalid");
        }

        if (gender < 0 || gender > 1) {
            throw new IllegalArgumentException("gender is invalid; should be 0 (female) or 1 (male)");
        }

        final Random rand = new Random();
        int identifier = rand.nextInt(99) + 900;
        if ((gender == 0 && identifier % 2 != 0) || (gender == 1 && identifier % 2 == 0)) {
            identifier++;
        }
        final String partialHetu = String.format("%s%s%s%c%s",
                StringUtils.leftPad(String.valueOf(day), 2, '0'),
                StringUtils.leftPad(String.valueOf(month), 2, '0'),
                StringUtils.substring(String.valueOf(year), 2),
                separators.get(Integer.parseInt(StringUtils.substring(String.valueOf(year), 0, 2))),
                StringUtils.leftPad(String.valueOf(identifier), 3, '0'));
        final char checksumCharacter = getChecksumCharacter(partialHetu);

        return String.format("%s%c", partialHetu, checksumCharacter);
    }

    private static Character getChecksumCharacter(final String partialHetu) {
        final long checkNumber = Long.parseLong(String.format("%s%s", StringUtils.substring(partialHetu, 0, 6),
                StringUtils.substring(partialHetu, 7, 10)));
        return CHECKSUM_CHARACTERS.charAt((int)(checkNumber % CHECKSUM_CHARACTERS.length()));
    }
}
