package mnkgame.cadregaBot;

import mnkgame.MNKCellState;

import java.util.Arrays;

/**
 * Debug utility class.
 */
public final class DebugUtil {

    /**
     * Utility which prints the current state of the table to the standard output.
     *
     * @param board The board to be printed.
     * @param cells The cells of the board evaluated by the heuristic.
     */
    public static void printTable(MNKCellState[][] board, EvaluatedCell[] cells) {
        int M = board.length;
        int N = board[0].length;

        class Printable {
            final int value;
            final MNKCellState state;

            public Printable(EvaluatedCell cell) {
                this.value = cell.getValue();
                this.state = MNKCellState.FREE;
            }

            public Printable(MNKCellState state) {
                this.value = -1;
                this.state = state;
            }

            @Override
            public String toString() {
                switch (state) {
                    case FREE:
                        if (value >= 0) {
                            return Integer.toString(value);
                        } else {
                            return " ";
                        }
                    case P1:
                        return "X";
                    case P2:
                        return "O";
                    default:
                        throw new RuntimeException("Hey! How did you get here?!");
                }
            }
        }

        Printable[][] printable = new Printable[M][N];

        for (EvaluatedCell cell : cells) {
            printable[cell.getCell().i][cell.getCell().j] = new Printable(cell);
        }
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (printable[i][j] == null) {
                    printable[i][j] = new Printable(board[i][j]);
                }
            }
        }
        int length = 4 * N + 1;
        char[] c = new char[length];
        Arrays.fill(c, '-');
        for (int i = 0; i < length; i += 4) {
            c[i] = '+';
        }
        String lines = new String(c);

        Arrays.fill(c, ' ');
        for (int i = 0; i < length; i += 4) {
            c[i] = '|';
        }
        for (int i = 0; i < M; i++) {
            System.out.println(lines);

            char[] arr = new char[length];
            System.arraycopy(c, 0, arr, 0, length);

            for (int j = 0, n = 1; j < N; j++, n += 4) {
                String s = printable[i][j].toString();
                if (s.length() == 1) {
                    arr[n + 1] = s.charAt(0);
                } else if (s.length() == 2) {
                    arr[n + 1] = s.charAt(0);
                    arr[n + 2] = s.charAt(1);
                } else {
                    System.arraycopy(s.toCharArray(), 0, arr, n, 3);
                }
            }

            System.out.println(arr);
        }
        System.out.println(lines);
    }

    // Private constructor
    private DebugUtil() {
        throw new UnsupportedOperationException();
    }
}
