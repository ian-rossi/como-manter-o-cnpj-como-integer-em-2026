import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Testing {

    private static final int CNPJ_SIZE = 14;
    private static final Pattern LETTERS_ONLY_PATTERN = Pattern.compile("([a-zA-Z]+)");
    private static final int TEN = 10;
    private static final int TEN_VALUE_ZERO_QUANTITY = 1;

    public static final void main(String[] args) {
        // Vamos contar que você tirou a máscara, validou o tamanho e padrão somente com número e letras, validou dígito verificador e as caralha td
        var cnpjStr = "13P85aZ200f150";
        var cnpjNumberAndMetadata = fromStringToNumberAndMetadata(cnpjStr);
        var cnpjStrConverted = fromNumberAndMetadataToString(cnpjNumberAndMetadata);
        System.out.println(cnpjStr.equals(cnpjStrConverted));
    }

    static Entry<Long, Map<Character, List<Integer>>> fromStringToNumberAndMetadata(String cnpjStr) {
        var cnpjNumber = 0L;
        var zerosQuantitiesByLetter = new HashMap<Character, List<Integer>>();
        var cnpjLettersMatcher = LETTERS_ONLY_PATTERN.matcher(cnpjStr);
        var previousMatcherEndIndex = 0;
        while (cnpjLettersMatcher.find()) {
            var cnpjLettersMatcherStartIndex = cnpjLettersMatcher.start();
            var numbersBeforeStartIndexAndAfterPreviousEndIndex =
                cnpjStr.substring(previousMatcherEndIndex, cnpjLettersMatcherStartIndex);
            if (!numbersBeforeStartIndexAndAfterPreviousEndIndex.isBlank()) {
                var numbersBeforeStartIndexAndAfterPreviousEndIndexParsed =
                    Long.valueOf(numbersBeforeStartIndexAndAfterPreviousEndIndex);
                var parsedNumberZerosQuantity = cnpjStr.length() - previousMatcherEndIndex -
                    numbersBeforeStartIndexAndAfterPreviousEndIndex.length();
                var tenElevatedByParsedNumberZerosQuantity = Math.pow(TEN, parsedNumberZerosQuantity);
                var parsedNumberConverted = numbersBeforeStartIndexAndAfterPreviousEndIndexParsed *
                    tenElevatedByParsedNumberZerosQuantity;
                cnpjNumber += parsedNumberConverted;
            }
            var cnpjLettersMatcherEndIndex = cnpjLettersMatcher.end();
            for (int i = cnpjLettersMatcherStartIndex; i < cnpjLettersMatcherEndIndex; i++) {
                var cnpjLetter = cnpjStr.charAt(i);
                var cnpjLetterZerosQuantity = cnpjStr.length() - i - TEN_VALUE_ZERO_QUANTITY;
                var zeroQuantities = zerosQuantitiesByLetter.get(cnpjLetter);
                if (zeroQuantities != null) {
                    zeroQuantities.add(cnpjLetterZerosQuantity);
                } else {
                    var newZeroQuantities = new LinkedList<Integer>();
                    newZeroQuantities.add(cnpjLetterZerosQuantity);
                    zerosQuantitiesByLetter.put(cnpjLetter, newZeroQuantities);
                }
                var tenElevatedByCNPJLetterZerosQuantity = Math.pow(TEN, cnpjLetterZerosQuantity);
                var asciiCNPJLetter = (int) cnpjLetter;
                var cnpjLetterConverted = asciiCNPJLetter * tenElevatedByCNPJLetterZerosQuantity;
                cnpjNumber += cnpjLetterConverted;
            }
            previousMatcherEndIndex = cnpjLettersMatcherEndIndex;
        }
        if (zerosQuantitiesByLetter.isEmpty()) {
            cnpjNumber = Long.valueOf(cnpjStr);
        }
        if (previousMatcherEndIndex != 0 && previousMatcherEndIndex < cnpjStr.length()) {
            var numbersBeyoundPreviousEndIndex = cnpjStr.substring(previousMatcherEndIndex);
            var numbersBeyoundPreviousEndIndexParsed = Long.valueOf(numbersBeyoundPreviousEndIndex);
            cnpjNumber += numbersBeyoundPreviousEndIndexParsed;
        }
        return Map.entry(cnpjNumber, zerosQuantitiesByLetter);
    }

    static String fromNumberAndMetadataToString(Entry<Long, Map<Character, List<Integer>>> cnpjNumberAndZeroQuantitiesByLetter) {
        var cnpjNumber = cnpjNumberAndZeroQuantitiesByLetter.getKey();
        var zerosQuantitiesByLetter = cnpjNumberAndZeroQuantitiesByLetter.getValue();
        if (zerosQuantitiesByLetter.isEmpty()) {
            return completeNumberWithZerosBehind(cnpjNumber);
        }
        var cnpjNumberClone = cnpjNumber.longValue();
        var cnpjLetters = new char[CNPJ_SIZE];
        for (var entry : zerosQuantitiesByLetter.entrySet()) {
            var letter = entry.getKey();
            var asciiLetter = (int) letter;
            var zeroQuantities = entry.getValue();
            for (var zeroQuantity : zeroQuantities) {
                var letterIndex = CNPJ_SIZE - (zeroQuantity + TEN_VALUE_ZERO_QUANTITY);
                cnpjLetters[letterIndex] = letter;
                var tenElevatedByZeroQuantity = Math.pow(TEN, zeroQuantity);
                var cnpjLetterConverted = asciiLetter * tenElevatedByZeroQuantity;
                cnpjNumberClone -= cnpjLetterConverted;
            }
        }
        var cnpjSizeLastIndex = CNPJ_SIZE - 1;
        for (int i = 0; i < cnpjSizeLastIndex; i++) {
            var zeroQuantity = cnpjSizeLastIndex - i;
            var tenElevatedByZeroQuantity = Math.pow(TEN, zeroQuantity);
            var numberConverted = new BigDecimal(cnpjNumberClone)
                .divide(new BigDecimal(tenElevatedByZeroQuantity))
                .setScale(0, RoundingMode.DOWN)
                .intValue();
            if (cnpjLetters[i] == (char) 0) {
                cnpjLetters[i] = Character.forDigit(numberConverted, TEN);
            }
            cnpjNumberClone -= numberConverted * tenElevatedByZeroQuantity;
        }
        if (cnpjNumberClone <  0 || cnpjNumberClone > 9) {
            throw new IllegalStateException("Fórmula incorreta, deveria ter sobrado somente um dígito no número do CNPJ.");
        }
        cnpjLetters[cnpjSizeLastIndex] = Character.forDigit((int) cnpjNumberClone, TEN);
        return new String(cnpjLetters);
    }

    static String completeNumberWithZerosBehind(Long cnpjNumber) {
        var cnpjNumberStr = cnpjNumber.toString();
        return "0".repeat(CNPJ_SIZE - cnpjNumberStr.length()) + cnpjNumberStr;
    }
}
