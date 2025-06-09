package snapdemos.tetris;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.util.Convert;
import snap.util.ListUtils;
import snap.util.MathUtils;
import snap.view.*;
import snap.viewx.Explode;

/**
 * This class is the main game view.
 */
public class PlayView extends ParentView {
    
    // The current block
    private Block _block;
    
    // The next block
    private Block _nextBlock;
    
    // The list of stack rows
    private List<StackRow> _stackRows = new ArrayList<>();
    
    // Whether user has requested block to drop faster
    private boolean _dropFast;

    // The current score
    private int _score;
    
    // Whether game is over
    private boolean _gameOver;

    // The Run to be called for each frame during game loop
    private Runnable _timerFiredRun;
    
    // The size of the field
    protected static int GRID_WIDTH = 10;
    private static int GRID_HEIGHT = 20;
    private static final int TILE_SIZE = Block.TILE_SIZE;

    // Constants
    static final String Score_Prop = "Score";
    static final String NextBlock_Prop = "NextBlock";

    /**
     * Constructor.
     */
    public PlayView()
    {
        super();
        setFill(Color.WHITE);
        enableEvents(KeyPress);
        setFocusable(true);

        // Set size
        double viewW = GRID_WIDTH * TILE_SIZE;
        double viewH = GRID_HEIGHT * TILE_SIZE;
        setPrefSize(viewW, viewH);

        // Get starting  block
        getNextBlock(true);
    }

    /**
     * Starts play.
     */
    public void startGame()
    {
        // Reset state
        _stackRows.clear();
        removeChildren();
        _gameOver = false;
        _score = 0;

        // Start timer, add block
        setTimerRunning(true);
        addNewBlock();
        requestFocus();
        getRootView().repaint();
    }

    /**
     * Pauses game.
     */
    public void pauseGame()
    {
        setTimerRunning(!isTimerRunning());
    }

    /**
     * Returns the score.
     */
    public int getScore()  { return _score; }

    /**
     * Sets the score.
     */
    public void setScore(int aValue)
    {
        if (aValue == getScore()) return;
        firePropChange(Score_Prop, _score, _score = aValue);
    }

    /**
     * Returns whether timer is running.
     */
    private boolean isTimerRunning()  { return _timerFiredRun != null; }

    /**
     * Sets whether timer is running.
     */
    private void setTimerRunning(boolean aValue)
    {
        if (aValue == isTimerRunning()) return;

        // Start timer
        if (_timerFiredRun == null) {
            _timerFiredRun = this::handleTimerFired;
            getEnv().runIntervals(_timerFiredRun, 20);
        }

        // Stop timer
        else {
            getEnv().stopIntervals(_timerFiredRun);
            _timerFiredRun = null;
        }
    }

    /**
     * Adds a new block to play view.
     */
    public void addNewBlock()
    {
        // Create block
        _block = getNextBlock(true);

        // Center block
        double blockX = MathUtils.round((getWidth() - _block.getWidth()) / 2, TILE_SIZE);
        _block.setXY(blockX, 0);

        // Add block
        addChild(_block);
        _dropFast = false;
    }

    /**
     * Returns the next block with option to reset.
     */
    public Block getNextBlock(boolean doReset)
    {
        Block nextBlock = _nextBlock;
        if (doReset) {
            _nextBlock = Block.getRandomBlock();
            firePropChange(NextBlock_Prop, nextBlock, _nextBlock);
        }

        // Return
        return nextBlock;
    }

    /**
     * Called when timer fires.
     */
    private void handleTimerFired()
    {
        // If no block, return
        if(_block == null) return;

        // Update block position
        int dy = _dropFast ? 18 : 3;
        _block.setY(_block.getY() + dy);

        // If block stopped, add to stack and start new
        if(isBlockObstructed())
            handleBlockStopped();
    }

    /**
     * Returns whether block is obstructed by stack or bottom.
     */
    private boolean isBlockObstructed()
    {
        // If block intersects stack row, return true
        for (int i = _stackRows.size() - 1; i >= 0; i--) {
            StackRow row = _stackRows.get(i);
            if (row.intersectsBlock(_block))
                return true;
        }

        // If block intersects bottom, return true
        if (MathUtils.gte(_block.getMaxY(), getHeight()))
            return true;

        // Return block not obstructed
        return false;
    }

    /**
     * Called when block hits stack tile or bottom.
     */
    private void handleBlockStopped()
    {
        // Back block up
        while (isBlockObstructed() && _block.getY() > 0)
            _block.setY(_block.getY() - 1);

        // Add stack rows to accommodate block
        topOffStackRowsList();
        if (_gameOver)
            return;
        addBlockToRows();

        // Add new block
        addNewBlock();
    }

    /**
     * Makes sure there are enough stack rows to reach current block y.
     */
    private void topOffStackRowsList()
    {
        while (_stackRows.isEmpty() || _block.getY() + TILE_SIZE / 2 < getTopRow().getY()) {
            addStackRow();
            if (_gameOver)
                return;
        }
    }

