package connectx.cadregaBot;

import connectx.CXCell;
import connectx.CXCellState;

import static connectx.cadregaBot.CadregaBot.OUR_VICTORY;

/**
 * Utility class to compute the heuristic.
 */
public final class EvaluateUtil {

    private final int M, N, K;
    private final CXCellState[][] tmpBoard;

    /**
     * Creates a new {@code EvaluateUtil}.
     *
     * @param M The M value of (M, N, K).
     * @param N The N value of (M, N, K).
     * @param K The K value of (M, N, K).
     * @param tmpBoard The tmpBoard.
     */
    public EvaluateUtil(int M, int N, int K, CXCellState[][] tmpBoard) {
        this.M = M;
        this.N = N;
        this.K = K;
        this.tmpBoard = tmpBoard;
    }

    /**
     * Evaluates the provided cell with the heuristic.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell. It is {@link CadregaBot#OUR_VICTORY OUR_VICTORY} if this move makes the provided player wins.
     */
    public int evaluate(CXCell cell, CXCellState player) {
        int val = 0;
        boolean diagonals = true;

        if (M >= K) {
            // There's enough space horizontally
            int res = evaluateVertical(cell, player);
            if (res == OUR_VICTORY) {
                return OUR_VICTORY;
            }
            val += res;
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (N >= K) {
            // There's enough space vertically
            int res = evaluateHorizontal(cell, player);
            if (res == OUR_VICTORY) {
                return OUR_VICTORY;
            }
            val += res;
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (diagonals) {
            int res = evaluateMainDiagonal(cell, player);
            if (res == OUR_VICTORY) {
                return OUR_VICTORY;
            }
            val += res;
            res = evaluateInvertedDiagonal(cell, player);
            if (res == OUR_VICTORY) {
                return OUR_VICTORY;
            }
            val += res;
        }
        return val;
    }

    /**
     * Returns the heuristic evaluation of the provided cell looking only at its column.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its column. It is
     * {@link CadregaBot#OUR_VICTORY OUR_VICTORY} if this move makes the provided player wins.
     */
    private int evaluateVertical(CXCell cell, CXCellState player) {
        int first = cell.i, last = cell.i, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, M - 1);
        boolean notFound = true;
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[first - 1][cell.j];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            first--;
        }
        notFound = true;
        while (last < maxLast) {
            CXCellState b = tmpBoard[last + 1][cell.j];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            last++;
        }

        if (counter >= K) {
            // The player can win in just one move
            return OUR_VICTORY;
        }

        // Calculating the points of this configuration
        int n = last - first + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; first < cell.i; first++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[first][cell.j] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; last > cell.i; last--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[last][cell.j] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Returns the heuristic evaluation of the provided cell looking only at its row.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its row. It is
     * {@link CadregaBot#OUR_VICTORY OUR_VICTORY} if this move makes the provided player wins.
     */
    private int evaluateHorizontal(CXCell cell, CXCellState player) {
        int first = cell.j, last = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, N - 1);
        boolean notFound = true;
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[cell.i][first - 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            first--;
        }
        notFound = true;
        while (last < maxLast) {
            CXCellState b = tmpBoard[cell.i][last + 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            last++;
        }

        if (counter >= K) {
            // The player can win in just one move
            return OUR_VICTORY;
        }

        // Calculating the points of this configuration
        int n = last - first + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; first < cell.j; first++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[cell.i][first] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; last > cell.j; last--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[cell.i][last] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Returns the heuristic evaluation of the provided cell looking only at its main diagonal.
     * The main diagonal goes from top left to bottom right.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its main diagonal. It is
     * {@link CadregaBot#OUR_VICTORY OUR_VICTORY} if this move makes the provided player wins.
     */
    private int evaluateMainDiagonal(CXCell cell, CXCellState player) {
        // The main diagonal goes from top left to bottom right (\)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirstI = Math.max(firstI - K + 1, 0), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.min(lastI + K - 1, M - 1), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        boolean notFound = true;
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (firstI > minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI - 1][firstJ - 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            firstI--;
            firstJ--;
        }
        notFound = true;
        while (lastI < maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI + 1][lastJ + 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            lastI++;
            lastJ++;
        }

        if (counter >= K) {
            // The player can win in just one move
            return OUR_VICTORY;
        }

        // Calculating the points of this configuration
        int n = lastI - firstI + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; firstI < cell.i; firstI++, firstJ++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[firstI][firstJ] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; lastI > cell.i; lastI--, lastJ--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[lastI][lastJ] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Returns the heuristic evaluation of the provided cell looking only at its inverted diagonal.
     * The inverted diagonal goes from bottom left to top right.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its inverted diagonal. It is
     * {@link CadregaBot#OUR_VICTORY OUR_VICTORY} if this move makes the provided player wins.
     */
    private int evaluateInvertedDiagonal(CXCell cell, CXCellState player) {
        // The inverted diagonal goes from bottom left to top right (/)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirstI = Math.min(firstI + K - 1, M - 1), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.max(lastI - K + 1, 0), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        boolean notFound = true;
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (firstI < minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI + 1][firstJ - 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            firstI++;
            firstJ--;
        }
        notFound = true;
        while (lastI > maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI - 1][lastJ + 1];
            if (b == player) {
                if (notFound) {
                    counter++;
                }
            } else if (b == CXCellState.FREE) {
                notFound = false;
            } else {
                break;
            }
            lastI--;
            lastJ++;
        }

        if (counter >= K) {
            // The player can win in just one move
            return OUR_VICTORY;
        }

        // Calculating the points of this configuration
        int n = firstI - lastI + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; firstI > cell.i; firstI--, firstJ++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[firstI][firstJ] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; lastI < cell.i; lastI++, lastJ--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[lastI][lastJ] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Returns whether with the provided move the player wins.
     *
     * @param cell The move.
     * @param player The player which makes the move.
     * @return Whether the player wins with the provided move.
     */
    public boolean isWinningCell(CXCell cell, CXCellState player) {
        boolean diagonals = true;

        if (M >= K) {
            // There's enough space horizontally
            if (isWinningVertical(cell, player)) {
                return true;
            }
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (N >= K) {
            // There's enough space vertically
            if (isWinningHorizontal(cell, player)) {
                return true;
            }
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (diagonals) {
            if (isWinningMainDiagonal(cell, player)) {
                return true;
            }
            return isWinningInvertedDiagonal(cell, player);
        }
        return false;
    }

    /**
     * Returns whether with the provided move the player wins looking only at its column.
     *
     * @param cell The move.
     * @param player The player which makes the move.
     * @return Whether the player wins with the provided move looking only at its column.
     */
    private boolean isWinningVertical(CXCell cell, CXCellState player) {
        int first = cell.i, last = cell.i, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, M - 1);
        // Checking if there is a contiguous sequence of player's cells (containing this cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[first - 1][cell.j];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            first--;
        }
        while (last < maxLast) {
            CXCellState b = tmpBoard[last + 1][cell.j];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            last++;
        }
        return counter >= K;
    }

    /**
     * Returns whether with the provided move the player wins looking only at its row.
     *
     * @param cell The move.
     * @param player The player which makes the move.
     * @return Whether the player wins with the provided move looking only at its row.
     */
    private boolean isWinningHorizontal(CXCell cell, CXCellState player) {
        int first = cell.j, last = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, N - 1);
        // Checking if there is a contiguous sequence of player's cells (containing this cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[cell.i][first - 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            first--;
        }
        while (last < maxLast) {
            CXCellState b = tmpBoard[cell.i][last + 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            last++;
        }

        return counter >= K;
    }

    /**
     * Returns whether with the provided move the player wins looking only at its main diagonal.
     * The main diagonal goes from top left to bottom right.
     *
     * @param cell The move.
     * @param player The player which makes the move.
     * @return Whether the player wins with the provided move looking only at its main diagonal.
     */
    private boolean isWinningMainDiagonal(CXCell cell, CXCellState player) {
        // The main diagonal goes from top left to bottom right (\)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirstI = Math.max(firstI - K + 1, 0), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.min(lastI + K - 1, M - 1), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        // Checking if there is a contiguous sequence of player's cells (containing this cell)
        while (firstI > minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI - 1][firstJ - 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            firstI--;
            firstJ--;
        }
        while (lastI < maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI + 1][lastJ + 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            lastI++;
            lastJ++;
        }

        return counter >= K;
    }

    /**
     * Returns whether with the provided move the player wins looking only at its inverted diagonal.
     * The inverted diagonal goes from bottom left to top right.
     *
     * @param cell The move.
     * @param player The player which makes the move.
     * @return Whether the player wins with the provided move looking only at its inverted diagonal.
     */
    private boolean isWinningInvertedDiagonal(CXCell cell, CXCellState player) {
        // The inverted diagonal goes from bottom left to top right (/)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j, counter = 1; // counter is initialized at 1 since we're already counting the player move
        int minFirstI = Math.min(firstI + K - 1, M - 1), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.max(lastI - K + 1, 0), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        // Checking if there is a contiguous sequence of player's cells (containing this cell)
        while (firstI < minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI + 1][firstJ - 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            firstI++;
            firstJ--;
        }
        while (lastI > maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI - 1][lastJ + 1];
            if (b == player) {
                counter++;
            } else {
                break;
            }
            lastI--;
            lastJ++;
        }

        return counter >= K;
    }

    /**
     * Evaluates the provided cell with the heuristic, but don't calculate if moving in the provided cell makes the player wins.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell.
     */
    public int simpleEvaluate(CXCell cell, CXCellState player) {
        int val = 0;
        boolean diagonals = true;

        if (M >= K) {
            // There's enough space horizontally
            val += simpleEvaluateVertical(cell, player);
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (N >= K) {
            // There's enough space vertically
            val += simpleEvaluateHorizontal(cell, player);
        } else {
            // We are sure there is no space for the diagonal
            diagonals = false;
        }

        if (diagonals) {
            val += simpleEvaluateMainDiagonal(cell, player);
            val += simpleEvaluateInvertedDiagonal(cell, player);
        }
        return val;
    }

    /**
     * Evaluates the provided cell with the heuristic looking only at its column, but doesn't
     * calculate if moving in the provided cell makes the player wins.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its column.
     */
    private int simpleEvaluateVertical(CXCell cell, CXCellState player) {
        int first = cell.i, last = cell.i;
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, M - 1);
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[first - 1][cell.j];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            first--;
        }
        while (last < maxLast) {
            CXCellState b = tmpBoard[last + 1][cell.j];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            last++;
        }

        // Calculating the points of this configuration
        int n = last - first + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; first < cell.i; first++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[first][cell.j] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; last > cell.i; last--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[last][cell.j] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Evaluates the provided cell with the heuristic looking only at its row, but doesn't
     * calculate if moving in the provided cell makes the player wins.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its row.
     */
    private int simpleEvaluateHorizontal(CXCell cell, CXCellState player) {
        int first = cell.j, last = cell.j;
        int minFirst = Math.max(first - K + 1, 0), maxLast = Math.min(last + K - 1, N - 1);
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (first > minFirst) {
            CXCellState b = tmpBoard[cell.i][first - 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            first--;
        }
        while (last < maxLast) {
            CXCellState b = tmpBoard[cell.i][last + 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            last++;
        }

        // Calculating the points of this configuration
        int n = last - first + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; first < cell.j; first++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[cell.i][first] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; last > cell.j; last--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[cell.i][last] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Evaluates the provided cell with the heuristic looking only at its main diagonal, but doesn't
     * calculate if moving in the provided cell makes the player wins.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its main diagonal.
     */
    private int simpleEvaluateMainDiagonal(CXCell cell, CXCellState player) {
        // The main diagonal goes from top left to bottom right (\)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j;
        int minFirstI = Math.max(firstI - K + 1, 0), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.min(lastI + K - 1, M - 1), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (firstI > minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI - 1][firstJ - 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            firstI--;
            firstJ--;
        }
        while (lastI < maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI + 1][lastJ + 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            lastI++;
            lastJ++;
        }

        // Calculating the points of this configuration
        int n = lastI - firstI + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; firstI < cell.i; firstI++, firstJ++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[firstI][firstJ] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; lastI > cell.i; lastI--, lastJ--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[lastI][lastJ] == player) {
                eval += max;
            }
        }

        return eval;
    }

    /**
     * Evaluates the provided cell with the heuristic looking only at its inverted diagonal, but doesn't
     * calculate if moving in the provided cell makes the player wins.
     *
     * @param cell The cell to evaluate.
     * @param player The player who makes the move.
     * @return The heuristic value of the provided cell looking only at its inverted diagonal.
     */
    private int simpleEvaluateInvertedDiagonal(CXCell cell, CXCellState player) {
        // The inverted diagonal goes from bottom left to top right (/)

        int firstI = cell.i, firstJ = cell.j, lastI = cell.i, lastJ = cell.j;
        int minFirstI = Math.min(firstI + K - 1, M - 1), minFirstJ = Math.max(firstJ - K + 1, 0), maxLastI = Math.max(lastI - K + 1, 0), maxLastJ = Math.min(lastJ + K - 1, N - 1);
        // Search the longest contiguous sequence of free or player's cells (containing the provided cell)
        while (firstI < minFirstI && firstJ > minFirstJ) {
            CXCellState b = tmpBoard[firstI + 1][firstJ - 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            firstI++;
            firstJ--;
        }
        while (lastI > maxLastI && lastJ < maxLastJ) {
            CXCellState b = tmpBoard[lastI - 1][lastJ + 1];
            if (!(b == player || b == CXCellState.FREE)) {
                break;
            }
            lastI--;
            lastJ++;
        }

        // Calculating the points of this configuration
        int n = firstI - lastI + 1;
        if (n < K) {
            return 0;
        }
        int maxN = n - K + 1, max = 0, eval = maxN;
        for (; firstI > cell.i; firstI--, firstJ++) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[firstI][firstJ] == player) {
                eval += max;
            }
        }
        max = 0;
        for (; lastI < cell.i; lastI++, lastJ--) {
            if (max < maxN) {
                max++;
            }
            if (tmpBoard[lastI][lastJ] == player) {
                eval += max;
            }
        }

        return eval;
    }
}
