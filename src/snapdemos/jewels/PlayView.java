package snapdemos.jewels;
import snap.geom.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A View to hold the play area for a match 3 game.
 */
public class PlayView extends ParentView {

    // The gems
    private Gem[][] _gems = new Gem[GRID_WIDTH][GRID_HEIGHT];

    // The last gem hit by mouse press
    private Gem _pressGem;

    // The grid width/height
    public static int GRID_WIDTH = 8;
    public static int GRID_HEIGHT = 8;
    public static int TILE_SIZE = 64;
    public static int BORDER_SIZE = 2;
    public static int GEM_SPEED = 140;

    /**
     * Constructor.
     */
    public PlayView()
    {
        // Set background texture
        Image backgroundImage = Image.getImageForClassResource(PlayView.class, "pkg.images/Cloth.jpg"); assert (backgroundImage != null);
        ImagePaint imagePaint = new ImagePaint(backgroundImage, BORDER_SIZE, BORDER_SIZE, 64, 64, false);
        backgroundImage.addLoadListener(() -> setFill(imagePaint));

        // Set PlayView fill, border, PrefSize
        setBorder(Color.BLACK, 2);
        setPrefSize(GRID_WIDTH * TILE_SIZE + BORDER_SIZE * 2, GRID_HEIGHT * TILE_SIZE + BORDER_SIZE * 2);
        setClipToBounds(true);
        enableEvents(MousePress, MouseDrag, MouseRelease);
    }

    /**
     * Starts the game.
     */
    public void startGame()
    {
        reloadGems();
    }

    /**
     * Pauses game.
     */
    public void pauseGame()
    {
    }

    /**
     * Returns the gem at given grid x/y.
     */
    private Gem getGemAtGridXY(int gridX, int gridY)
    {
        if (gridX < 0 || gridX >= GRID_WIDTH || gridY < 0 || gridY >= GRID_HEIGHT)
            return null;
        return _gems[gridX][gridY];
    }

    /**
     * Sets the gem at given grid X/Y.
     */
    private void setGemAtGridXY(Gem aGem, int gridX, int gridY)
    {
        // Set gem at grid X/Y (just return if null)
        _gems[gridX][gridY] = aGem;
        if (aGem == null)
            return;

        // Handle setting new gem
        aGem.setGridXY(gridX, gridY);
        Point viewXY = gridToLocal(gridX, gridY);
        aGem.setXY(viewXY.x, viewXY.y);
        aGem.setTransX(0);
        aGem.setTransY(0);
    }

    /**
     * Sets the gem at given x/y.
     */
    private void setGemAtGridXYAnimated(Gem aGem, int gridX, int gridY, int aDelay)
    {
        // If gem already set, just return
        Gem oldGem = getGemAtGridXY(gridX, gridY);
        if (aGem == oldGem)
            return;

        // Cache old gem X/Y
        double oldX = aGem != null ? aGem.getX() + aGem.getTransX() : 0;
        double oldY = aGem != null ? aGem.getY() + aGem.getTransY() : 0;

        // Set gem at grid X/Y
        setGemAtGridXY(aGem, gridX, gridY);

        // If replacing with null, animate out and remove
        if (aGem == null) {
            snapdemos.shared.Explode.explode(oldGem, oldGem.getImage(), 8, 8, aDelay);
            return;
        }

        // Calculate and set translation X/Y so gem appears to be at old X/Y
        double transX = oldX - aGem.getX();
        double transY = oldY - aGem.getY();
        aGem.setTransX(transX);
        aGem.setTransY(transY);

        // Calculate the time to travel to new XY and animate translation back to zero
        double dist = Math.max(Math.abs(transX), Math.abs(transY));
        int travelTime = (int) Math.round(dist * GEM_SPEED / TILE_SIZE);
        aGem.getAnimCleared(aDelay).getAnim(aDelay + travelTime).setTransX(0).setTransY(0).setLinear().play();
    }

    /**
     * Returns the gem at given x/y.
     */
    private Gem getGemAtXY(double aX, double aY)
    {
        int gridX = localToGridX(aX);
        int gridY = localToGridY(aY);
        return getGemAtGridXY(gridX, gridY);
    }

    /**
     * Return the point in view coords for point in grid coords.
     */
    public Point gridToLocal(int aX, int aY)
    {
        int localX = aX * TILE_SIZE + BORDER_SIZE;
        int localY = aY * TILE_SIZE + BORDER_SIZE;
        return new Point(localX, localY);
    }

    /**
     * Return the grid X value for given view X.
     */
    public int localToGridX(double aX)  { return (int) Math.floor((aX - BORDER_SIZE) / TILE_SIZE); }

    /**
     * Return the grid Y value for given view Y.
     */
    public int localToGridY(double aY)  { return (int) Math.floor((aY - BORDER_SIZE) / TILE_SIZE); }

