package connectx.cadregaBot;

import java.util.LinkedList;

/**
 * Utility to sort arrays of {@link EvaluateUtil}s.
 */
public final class SortUtil {

    /**
     * Orders the array of {@link EvaluatedCell}s in base of the provided nth digit (in base 16) of the cell's value.
     *
     * @param cells The array to order.
     * @param digit The nth digit (in base 16) of the cell's value to use to order.
     */
    public static void bucketSort(EvaluatedCell[] cells, int digit) {
        LinkedList<EvaluatedCell>[] bucketArray = new LinkedList[16]; // Array for bucket sort. It has length 16 since we're using hexadecimal

        // Put the cells into the right bucket
        for (EvaluatedCell evaluatedCell : cells) {
            int cell = getDigit(evaluatedCell.getValue(), digit);
            if (bucketArray[cell] == null) {
                bucketArray[cell] = new LinkedList<>();
            }
            bucketArray[cell].add(evaluatedCell);
        }

        // Put cells back into the array to sort
        int evaluatedCellInsertionIndex = 0;
        for (int i = bucketArray.length - 1; i >= 0; i--) {
            if (bucketArray[i] != null) {
                for (EvaluatedCell j : bucketArray[i]) {
                    cells[evaluatedCellInsertionIndex] = j;
                    evaluatedCellInsertionIndex++;
                }
            }
        }
    }

    /**
     * Orders the provided array of {@link EvaluatedCell}s using Radix Sort algorithm.
     *
     * @param cells The array to order.
     */
    public static void radixSort(EvaluatedCell[] cells) {
        // Simple cases
        if (cells.length <= 1) {
            return;
        }
        if (cells.length == 2) {
            if (cells[0].getValue() > cells[1].getValue()) {
                EvaluatedCell tmp = cells[0];
                cells[0] = cells[1];
                cells[1] = tmp;
                return;
            }
        }

        // Find the maximum length of the cells' value in hexadecimal
        int maxLength = 0;
        for (EvaluatedCell cell : cells) {
            int thisLength = digitCount(cell.getValue());
            if (thisLength < 0) {
                throw new IllegalArgumentException("EvaluatedCell's value is less than zero");
            }
            if (thisLength > maxLength) {
                maxLength = thisLength;
            }
        }

        // Sort using bucket sort
        for (int j = 0; j < maxLength; j++) {
            bucketSort(cells, j);
        }
    }

    /**
     * Returns the nth hexadecimal digit of num.
     *
     * @param num The number. Must be positive.
     * @param digit The digit to return.
     * @return The nth hexadecimal digit of num.
     */
    private static int getDigit(int num, int digit) {
        // this function returns the nth hexadecimal digit of num

        // In base 16 a digit equals to 4 binary digits.
        // So, the parameter digit is firstly multiplied by 4.
        // Then, the parameter num is shifted by that amount and the first 4 digits are returned.
        return (num >>> (digit << 2)) & 0xF;
    }

    /**
     * Returns the number of digits of n in hexadecimal
     *
     * @param n The number. Must be positive.
     * @return The number of digits of n in hexadecimal
     */
    private static int digitCount(int n) {
        // Adapted from https://stackoverflow.com/a/1308407

        // For ints and base 16, the maximum number of digits is 8
        // (Integer.MAX_VALUE is 0x7FFFFFFF, which is 8 digits long)
        if (n < 0x10000) {
            // 4 or less
            if (n < 0x100) {
                // 1 or 2
                if (n < 0x10)
                    return 1;
                else
                    return 2;
            } else {
                // 3 or 4
                if (n < 0x1000)
                    return 3;
                else {
                    return 4;
                }
            }
        } else {
            // 5 or more
            if (n < 0x1000000) {
                // 5 or 6
                if (n < 0x100000)
                    return 5;
                else
                    return 6;
            } else {
                // 7 or 8
                if (n < 0x10000000)
                    return 7;
                else {
                    return 8;
                }
            }
        }
    }

    // Private constructor
    private SortUtil() {
        throw new UnsupportedOperationException();
    }
}
