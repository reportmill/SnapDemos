package snapdemos.puzzle;

/**
 * This class represents a puzzle piece.
 */
public class PuzzlePiece {

    // The Puzzle
    private Puzzle _puzzle;

    // The column index in puzzle
    private int _colIndex;

    // The row index in puzzle
    private int _rowIndex;

    // The number
    private int _number;

    /**
     * Constructor.
     */
    public PuzzlePiece(Puzzle aPuzzle, int colIndex, int rowIndex)
    {
        super();
        _puzzle = aPuzzle;
        _colIndex = colIndex;
        _rowIndex = rowIndex;
    }

    /**
     * Returns the column index.
     */
    public int getColIndex()  { return _colIndex; }

    /**
     * Returns the row index.
     */
    public int getRowIndex()  { return _rowIndex; }

    /**
     * Returns the number.
     */
    public int getNumber()  { return _number; }

    /**
     * Set the number.
     */
    public void setNumber(int aValue)  { _number = aValue; }
}