    /**
     * Reloads the gems.
     */
    private void reloadGems()
    {
        // Remove gem views, null out grid gems (to suppress explosion) and clearGems (to reload)
        removeChildren();
        for (int i = 0; i < GRID_WIDTH; i++)
            for (int j = 0; j < GRID_HEIGHT; j++)
                _gems[i][j] = null;
        clearGems(0, 0, GRID_WIDTH - 1, GRID_HEIGHT - 1);

        // Clear unintended matches
        checkAndClearAllMatchesLater(GEM_SPEED * (GRID_HEIGHT + 2));
    }

    /**
     * Clears the gems in rect from given col/row to second col/row.
     */
    private void clearGems(int aCol0, int aRow0, int aCol1, int aRow1)
    {
        // Iterate over columns
        for (int i = aCol0; i <= aCol1; i++) {

            // Iterate over column rows and clear gems
            for (int j = aRow0; j <= aRow1; j++) {
                setGemAtGridXYAnimated(null, i, j, ViewUtils.isAltDown() ? 0 : 200);
            }

            // Copy gems for column from source row
            int srcRow = -(aRow1 - aRow0 + 1);
            int len = aRow1 + 1;
            copyColumnGems(i, srcRow, 0, len);
        }
    }

    /**
     * Copies the gems in rect from given col/row to second col/row.
     */
    private void copyColumnGems(int aCol, int srcRow, int dstRow, int aLen)
    {
        Gem[] columnGems = new Gem[aLen];

        // Get source gems (create/position/add if above bounds)
        for (int i = 0; i < aLen; i++) {
            Gem gem = getGemAtGridXY(aCol, srcRow + i);
            if (gem == null) {
                gem = new Gem();
                Point gemXY = gridToLocal(aCol, srcRow + i);
                gem.setXY(gemXY.x, gemXY.y);
                addChild(gem);
            }
            columnGems[i] = gem;
        }

        // Set gems
        for (int i = 0; i < aLen; i++) {
            setGemAtGridXYAnimated(columnGems[i], aCol, dstRow + i, ViewUtils.isAltDown() ? 0 : 400);
        }
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle MouseDown
        if (anEvent.isMousePress())
            _pressGem = getGemAtXY(anEvent.getX(), anEvent.getY());

        // Handle alt events
        else if (anEvent.isAltDown())
            processEventAlt(anEvent);

        // Handle MouseDrag
        else if (_pressGem != null && anEvent.isMouseDrag()) {
            Size move = getDragChange(anEvent);
            if (Math.abs(move.width) > 5 || Math.abs(move.height) > 5) {
                int gridX = _pressGem.getGridX();
                int gridY = _pressGem.getGridY();
                if (Math.abs(move.width) > Math.abs(move.height))
                    gridX += move.width > 0 ? 1 : -1;
                else gridY += move.height > 0 ? 1 : -1;
                Gem gem2 = getGemAtGridXY(gridX, gridY);
                if (gem2 == null)
                    return;

                // Swap gems
                swapGems(_pressGem, gem2, false);
                _pressGem = null;
            }
        }
    }

    /**
     * Testing method - just explodes selected gem range on MouseRelease.
     */
    private void processEventAlt(ViewEvent anEvent)
    {
        // Handle MouseRelease
        if (anEvent.isMouseRelease() && _pressGem != null) {
            int pressGridX = _pressGem.getGridX();
            int pressGridY = _pressGem.getGridY();
            int mouseGridX = localToGridX(anEvent.getX());
            int mouseGridY = localToGridY(anEvent.getY());
            int gridX0 = Math.min(pressGridX, mouseGridX);
            int gridY0 = Math.min(pressGridY, mouseGridY);
            int gridX1 = Math.max(pressGridX, mouseGridX);
            int gridY1 = Math.max(pressGridY, mouseGridY);
            clearGems(gridX0, gridY0, gridX1, gridY1);
        }
    }

    /**
     * Swaps two gems.
     */
    public void swapGems(Gem aGem1, Gem aGem2, boolean isSwapBack)
    {
        // Swap the two gems
        int col1 = aGem1.getGridX();
        int row1 = aGem1.getGridY();
        int col2 = aGem2.getGridX();
        int row2 = aGem2.getGridY();
        setGemAtGridXYAnimated(aGem1, col2, row2, isSwapBack ? 100 : 0);
        setGemAtGridXYAnimated(aGem2, col1, row1, isSwapBack ? 100 : 0);

        // If original swap, register for swap done
        if (!isSwapBack)
            aGem2.getAnim(0).setOnFinish(() -> swapAnimDone(aGem1, aGem2));
    }

