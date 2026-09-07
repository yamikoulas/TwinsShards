package com.twinsshards.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class NumberFormatter {

    private static List<String> suffixes = Arrays.asList(
            "", "K", "M", "B", "T", "Q", "Qi", "Sx", "Sp", "O", "N", "D"
    );
    private static int decimalPlaces = 2;
    private static DecimalFormat commaFormat = new DecimalFormat("#,##0.##", new DecimalFormatSymbols(Locale.US));

    public static void reload(List<String> newSuffixes, int newDecimalPlaces) {
        if (newSuffixes != null && !newSuffixes.isEmpty()) {
            suffixes = newSuffixes;
        }
        decimalPlaces = Math.max(0, newDecimalPlaces);
    }

    /**
     * Sayıyı K, M, B gibi kısaltılmış formata dönüştürür (örn. 1.5M, 250K).
     */
    public static String formatCompact(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (value < 1000) {
            return formatRaw(value);
        }

        int exp = (int) (Math.log10(value) / 3);
        if (exp >= suffixes.size()) {
            exp = suffixes.size() - 1;
        }

        double val = value / Math.pow(1000, exp);
        String pattern = "#." + "#".repeat(decimalPlaces);
        DecimalFormat df = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
        return df.format(val) + suffixes.get(exp);
    }

    /**
     * Sayıyı basamaklarına ayırarak virgüllü formatlar (örn. 1,500,000).
     */
    public static String formatCommas(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        return commaFormat.format(value);
    }

    /**
     * Tam sayı ise ondalıksız, ondalıklı ise normal haliyle döndürür.
     */
    public static String formatRaw(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "0";
        }
        if (value == (long) value) {
            return String.format(Locale.US, "%d", (long) value);
        } else {
            return String.format(Locale.US, "%." + decimalPlaces + "f", value);
        }
    }
}