    /**
     * Adds a stack row.
     */
    private void addStackRow()
    {
        // If all rows full, it's GameOver
        if (_stackRows.size() >= GRID_HEIGHT - 1) {
            gameOver();
            return;
        }

        // Create new row, position above TopRow and add
        StackRow newRow = new StackRow();
        StackRow topRow = getTopRow();
        double rowY = topRow != null ? topRow.getY() : getHeight();
        rowY -= TILE_SIZE;
        newRow.setY(rowY);
        newRow._rowNum = _stackRows.size();
        _stackRows.add(newRow); addChild(newRow);
    }

    /**
     * Removes stack row (with explosion) and moves rows above down.
     */
    void removeStackRow(StackRow aRow)
    {
        // Cache row index, explode row and remove from Rows list
        int rowIndex = _stackRows.indexOf(aRow);
        new Explode(aRow, 20, 5).play();
        _stackRows.remove(aRow);
        removeChild(aRow);

        // Iterate over rows above and configure to move down
        for (int i = rowIndex; i < _stackRows.size(); i++) {
            StackRow row = _stackRows.get(i);
            row.setY(getHeight() - (i + 1) * TILE_SIZE);
            row.setTransY(row.getTransY() - TILE_SIZE);
            row.getAnimCleared(500).setTransY(0).play();
        }
    }

    /**
     * Adds the current block to rows.
     */
    void addBlockToRows()
    {
        // Get block row/col counts
        int rowCount = _block._pattern.rowCount;

        // Iterate over block rows
        for (int i = 0; i < rowCount; i++) {
            double blockY = _block.getY() + i * TILE_SIZE + TILE_SIZE / 2;
            StackRow row = getRowForY(blockY);
            if (row == null)
                continue;
            row.addBlockTilesToRow(_block);
        }

        // Remove block
        removeChild(_block);

        // If full rows, update score
        int fullRowCount = ListUtils.count(_stackRows, StackRow::isFull);
        if (fullRowCount > 0) {
            int points = switch (fullRowCount) { case 1 -> 300; case 2 -> 500; case 3 -> 500; default -> 800; };
            int newScore = getScore() + points;
            getAnimCleared(500).setValue(Score_Prop, newScore).play();
        }

        // Remove full rows
        for (int i = _stackRows.size() - 1; i >= 0; i--) {
            StackRow row = _stackRows.get(i);
            if (row.isFull())
                removeStackRow(row);
        }
    }

    /**
     * Returns the top row.
     */
    private StackRow getTopRow()  { return !_stackRows.isEmpty() ? _stackRows.get(_stackRows.size() - 1) : null; }

    /**
     * Returns the row for y value.
     */
    private StackRow getRowForY(double aY)
    {
        return ListUtils.findMatch(_stackRows, row -> row.contains(row.getWidth() / 2, aY - row.getY()));
    }

    /**
     * Called when game is over.
     */
    private void gameOver()
    {
        _gameOver = true;
        setTimerRunning(false);

        // Explode rows
        for (int i = 0; i < _stackRows.size(); i++) {
            StackRow row = _stackRows.get(_stackRows.size() - i - 1);
            new Explode(row, 20, 5).playDelayed(i * 150);
        }

        addBlockToRows();

        // Create 'Game Over' label and add
        Label gameOverLabel = new Label("Game Over");
        gameOverLabel.setFont(new Font("Arial Bold", 36));
        gameOverLabel.setTextColor(Color.MAGENTA);
        gameOverLabel.setScale(.1);
        gameOverLabel.setOpacity(0);
        gameOverLabel.setManaged(false);
        gameOverLabel.setLean(Pos.CENTER);
        gameOverLabel.setSizeToPrefSize();
        addChild(gameOverLabel);

        // Animate game over label spin in
        int time = _stackRows.size() * 150;
        gameOverLabel.getAnim(time).getAnim(time + 1200).setScale(1).setOpacity(1).setRotate(360).play();
    }

    /**
     * Handles event.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        switch (anEvent.getKeyCode()) {
            case KeyCode.LEFT -> moveLeft();
            case KeyCode.RIGHT -> moveRight();
            case KeyCode.DOWN -> dropBlock();
            case KeyCode.UP, KeyCode.SPACE -> rotateBlock();
        }
    }

    /**
     * Move Left.
     */
    public void moveLeft()  { _block.moveLeft(); }

    /**
     * Move Right.
     */
    public void moveRight()  { _block.moveRight();}

    /**
     * Drop block.
     */
    public void dropBlock()
    {
        _dropFast = true;

        // Add 2 points for every row dropped
        int rowsLeft = (int) Math.round((getHeight() - _block.getMaxY()) / TILE_SIZE);
        int newScore = getScore() + rowsLeft * 2;
        getAnimCleared(300).setValue(Score_Prop, newScore).play();
    }

    /**
     * Rotate block.
     */
    public void rotateBlock()  { _block.rotateRight(); }

    /**
     * Override to support props for this class.
     */
    @Override
    public Object getPropValue(String propName)
    {
        if (propName == Score_Prop)
            return getScore();
        return super.getPropValue(propName);
    }

    /**
     * Override to support props for this class.
     */
    @Override
    public void setPropValue(String propName, Object aValue)
    {
        if (propName == Score_Prop)
            setScore(Convert.intValue(aValue));
        else super.setPropValue(propName, aValue);
    }
}