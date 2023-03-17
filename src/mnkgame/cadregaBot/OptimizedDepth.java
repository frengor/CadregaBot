package mnkgame.cadregaBot;

/**
 * Utility class to find the optimized depth for an alphabeta visit.
 */
public final class OptimizedDepth {
    /**
     * Calculates the depth to reach in order to pass through nodesToVisit nodes in a full alphabeta visit.
     *
     * @param startingDepth The minimum depth, this function cannot return a depth that is lower than this.
     * @param freeCells The amount of free cells in the board.
     * @param nodesToVisit The number of nodes to pass through in a visit.
     * @return The calculated depth.
     */
    public static int optimizedDepth(int startingDepth, long freeCells, long nodesToVisit) {
        // For the following passages we are assuming we are in the best case (that's not true, but we are close enough to assume it)
        int depth = startingDepth;
        long visitedNodes = (long) Math.sqrt(freeCells);
        for (int i = 1; i < startingDepth && freeCells > 0; i++) { // Catches us with startingDepth depth
            freeCells--;
            try {   //overflow handler
                visitedNodes = Math.multiplyExact(visitedNodes, (long) Math.sqrt(freeCells));
            } catch (ArithmeticException e) {
                return depth;
            }
        }

        while (visitedNodes < nodesToVisit) { // Increases the depth until visitedNodes > nodesToVisit
            freeCells--;
            if (freeCells <= 0) {
                return depth;
            }
            try {   //overflow handler
                visitedNodes = Math.multiplyExact(visitedNodes, (long) Math.sqrt(freeCells));
            } catch (ArithmeticException e) {
                return depth;
            }
            depth++;
            //System.out.println("VisitedNodes: " + visitedNodes);
        }

        // Now visitedNodes < nodesToVisit, but we need to return a depth in which visitedNodes > nodesToVisit so we decrease by 1 the depth
        // Since this is not the best case, depth is decreased by 1 again in order to keep a bit of margin
        return Math.max(startingDepth, depth - 2); // depth cannot be lesser than startingDepth
    }

    // Private constructor
    private OptimizedDepth() {
        throw new UnsupportedOperationException();
    }
}
