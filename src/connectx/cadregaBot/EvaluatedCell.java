package connectx.cadregaBot;

import connectx.CXCell;

/**
 * A cell which has been evaluated by the heuristic.
 * If placing a move in that cell leads to an immediate victory, the value of that cell is either:
 * <ul>
 *     <li>{@link CadregaBot#OUR_VICTORY OUR_VICTORY} when the winner is the current player</li>
 *     <li>{@link CadregaBot#OPPONENT_VICTORY OPPONENT_VICTORY} when the winner is the opponent</li>
 * </ul>
 */
public final class EvaluatedCell {

    private final CXCell cell;
    private final int value;

    /**
     * Creates a new {@code EvaluatedCell}.
     *
     * @param cell The {@link CXCell}.
     * @param value The heuristic value of cell.
     */
    public EvaluatedCell(CXCell cell, int value) {
        this.cell = cell;
        this.value = value;
    }

    /**
     * Returns the {@link CXCell}.
     *
     * @return The {@link CXCell}.
     */
    public CXCell getCell() {
        return cell;
    }

    /**
     * Returns the heuristic value of the cell.
     *
     * @return The heuristic value of the cell.
     */
    public int getValue() {
        return value;
    }
}