    /**
     * Called when a swap is done.
     */
    private void swapAnimDone(Gem aGem1, Gem aGem2)
    {
        Match match1 = getMatchForGemAtGridXY(aGem1.getGridX(), aGem1.getGridY());
        Match match2 = getMatchForGemAtGridXY(aGem2.getGridX(), aGem2.getGridY());
        if (match1 == null && match2 == null) {
            ViewUtils.runLater(() -> swapGems(aGem1, aGem2, true));
            return;
        }

        // Clear matches
        if (match1 != null)
            clearMatch(match1);
        if (match2 != null)
            clearMatch(match2);

        // Check and clear all matches
        checkAndClearAllMatchesLater(300);
    }

    /**
     * Checks for matches on all gems and clears them.
     */
    private void checkAndClearAllMatchesLater(int aDelay)
    {
        ViewUtils.runDelayed(() -> checkAndClearMatches(), aDelay);
    }

    /**
     * Checks for matches on all gems and clears them.
     */
    private void checkAndClearMatches()
    {
        // Iterate over grid and check/clear any matches at each point
        for (int i = 0; i < GRID_WIDTH; i++) {
            for (int j = 0; j < GRID_HEIGHT; j++) {
                boolean found = checkAndClearMatchAtGridXY(i, j);
                if (found) {
                    checkAndClearAllMatchesLater(300);
                    return;
                }
            }
        }
    }

    /**
     * Checks for matches on gem at given grid X/Y and clears them.
     */
    private boolean checkAndClearMatchAtGridXY(int gridX, int gridY)
    {
        // Get match at col/row (just return if null)
        Match match = getMatchForGemAtGridXY(gridX, gridY);
        if (match == null)
            return false;
        clearMatch(match);
        return true;
    }

    /**
     * Clear gems for match.
     */
    private void clearMatch(Match aMatch)
    {
        if (aMatch.width() >= 2)
            clearGems(aMatch.gridStartX, aMatch.gridY, aMatch.gridEndX, aMatch.gridY);
        if (aMatch.height() >= 2)
            clearGems(aMatch.gridX, aMatch.gridStartY, aMatch.gridX, aMatch.gridEndY);
    }

    /**
     * Returns any match found for gem at given grid X/Y.
     */
    public Match getMatchForGemAtGridXY(int aCol, int aRow)
    {
        // Get gem at col/row and gem info (just return if null)
        Gem gem = getGemAtGridXY(aCol, aRow);
        if (gem == null)
            return null;
        int gemId = gem.getId();
        int col0 = aCol;
        int row0 = aRow;
        int col1 = aCol;
        int row1 = aRow;

        // Iterate left/right and up/down to find matching gem extents
        for (int i = aCol - 1; i >= 0; i--)
            if (getGemId(i, aRow) == gemId)
                col0--;
            else break;
        for (int i = aCol + 1; i < GRID_WIDTH; i++)
            if (getGemId(i, aRow) == gemId)
                col1++;
            else break;
        for (int i = aRow - 1; i >= 0; i--)
            if (getGemId(aCol, i) == gemId)
                row0--;
            else break;
        for (int i = aRow + 1; i < GRID_HEIGHT; i++)
            if (getGemId(aCol, i) == gemId)
                row1++;
            else break;

        // If extends exceed 2 in either direction, return match
        int dx = col1 - col0;
        if (dx < 2)
            col0 = col1 = aCol;
        int dy = row1 - row0;
        if (dy < 2)
            row0 = row1 = aRow;
        if (dx < 2 && dy < 2)
            return null;
        return new Match(aCol, aRow, col0, row0, col1, row1);
    }

    /**
     * Returns whether gem at col/row matches id.
     */
    private int getGemId(int aCol, int aRow)
    {
        Gem gem = getGemAtGridXY(aCol, aRow);
        return gem != null ? gem.getId() : -1;
    }

    /**
     * Returns the drag change.
     */
    private Size getDragChange(ViewEvent anEvent)
    {
        Point pnt0 = ViewUtils.getMouseDown().getPoint(anEvent.getView());
        Point pnt1 = anEvent.getPoint();
        return new Size(pnt1.x - pnt0.x, pnt1.y - pnt0.y);
    }

    /**
     * A class to represent a match.
     */
    private static class Match {

        // The match grid X/Y
        int gridX, gridY;

        // The match extents
        int gridStartX, gridStartY, gridEndX, gridEndY;

        /**
         * Creates a Match for given col/row and extents.
         */
        public Match(int aGridX, int aGridY, int aGridStartX, int aGridStartY, int aGridEndX, int aGridEndY)
        {
            gridX = aGridX;
            gridY = aGridY;
            gridStartX = aGridStartX;
            gridStartY = aGridStartY;
            gridEndX = aGridEndX;
            gridEndY = aGridEndY;
        }

        int width() { return gridEndX - gridStartX; }
        int height() { return gridEndY - gridStartY; }
    }
}