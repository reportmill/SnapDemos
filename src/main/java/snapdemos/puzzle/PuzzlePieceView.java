package snapdemos.puzzle;
import snap.geom.Pos;
import snap.gfx.*;
import snap.view.*;

/**
 * This class represents a puzzle piece.
 */
public class PuzzlePieceView extends BoxView {

    // The PuzzleView
    private PuzzleView _puzzleView;

    // The PuzzlePiece
    private PuzzlePiece _puzzlePiece;

    // The label
    private Label _label;

    // Whether piece is selected
    private boolean _selected;

    // Whether button is pressed
    private boolean  _pressed;

    // Whether button is under mouse
    protected boolean  _targeted;

    // Constants for properties
    public static final String Selected_Prop = "Selected";
    public static final String Pressed_Prop = "Pressed";
    public static final String Targeted_Prop = "Targeted";

    // Constants for attributes
    private static Color BACK_FILL = new Color("#F0F8FF");
    private static Border BORDER = Border.createLineBorder(Color.BLACK, 1);
    private static int BORDER_RADIUS = 5;
    private static Effect DEFAULT_EFFECT = new ShadowEffect(12, Color.GRAY, 5, 5);

    // Images
    public static final Image UP_ARROW = Image.getImageForClassResource(PuzzlePiece.class, "UpArrow.png");
    public static final Image DOWN_ARROW = Image.getImageForClassResource(PuzzlePiece.class, "DownArrow.png");
    public static final Image LEFT_ARROW = Image.getImageForClassResource(PuzzlePiece.class, "LeftArrow.png");
    public static final Image RIGHT_ARROW = Image.getImageForClassResource(PuzzlePiece.class, "RightArrow.png");

    /**
     * Constructor.
     */
    public PuzzlePieceView(PuzzleView puzzleView, PuzzlePiece puzzlePiece)
    {
        super();
        setFill(BACK_FILL);
        setBorder(BORDER);
        setBorderRadius(BORDER_RADIUS);
        setEffect(DEFAULT_EFFECT);
        setGrowWidth(true);
        setGrowHeight(true);
        setAlign(Pos.CENTER);

        enableEvents(MouseEvents);

        // Set PuzzleView, PuzzlePiece
        _puzzleView = puzzleView;
        _puzzlePiece = puzzlePiece;

        // Create label
        String labelText = String.valueOf(puzzlePiece.getNumber());
        _label = new Label(labelText);
        _label.setAlign(Pos.CENTER);
        setContent(_label);
    }

    /**
     * Returns whether piece is selected.
     */
    public boolean isSelected()  { return _selected; }

    /**
     * Sets whether piece is selected.
     */
    public void setSelected(boolean aValue)
    {
        if (aValue == isSelected()) return;
        firePropChange(Selected_Prop, _selected, _selected = aValue);

        // Handle Selected true
        if (aValue) {
            ViewAnim anim = getAnimCleared(200);
            anim.setScale(1.2);
            anim.setFill(BACK_FILL.brighter().brighter());
            anim.play();
        }

        // Handle Selected false
        else {
            ViewAnim anim = getAnimCleared(200);
            anim.setScale(1);
            anim.setFill(BACK_FILL);
            anim.play();
        }
    }

    /**
     * Returns whether piece is pressed (visibly).
     */
    public boolean isPressed()  { return _pressed; }

    /**
     * Sets whether piece is pressed (visibly).
     */
    protected void setPressed(boolean aValue)
    {
        if (aValue == _pressed) return;
        firePropChange(Pressed_Prop, _pressed, _pressed = aValue);

        // Handle Pressed true
        if (aValue) {
            ViewAnim anim = getAnimCleared(200);
            anim.setScale(.9);
            anim.setFill(BACK_FILL.darker());
            anim.play();
        }

        // Handle Pressed false
        else {
            ViewAnim anim = getAnimCleared(200);
            anim.setScale(1);
            anim.setFill(BACK_FILL);
            anim.play();
        }
    }

    /**
     * Returns whether piece is under mouse.
     */
    public boolean isTargeted()  { return _targeted; }

    /**
     * Sets whether piece is under mouse.
     */
    protected void setTargeted(boolean aValue)
    {
        if (aValue == _targeted) return;
        firePropChange(Targeted_Prop, _targeted, _targeted = aValue);
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MousePress
        if (anEvent.isMousePress()) {
            setTargeted(true);
            setPressed(true);
            anEvent.consume();
        }

        // Handle MouseReleased
        else if (anEvent.isMouseRelease()) {
            boolean isPressed = isPressed();
            setPressed(false);
            if (isTargeted() && isPressed) {
                setSelected(!isSelected());
                fireActionEvent(anEvent);
            }
        }

        // Handle MouseEnter
        else if (anEvent.isMouseEnter())
            setTargeted(true);

        // Handle MouseExit
        else if (anEvent.isMouseExit())
            setTargeted(false);
    }

    /**
     * Override to expand paint bounds.
     */
    @Override
    public void repaint(double aX, double aY, double aW, double aH)
    {
        super.repaint(aX - 20, aY - 20, aW + 40, aH + 40);
    }

    /**
     * Returns whether is solved.
     */
    public boolean isSolved()
    {
        boolean colMatches = getParent().indexInParent() == _puzzlePiece.getColIndex();
        return colMatches && indexInParent() == _puzzlePiece.getRowIndex();
    }

    /**
     * Override to paint col/row matching arrows.
     */
    @Override
    protected void paintFront(Painter aPntr)
    {
        boolean colMatches = getParent().indexInParent() == _puzzlePiece.getColIndex();
        boolean rowMatches = indexInParent() == _puzzlePiece.getRowIndex();
        if (colMatches && rowMatches) {
            aPntr.fillWithPaint(getBoundsLocal(), Color.LIGHTBLUE);
            return;
        }

        aPntr.setOpacity(.5);
        double IMAGE_WIDTH = 20;

        if (colMatches) {
            double imageX = _label.getMidX() - IMAGE_WIDTH / 2;
            double imageY = _label.getY() - IMAGE_WIDTH;
            aPntr.drawImage(UP_ARROW, imageX, imageY, IMAGE_WIDTH, IMAGE_WIDTH);
            double imageY2 = _label.getMaxY();
            aPntr.drawImage(DOWN_ARROW, imageX, imageY2, IMAGE_WIDTH, IMAGE_WIDTH);
        }
        else if (rowMatches) {
            double imageX = _label.getX() - IMAGE_WIDTH;
            double imageY = _label.getMidY() - IMAGE_WIDTH / 2;
            aPntr.drawImage(LEFT_ARROW, imageX, imageY, IMAGE_WIDTH, IMAGE_WIDTH);
            double imageX2 = _label.getMaxX();
            aPntr.drawImage(RIGHT_ARROW, imageX2, imageY, IMAGE_WIDTH, IMAGE_WIDTH);
        }
        aPntr.setOpacity(1);
    }

    /**
     * Override to add props.
     */
    @Override
    public String toStringProps()
    {
        String superProps = super.toStringProps();
        return superProps + ", Number=" + _puzzlePiece.getNumber();
    }
}
