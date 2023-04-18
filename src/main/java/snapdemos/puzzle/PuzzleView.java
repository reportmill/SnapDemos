package snapdemos.puzzle;
import snap.geom.Insets;
import snap.geom.Point;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.Painter;
import snap.view.*;
import snap.viewx.Explode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This class draws the puzzle board and handles user interaction.
 */
public class PuzzleView extends RowView {

    // The puzzle
    private Puzzle _puzzle;

    // The piece views
    private PuzzlePieceView[][] _pieceViews;

    // The selected pieces
    private List<PuzzlePieceView> _selPieceViews = new ArrayList<>();

    // Constants
    private static Font DEFAULT_FONT = Font.getFont("Arial", 32);
    private static int DEFAULT_SPACING = 10;
    private static Border DEFAULT_BORDER = Border.createLineBorder(Color.LIGHTGRAY, 1);
    private static Insets DEFAULT_PADDING = new Insets(16);

    /**
     * Constructor.
     */
    public PuzzleView()
    {
        super();
        setFill(Color.WHITE);
        setBorder(DEFAULT_BORDER);
        setSpacing(DEFAULT_SPACING);
        setFont(DEFAULT_FONT);
        setPadding(DEFAULT_PADDING);
        setFillHeight(true);
        setPrefSize(500, 500);

        // Get events
        enableEvents(MouseEvents);

        _puzzle = new Puzzle();
        createPieceViews();
    }

    /**
     * Returns the puzzle.
     */
    public Puzzle getPuzzle()  { return _puzzle; }

    /**
     * Returns the puzzle piece views.
     */
    public PuzzlePieceView[][] getPieceViews()  { return _pieceViews; }

    /**
     * Creates the puzzle piece views.
     */
    protected void createPieceViews()
    {
        int colCount = getColCount();
        int rowCount = getRowCount();
        _pieceViews = new PuzzlePieceView[colCount][rowCount];

        // Get pieces
        PuzzlePiece[] allPieces = _puzzle.getPieces();
        List<PuzzlePiece> shuffledPieces = new ArrayList<>();
        Collections.addAll(shuffledPieces, allPieces);
        Collections.shuffle(shuffledPieces);
        Random random = new Random(1000);

        for (int i = 0; i < colCount; i++) {

            // Create/add column
            ColView puzzleCol = new ColView();
            puzzleCol.setGrowWidth(true);
            puzzleCol.setSpacing(DEFAULT_SPACING);
            addChild(puzzleCol);

            // Create/add piece views
            for (int j = 0; j < rowCount; j++) {

                // Get random puzzle piece
                int puzzlePieceIndex = random.nextInt(shuffledPieces.size());
                PuzzlePiece puzzlePiece = shuffledPieces.get(puzzlePieceIndex);
                while (i == puzzlePiece.getColIndex() && j == puzzlePiece.getRowIndex() && shuffledPieces.size() > 1) {
                    puzzlePieceIndex = random.nextInt(shuffledPieces.size());
                    puzzlePiece = shuffledPieces.get(puzzlePieceIndex);
                }
                shuffledPieces.remove(puzzlePiece);

                // Create View
                PuzzlePieceView puzzlePieceView = new PuzzlePieceView(this, puzzlePiece);
                _pieceViews[i][j] = puzzlePieceView;
                puzzleCol.addChild(puzzlePieceView);
                puzzlePieceView.addPropChangeListener(pc -> puzzlePieceViewSelectedChanged(puzzlePieceView), PuzzlePieceView.Selected_Prop);
            }
        }
    }

    /**
     * Returns the piece view at given XY.
     */
    public PuzzlePieceView getPieceViewForXY(double aX, double aY)
    {
        for (PuzzlePieceView[] pieceViewsRow : _pieceViews) {
            double yInRowView = pieceViewsRow[0].getParent().getY() - aY;
            for (PuzzlePieceView pieceView : pieceViewsRow)
                if (pieceView.contains(pieceView.getX() - aX, pieceView.getY() - yInRowView))
                    return pieceView;
        }
        return null;
    }

