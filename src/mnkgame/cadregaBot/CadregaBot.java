package mnkgame.cadregaBot;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKPlayer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Questo qua mi convince proprio poco poco poco...
 * <br>
 * [...]
 * <br>
 * Facciamogli l'inganno della cadrega!
 */
public final class CadregaBot implements MNKPlayer {
    // Here and there there are commented lines of code that are useful for debugging

    public static final int OUR_VICTORY = Integer.MAX_VALUE - 1, OPPONENT_VICTORY = Integer.MAX_VALUE - 2;
    private static final int DEFAULT_DEPTH = 6;
    private int depth = DEFAULT_DEPTH;

    private int M, N, K;

    // Variables used to keep track of selectCell's execution time
    private long timeout, startTime, oldExecutionTime = -1;

    private MNKCellState[][] board, tmpBoard; // board reflects the actual board state, tmpBoard is used for computations
    private MNKCellState our, opponent;
    private EvaluateUtil evaluateUtil;
    private Node root, bestMove; // root is the first node analyzed of the tree, bestMove is the best move found yet

    // Variables used to calculate the visit depth
    private long nodeCounter, nodesAverage;
    private boolean alphabetaStarted;

    /**
     * Empty constructor
     */
    public CadregaBot() {
    }

    /**
     * Initialize the (M,N,K) Player
     *
     * @param M Board rows
     * @param N Board columns
     * @param K Number of symbols to be aligned (horizontally, vertically, diagonally) for a win
     * @param first True if it is the first player, False otherwise
     * @param timeout_in_secs Maximum amount of time (in seconds) for selectCell
     */
    @Override
    public void initPlayer(int M, int N, int K, boolean first, int timeout_in_secs) {
        this.M = M;
        this.N = N;
        this.K = K;
        this.timeout = (timeout_in_secs * 1000L) - 1000L; // Keeping a margin of a second for the initialization

        // Reset fields
        this.depth = DEFAULT_DEPTH;
        this.startTime = 0;
        this.oldExecutionTime = -1;
        this.root = null;
        this.bestMove = null;
        this.nodeCounter = 0;
        this.nodesAverage = 0;
        this.alphabetaStarted = false;

        // Create the boards and initialize them
        this.board = new MNKCellState[M][N];
        this.tmpBoard = new MNKCellState[M][N];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                board[i][j] = MNKCellState.FREE;
            }
        }

        // Run selectCell on a dummy board to collect data in order to be able to calculate nodesAverage on our first (real) move

        // We need FC and MC to call selectCell
        MNKCell[] FC, MC;

        if (first) {
            our = MNKCellState.P1;
            opponent = MNKCellState.P2;

            FC = new MNKCell[M * N];
            MC = new MNKCell[0];
        } else {
            our = MNKCellState.P2;
            opponent = MNKCellState.P1;

            // We're the second to play, just place a dummy move
            this.board[M / 2][N / 2] = opponent;

            FC = new MNKCell[M * N - 1];
            MC = new MNKCell[1];
        }

        // Fill FC and MC
        int n = 0, k = 0;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (this.board[i][j] == MNKCellState.FREE) { // Always true if we're the first moving
                    FC[n++] = new MNKCell(i, j, MNKCellState.FREE);
                } else {
                    MC[k++] = new MNKCell(i, j, this.board[i][j]);
                }
            }
        }

        // Make sure tmpBoard is the same as board
        // This is not really needed, since it will be done by selectCell, that's just to be sure there aren't nulls in tmpBoard at the start of selectCell
        copyTmpBoard();

        this.evaluateUtil = new EvaluateUtil(M, N, K, tmpBoard);

        try {
            if (FC.length > 0) { // Don't execute selectCell with zero free cells (this happens on (1, 1, 1) games when we are the second player)
                selectCell(FC, MC);
            }
        } catch (Exception ignored) {
            // System.out.println("TIMEOUT");
        } finally {
            // Reset board, tmpBoard, root, bestMove and depth since we ran on a dummy board
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    board[i][j] = MNKCellState.FREE;
                }
            }

            copyTmpBoard();

            this.root = null;
            this.bestMove = null;
            this.depth = DEFAULT_DEPTH;

            // For the "real" selectCell we want a margin of only half a second
            this.timeout = (timeout_in_secs * 1000L) - 500L;
        }
    }

    /**
     * Select a position among those listed in the <code>FC</code> array
     *
     * @param FC Free Cells: array of free cells
     * @param MC Marked Cells: array of already marked cells, ordered with respect to the game moves (first move is in the first position, etc)
     * @return an element of <code>FC</code>
     */
    @Override
    public MNKCell selectCell(MNKCell[] FC, MNKCell[] MC) {
        startTime = System.currentTimeMillis();
        // System.out.println("Loading...");

        // Calculates the visit depth
        if (nodeCounter == 0) {
            // Corner case (which should happen only the first time selectCell is run in initPlayer)
            depth = DEFAULT_DEPTH;
            // System.out.println("Default depth: " + depth);
        } else {
            if (oldExecutionTime < timeout) { // Adjust the nodeCounter
                if (oldExecutionTime < 0) { // Invalid oldExecutionTime, just assume we can analyze the full tree
                    nodeCounter = Long.MAX_VALUE;
                } else if (alphabetaStarted) {
                    if (oldExecutionTime == 0) {
                        nodeCounter = Long.MAX_VALUE;
                    } else { // Don't update if execution time is too low
                        // We finished in time, adjust nodeCounter:
                        // oldExecutionTime / timeout = nodeCounter / adjustedNodeCounter
                        // Thus:
                        // adjustedNodeCounter = (timeout * nodeCounter) / oldExecutionTime
                        try {
                            nodeCounter = Math.multiplyExact(timeout, nodeCounter) / oldExecutionTime;
                        } catch (ArithmeticException e) {
                            nodeCounter = Long.MAX_VALUE;
                        }
                    }
                }
            }

            // If alphabeta hadn't started the old run (only the heuristic was calculated), don't update the nodesAverage
            if (alphabetaStarted) {
                if (nodesAverage == 0) {
                    nodesAverage = nodeCounter;
                } else {
                    // Give more importance to nodeCounter since it contains
                    // the number of nodes analyzed in the previous round
                    try {
                        nodesAverage = Math.addExact(nodeCounter, nodesAverage) / 2L;
                    } catch (ArithmeticException e) {
                        nodesAverage = Long.MAX_VALUE;
                    }
                }
            }
            // Reset
            nodeCounter = 0;
            this.alphabetaStarted = false;

            // Actually calculates the visit depth
            depth = OptimizedDepth.optimizedDepth(3, FC.length, nodesAverage);
            // System.out.println("Depth found: " + depth);
        }

        // System.out.println("Nodes average: " + nodesAverage);

        // Update board with opponent's move
        MNKCell lastOpponentMove = null;
        if (MC.length > 0) {
            lastOpponentMove = MC[MC.length - 1];
            board[lastOpponentMove.i][lastOpponentMove.j] = lastOpponentMove.state;
        }

        // Update tmpBoard
        copyTmpBoard();

        // Using a hash table for O(1) operations
        Set<MNKCell> freeCellsSet = new HashSet<>(Arrays.asList(FC));

        // Updates the tree (calculated in previous rounds) discarding the branches of the not selected moves
        // If it hasn't been calculated it runs the heuristic on the current table
        EvaluatedCell[] cells;
        if (bestMove != null && lastOpponentMove != null /* So it is not the first round */) {
            Node child = bestMove.selectChildByMove(lastOpponentMove); // Search if the opponent has made a move we already computed in previous rounds
            if (child != null) {
                root = child;
                root.setParent(null);
                cells = root.getSortedCells();
            } else {
                cells = complexEvaluateTmpBoard(freeCellsSet, our);
                root = new Node(null, null, cells);
            }
        } else {
            cells = complexEvaluateTmpBoard(freeCellsSet, our);
            root = new Node(null, null, cells);
        }

        // DebugUtil.printTable(board, cells);

        bestMove = null;

        try {
            // alphabetaStart(rootNode, −∞, +∞, depth, freeCells)
            // -Integer.MAX_VALUE is used instead of Integer.MIN_VALUE because -Integer.MIN_VALUE overflows (due to two's complement)
            alphabetaStart(root, -Integer.MAX_VALUE, Integer.MAX_VALUE, depth, freeCellsSet);
        } catch (Exception ignored) {
            // System.out.println("TIMEOUT");
        }

        oldExecutionTime = System.currentTimeMillis() - startTime;

        // System.out.print("Best move: ");
        // System.out.println(bestMove != null ? bestMove.getCell().getCell().i + " " + bestMove.getCell().getCell().j : "null");
        // System.out.println("Nodes counted this round: " + nodeCounter + " in " + oldExecutionTime + " ms");
        // System.out.println("");

        // Save the selected move into this.board and returns it
        // bestMove is null when we block an opponent win, we can win in a move or the first branch of the tree hasn't been completely visited in time
        // In all of those cases the best move to do is the one indicated by the heuristic, so cells[0]
        return saveMove(bestMove == null ? cells[0].getCell() : bestMove.getCell().getCell());
    }

    /**
     * Copies board into tmpBoard.
     */
    private void copyTmpBoard() {
        for (int i = 0; i < M; i++) {
            System.arraycopy(board[i], 0, tmpBoard[i], 0, N);
        }
    }

    /**
     * Throws a {@link RuntimeException} if the time has run out.
     *
     * @throws RuntimeException If the time has run out.
     */
    private void checkTime() throws RuntimeException {
        if (System.currentTimeMillis() - startTime >= timeout) {
            throw new RuntimeException("TIMEOUT");
        }
    }

    /**
     * This function is invoked by {@link #alphabetaStart(Node, int, int, int, Set)} and visits the
     * provided tree using the alphabeta algorithm.
     *
     * @param node The tree.
     * @param alpha The alpha value inherited from the parent.
     * @param beta The beta value inherited from the parent.
     * @param depth The depth of the visit.
     * @param player Indicates whose turn it is.
     * @param FC The set containing the free cells.
     * @return The result of the alphabeta visit.
     */
    private int alphabeta(Node node, int alpha, int beta, int depth, MNKCellState player, Set<MNKCell> FC) {
        checkTime();

        // Keep track of analyzed nodes
        nodeCounter++;

        if (node.getSortedCells().length == 0) {
            return 0; // Draw
        }
        if (node.getSortedCells().length == 1 && node.getSortedCells()[0].getValue() >= OUR_VICTORY) {
            // A player has won
            // Returns a value that takes into consideration the amount of moves that it takes to win in order to be more aggressive
            if (player == our) {
                return OUR_VICTORY - (this.depth - depth);
            } else {
                return -OUR_VICTORY + (this.depth - depth);
            }
        }
        if (depth == 0) {
            // Stops the visit and returns the heuristic value of this configuration
            return simpleEvaluateTmpBoard(FC);
        }

        // Values needed by alphabeta
        int value;
        Node[] children = node.getChildren();
        EvaluatedCell[] cells = node.getSortedCells();

        if (player == our) {
            value = -Integer.MAX_VALUE;
            for (int i = 0; i < cells.length; i++) {
                EvaluatedCell cell = cells[i];

                // Update tmpBoard and FC before calling alphabeta recursively
                tmpBoard[cell.getCell().i][cell.getCell().j] = our;
                FC.remove(cell.getCell());

                // Create (or get) the child node
                Node child;
                if (children != null) {
                    child = children[i];
                    if (child == null) {
                        EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, opponent);
                        child = node.addChild(cell, evaluatedCells);
                    }
                } else {
                    EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, opponent);
                    child = node.addChild(cell, evaluatedCells); // Instantiate node.getChildren() array
                    children = node.getChildren();
                }

                // Calls alphabeta recursively
                int alphabeta = alphabeta(child, alpha, beta, depth - 1, opponent, FC);

                value = Math.max(value, alphabeta);
                alpha = Math.max(value, alpha);

                // Restore tmpBoard and FC
                tmpBoard[cell.getCell().i][cell.getCell().j] = MNKCellState.FREE;
                FC.add(cell.getCell());

                // alphabeta cutoff
                if (beta <= alpha) {
                    break;
                }
            }
        } else {
            value = Integer.MAX_VALUE;
            for (int i = 0; i < cells.length; i++) {
                EvaluatedCell cell = cells[i];

                // Update tmpBoard and FC before calling alphabeta recursively
                tmpBoard[cell.getCell().i][cell.getCell().j] = opponent;
                FC.remove(cell.getCell());

                // Create (or get) the child node
                Node child;
                if (children != null) {
                    child = children[i];
                    if (child == null) {
                        EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, our);
                        child = node.addChild(cell, evaluatedCells);
                    }
                } else {
                    EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, our);
                    child = node.addChild(cell, evaluatedCells); // Instantiate node.getChildren() array
                    children = node.getChildren();
                }

                // Calls alphabeta recursively
                int alphabeta = alphabeta(child, alpha, beta, depth - 1, our, FC);

                value = Math.min(value, alphabeta);
                beta = Math.min(value, beta);

                // Restore tmpBoard and FC
                tmpBoard[cell.getCell().i][cell.getCell().j] = MNKCellState.FREE;
                FC.add(cell.getCell());

                // alphabeta cutoff
                if (beta <= alpha) {
                    break;
                }
            }
        }

        // Returns the result of the alphabeta visit
        return value;
    }

    /**
     * Starts the alphabeta visit and puts in bestMove the best move it has found.
     *
     * @param node The root of the tree to visit.
     * @param alpha The alpha value inherited from the parent.
     * @param beta The beta value inherited from the parent.
     * @param depth The depth of the visit.
     * @param FC The set containing the free cells.
     */
    private void alphabetaStart(Node node, int alpha, int beta, int depth, Set<MNKCell> FC) {
        checkTime();

        // Keep track of analyzed nodes
        nodeCounter++;

        if (node.getSortedCells().length <= 1 || depth == 0) {
            // We must block the opponent from winning, go to win in one move or alphabetaStart was invoked with a depth of 0
            this.alphabetaStarted = false; // We didn't start the alphabeta algorithm
            bestMove = null;
        } else {
            // Start of the alphabeta algorithm

            this.alphabetaStarted = true; // We started the alphabeta algorithm

            // Values needed to start alphabeta
            int value = -Integer.MAX_VALUE;
            Node[] children = node.getChildren();
            EvaluatedCell[] cells = node.getSortedCells();

            int bestMoveValue = value;

            for (int i = 0; i < cells.length; i++) {
                EvaluatedCell cell = cells[i];

                // Update tmpBoard and FC before calling alphabeta
                tmpBoard[cell.getCell().i][cell.getCell().j] = our;
                FC.remove(cell.getCell());

                // Create (or get) the child node
                Node child;
                if (children != null) {
                    child = children[i];
                    if (child == null) {
                        EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, opponent);
                        child = node.addChild(cell, evaluatedCells);
                    }
                } else {
                    EvaluatedCell[] evaluatedCells = complexEvaluateTmpBoard(FC, opponent);
                    child = node.addChild(cell, evaluatedCells); // Instantiate node.getChildren() array
                    children = node.getChildren();
                }

                // Calls alphabeta
                int alphabeta = alphabeta(child, alpha, beta, depth - 1, opponent, FC);

                value = Math.max(value, alphabeta);
                alpha = Math.max(value, alpha);

                // Restore tmpBoard and FC
                tmpBoard[cell.getCell().i][cell.getCell().j] = MNKCellState.FREE;
                FC.add(cell.getCell());

                // Update bestMove if this move is better than the previous
                if (value > bestMoveValue) {
                    bestMove = child;
                    bestMoveValue = value;
                }

                // alphabeta cutoff
                if (beta <= alpha) {
                    break;
                }
            }
        }
    }

    /**
     * Calculates a simple evaluation of the board used to determine how good is that configuration.
     *
     * @param FC The set containing the free cells.
     * @return The evaluation of the board.
     */
    private int simpleEvaluateTmpBoard(Set<MNKCell> FC) {
        int sum = 0;
        for (MNKCell cell : FC) {
            sum += evaluateUtil.simpleEvaluate(cell, our);
            sum -= evaluateUtil.simpleEvaluate(cell, opponent);
        }
        return sum;
    }

    /**
     * Returns an array of {@link EvaluatedCell} sorted by best move (using the heuristic provided by {@link EvaluateUtil#evaluate(MNKCell, MNKCellState)}).
     * If there is a move that leads to an immediate victory, either of the current player or the opponent,
     * the returned array has length of 1 and contains only that move.
     *
     * @param FC The set containing the free cells.
     * @param player Whose player the turn is.
     * @return An array of {@link EvaluatedCell} sorted by best move.
     */
    private EvaluatedCell[] complexEvaluateTmpBoard(Set<MNKCell> FC, MNKCellState player) {
        EvaluatedCell[] cells = new EvaluatedCell[FC.size()]; // The array to return
        int index = 0; // The index of the next element to insert

        Iterator<MNKCell> it = FC.iterator(); // Iterator over the Set of FC
        while (it.hasNext()) {
            MNKCell cell = it.next();

            int eval = evaluateUtil.evaluate(cell, player); // Evaluate our move
            if (eval == OUR_VICTORY) {
                return new EvaluatedCell[]{new EvaluatedCell(cell, OUR_VICTORY)};
            }
            int evalOpponent = evaluateUtil.evaluate(cell, player == our ? opponent : our); // Evaluate opponent's move
            if (evalOpponent == OUR_VICTORY) { // Does the opponent win?
                // The opponent wins with a move, can we win in 1 move?
                while (it.hasNext()) {
                    MNKCell otherCell = it.next(); // This consumes the rest of the iterator

                    if (evaluateUtil.isWinningCell(otherCell, player)) {
                        // Yes, we can win!
                        return new EvaluatedCell[]{new EvaluatedCell(otherCell, OUR_VICTORY)};
                    }
                }
                // No, we can't win. Block the opponent then
                return new EvaluatedCell[]{new EvaluatedCell(cell, OPPONENT_VICTORY)};
            }
            // The value of the cell is the sum of the heuristic evaluation from our point of view and from the opponent's one.
            // This way we take into consideration cells which doesn't help us, but blocks opponent's possible good alignments.
            cells[index++] = new EvaluatedCell(cell, eval + evalOpponent);
        }

        // Sort in O(n)
        SortUtil.radixSort(cells);
        return cells;
    }

    /**
     * Saves the provided move into {@link #board} and returns it.
     *
     * @param move The move to be saved.
     * @return The provided move.
     */
    private MNKCell saveMove(MNKCell move) {
        board[move.i][move.j] = our;
        root = bestMove;
        return move;
    }

    /**
     * Returns the player name
     *
     * @return string
     */
    @Override
    public String playerName() {
        return "CadregaBot";
    }
}
