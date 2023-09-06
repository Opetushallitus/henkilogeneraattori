package fi.vm.sade.conversion.hakemus.henkilogenerator;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class HetuUtils {
    private static final String CHECKSUM_CHARACTERS = "0123456789ABCDEFHJKLMNPRSTUVWXY";
    public static volatile Map<Integer, List<Character>> separators = new HashMap<Integer, List<Character>>();

    static {
        separators.put(18, Arrays.asList('-'));
        separators.put(19, Arrays.asList('Y', 'X', 'W', 'V', 'U'));
        separators.put(20, Arrays.asList('B', 'C', 'D', 'E', 'F'));
    }

    private HetuUtils() {
    }

    public static String generateHetu(final int minBirthYear, final int maxBirthYear) {
        final Calendar now = Calendar.getInstance();
        final int currentYear = now.get(Calendar.YEAR);
        final Random rand = new Random();
        final int day = rand.nextInt(28) + 1;
        final int month = rand.nextInt(12) + 1;
        final int year = minBirthYear + rand.nextInt(maxBirthYear - minBirthYear + 1);
        final List<Character> separatorsAvailable = separators.get(Integer.parseInt(StringUtils.substring(String.valueOf(year), 0, 2)));
        final Character separator = separatorsAvailable.get(rand.nextInt(separatorsAvailable.size()));
        final int gender = rand.nextInt(2); // 0 = female, 1 = male
        return generateHetuWithArgs(day, month, year, gender, separator);
    }

    private  static String generateHetuWithArgs(final int day, final int month, final int year, final int gender, final Character separator) {
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
                separator,
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
