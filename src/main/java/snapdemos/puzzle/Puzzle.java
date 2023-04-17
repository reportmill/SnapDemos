package snapdemos.puzzle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class models the puzzle.
 */
public class Puzzle {

    // The number of columns
    private int _colCount = 4;

    // The number of rows
    private int _rowCount = 4;

    // The pieces
    private PuzzlePiece[][] _pieces;

    /**
     * Constructor.
     */
    public Puzzle()
    {
        super();
    }

    /**
     * Returns the column count.
     */
    public int getColCount()  { return _colCount; }

    /**
     * Returns the row count.
     */
    public int getRowCount()  { return _rowCount; }

    /**
     * Returns the pieces.
     */
    public PuzzlePiece[][] getPieces()
    {
        // If already set, just return
        if (_pieces != null) return _pieces;

        // Get random numbers
        Random random = new Random(1000);
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < 100; i++)
            numbers.add(i);

        // Create
        int colCount = getColCount();
        int rowCount = getRowCount();
        PuzzlePiece[][] pieces = new PuzzlePiece[colCount][rowCount];
        for (int i = 0; i < colCount; i++) {
            for (int j = 0; j < rowCount; j++) {
                PuzzlePiece piece = new PuzzlePiece(this, i, j);
                int randomNumberIndex = random.nextInt(numbers.size());
                int randomNumber = numbers.remove(randomNumberIndex);
                piece.setNumber(randomNumber);
                pieces[i][j] = piece;
            }
        }

        // Set, return
        return _pieces = pieces;
    }

    /**
     * Returns the puzzle piece for column and view.
     */
    public PuzzlePiece getPieceForColAndRow(int colIndex, int rowIndex)
    {
        PuzzlePiece[][] pieces = getPieces();
        return pieces[colIndex][rowIndex];
    }
}
