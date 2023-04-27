package connectx.cadregaBot;

import connectx.CXCell;

/**
 * A node of the alphabeta tree.
 */
public final class Node {
    private final EvaluatedCell cell; // Cell of this node
    private Node parent;
    private final EvaluatedCell[] sortedCells; // Possible next moves sorted by (heuristic) probability of being a good move
    // The children of a node are not created at its creation, but they are added dynamically when alphabeta needs them
    private Node[] children; // This array contains the children already created
    private int childToAdd; // Index of the next child to add

    /**
     * Creates a new {@code Node}.
     *
     * @param cell The {@link EvaluatedCell} of this node.
     * @param parent The parent {@code Node}.
     * @param sortedCells The sorted array of {@link EvaluatedCell}.
     */
    public Node(EvaluatedCell cell, Node parent, EvaluatedCell[] sortedCells) {
        this.cell = cell;
        this.parent = parent;
        this.sortedCells = sortedCells;
    }

    /**
     * Returns the {@link EvaluatedCell} of this node.
     *
     * @return The {@link EvaluatedCell} of this node.
     */
    public EvaluatedCell getCell() {
        return cell;
    }

    /**
     * Returns the parent of this node.
     *
     * @return The parent of this node.
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node.
     *
     * @param parent The new parent of this node.
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    /**
     * Returns the sorted array of {@link EvaluatedCell}s of the node.
     *
     * @return The sorted array of {@link EvaluatedCell}s of the node.
     */
    public EvaluatedCell[] getSortedCells() {
        return sortedCells;
    }

    /**
     * Returns the array of children already created.
     *
     * @return The array of children already created.
     */
    public Node[] getChildren() {
        return children;
    }

    /**
     * Adds a child to this node. To create a node it is necessary to provide its sorted array of {@link EvaluatedCell}s.
     *
     * @param child The cell of the child to add.
     * @param evaluatedCellsOfChild The sorted array of {@link EvaluatedCell}s of the child.
     * @return The newly created child.
     */
    public Node addChild(EvaluatedCell child, EvaluatedCell[] evaluatedCellsOfChild) {
        if (childToAdd < 0 || childToAdd > sortedCells.length - 1) {
            throw new RuntimeException("Invalid childToAdd value");
        }
        if (children == null) {
            children = new Node[sortedCells.length];
        }
        Node newChild = new Node(child, this, evaluatedCellsOfChild);
        children[childToAdd] = newChild;
        childToAdd++;
        return newChild;
    }

    /**
     * Searches a child with the provided cell into the children's array of this node.
     *
     * @param move The cell to search for.
     * @return The child node if it exists, {@code null} otherwise.
     */
    public Node selectChildByMove(CXCell move) {
        if (children == null) {
            return null;
        }
        for (Node n : children) {
            if (n == null) {
                break;
            }
            CXCell cell = n.getCell().getCell();
            if (cell.i == move.i && cell.j == move.j) {
                return n;
            }
        }
        return null;
    }

    /**
     * Adds a leaf as a child of this node.
     *
     * @param child The cell of the leaf to add.
     * @return the newly created leaf.
     */
    public Node addLeaf(EvaluatedCell child) {
        if (childToAdd < 0 || childToAdd > sortedCells.length - 1) {
            throw new RuntimeException("Invalid childToAdd value");
        }
        if (children == null) {
            children = new Node[sortedCells.length];
        }
        Node newChild = new Node(child, this, new EvaluatedCell[0]);
        children[childToAdd] = newChild;
        childToAdd++;
        return newChild;
    }
}