    /**
     * Returns the number of puzzle columns.
     */
    public int getColCount()  { return _puzzle.getColCount(); }

    /**
     * Returns the number of puzzle rows.
     */
    public int getRowCount()  { return _puzzle.getRowCount(); }

    /**
     * Called when puzzle piece view selected changed.
     */
    private void puzzlePieceViewSelectedChanged(PuzzlePieceView puzzlePieceView)
    {
        // Handle de-selected
        if (!puzzlePieceView.isSelected()) {
            _selPieceViews.remove(puzzlePieceView);
        }

        // Handle selected
        else {

            // Add to SelPieces
            if (!_selPieceViews.contains(puzzlePieceView))
                _selPieceViews.add(puzzlePieceView);

            // If two selected, do exchange
            if (_selPieceViews.size() == 2) {
                ViewUtils.runLater(() -> exchangeSelPieceViews());
            }
        }
    }

    /**
     * Override to repaint SelPieceView.
     */
    @Override
    protected void paintChildren(Painter aPntr)
    {
        super.paintChildren(aPntr);

        // Repaint selected
        if (_selPieceViews.size() > 0) {
            PuzzlePieceView selPieceView = _selPieceViews.get(0);
            aPntr.save();
            aPntr.transform(selPieceView.getParent().getLocalToParent());
            aPntr.transform(selPieceView.getLocalToParent());
            ViewUtils.paintAll(selPieceView, aPntr);
            aPntr.restore();
        }
    }

    /**
     * Exchanges two pieces.
     */
    protected void exchangeSelPieceViews()
    {
        PuzzlePieceView pieceView1 = _selPieceViews.get(0);
        PuzzlePieceView pieceView2 = _selPieceViews.get(1);

        // Get current piece view mid points in puzzle view
        Point midView1 = pieceView1.localToParent(pieceView1.getWidth() / 2, pieceView1.getHeight() / 2, this);
        Point midView2 = pieceView2.localToParent(pieceView2.getWidth() / 2, pieceView2.getHeight() / 2, this);

        // Swap Views
        ParentView parent1 = pieceView1.getParent();
        int index1 = pieceView1.indexInParent();
        ViewUtils.removeChild(parent1, pieceView1);
        ViewUtils.replaceView(pieceView2, pieceView1);
        ViewUtils.addChild(parent1, pieceView2, index1);

        // Configure PieceView1 to scale up, translate over, and de-select
        pieceView1.setTransX(midView1.x - midView2.x);
        pieceView1.setTransY(midView1.y - midView2.y);
        ViewAnim anim1 = pieceView1.getAnim(200);
        anim1.setScale(1.4);
        anim1.getAnim(800).setTransX(0).setTransY(0);
        anim1.setOnFinish(() -> exchangeSelPieceViewsAnimFinished(pieceView1));
        anim1.play();

        // Configure PieceView2 to translate over
        pieceView2.setSelected(false);
        pieceView2.setTransX(midView2.x - midView1.x);
        pieceView2.setTransY(midView2.y - midView1.y);
        ViewAnim anim2 = pieceView2.getAnim(800);
        anim2.setTransX(0).setTransY(0);
        anim2.play();
    }

    /**
     * Called when anim is finished.
     */
    private void exchangeSelPieceViewsAnimFinished(PuzzlePieceView pieceView1)
    {
        pieceView1.setSelected(false);
        _selPieceViews.clear();

        PuzzlePieceView[][] pieceViews = getPieceViews();
        for (PuzzlePieceView[] pieceViewCol : pieceViews)
            for (PuzzlePieceView pieceView : pieceViewCol)
                if (!pieceView.isSolved())
                    return;
        new Explode(this, 30, 30, null).playAndRestore();
    }
}
