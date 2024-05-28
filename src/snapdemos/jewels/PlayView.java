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
     * Returns the gem at given x/y.
     */
    private Gem getGem(int aCol, int aRow)
    {
        if (aCol < 0 || aCol >= GRID_WIDTH || aRow < 0 || aRow >= GRID_HEIGHT)
            return null;
        return _gems[aCol][aRow];
    }

    /**
     * Sets the gem at given x/y.
     */
    private Gem setGem(Gem aGem, int aCol, int aRow)
    {
        Gem oldGem = _gems[aCol][aRow];
        _gems[aCol][aRow] = aGem;

        // Handle null
        if (aGem == null) {
            return oldGem;
        }

        // Handle setting new gem
        aGem.setColRow(aCol, aRow);
        Point pnt = gridToLocal(aCol, aRow);
        aGem.setXY(pnt.x, pnt.y);
        aGem.setTransX(0);
        aGem.setTransY(0);
        return oldGem;
    }

    /**
     * Sets the gem at given x/y.
     */
    private void setGemAnimated(Gem aGem, int aCol, int aRow, int aDelay)
    {
        // Sets gem
        double x0 = aGem != null ? aGem.getX() + aGem.getTransX() : 0;
        double y0 = aGem != null ? aGem.getY() + aGem.getTransY() : 0;
        Gem oldGem = setGem(aGem, aCol, aRow);
        if (aGem == oldGem)
            return;

        // If replacing with null, animate out and remove
        if (aGem == null) {
            snapdemos.shared.Explode.explode(oldGem, oldGem.getImage(), 8, 8, aDelay);
            return;
        }

        // Animate new gem into place
        double x1 = aGem.getX() + aGem.getTransX();
        double y1 = aGem.getY() + aGem.getTransY();
        double dx = x1 - x0;
        double dy = y1 - y0;
        double dist = Math.max(Math.abs(dx), Math.abs(dy));
        aGem.setTransX(-dx);
        aGem.setTransY(-dy);
        int time = (int) Math.round(dist * GEM_SPEED / TILE_SIZE);
        aGem.getAnimCleared(aDelay).getAnim(aDelay + time).setTransX(0).setTransY(0).setLinear().play();
    }

    /**
     * Returns the gem at given x/y.
     */
    private Gem getGemAtXY(double aX, double aY)
    {
        GridXY gridXY = localToGrid(aX, aY);
        return getGem(gridXY.x, gridXY.y);
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
     * Return the point in view coords for point in grid coords.
     */
    public GridXY localToGrid(double aX, double aY)
    {
        int gridX = (int) Math.floor((aX - BORDER_SIZE) / TILE_SIZE);
        int gridY = (int) Math.floor((aY - BORDER_SIZE) / TILE_SIZE);
        return new GridXY(gridX, gridY);
    }

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
                setGemAnimated(null, i, j, ViewUtils.isAltDown() ? 0 : 200);
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
            Gem gem = getGem(aCol, srcRow + i);
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
            setGemAnimated(columnGems[i], aCol, dstRow + i, ViewUtils.isAltDown() ? 0 : 400);
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
                int col = _pressGem.getCol();
                int row = _pressGem.getRow();
                if (Math.abs(move.width) > Math.abs(move.height))
                    col += move.width > 0 ? 1 : -1;
                else row += move.height > 0 ? 1 : -1;
                Gem gem2 = getGem(col, row);
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
            GridXY pnt0 = new GridXY(_pressGem.getCol(), _pressGem.getRow());
            GridXY pnt1 = localToGrid(anEvent.getX(), anEvent.getY());
            int col0 = Math.min(pnt0.x, pnt1.x);
            int row0 = Math.min(pnt0.y, pnt1.y);
            int col1 = Math.max(pnt0.x, pnt1.x);
            int row1 = Math.max(pnt0.y, pnt1.y);
            clearGems(col0, row0, col1, row1);
        }
    }

    /**
     * Swaps two gems.
     */
    public void swapGems(Gem aGem1, Gem aGem2, boolean isSwapBack)
    {
        // Swap the two gems
        int col1 = aGem1.getCol();
        int row1 = aGem1.getRow();
        int col2 = aGem2.getCol();
        int row2 = aGem2.getRow();
        setGemAnimated(aGem1, col2, row2, isSwapBack ? 100 : 0);
        setGemAnimated(aGem2, col1, row1, isSwapBack ? 100 : 0);

        // If original swap, register for swap done
        if (!isSwapBack)
            aGem2.getAnim(0).setOnFinish(() -> swapAnimDone(aGem1, aGem2));
    }

    /**
     * Called when a swap is done.
     */
    private void swapAnimDone(Gem aGem1, Gem aGem2)
    {
        Match match1 = getMatch(aGem1.getCol(), aGem1.getRow());
        Match match2 = getMatch(aGem2.getCol(), aGem2.getRow());
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
                boolean found = checkAndClearMatchAt(i, j);
                if (found) {
                    checkAndClearAllMatchesLater(300);
                    return;
                }
            }
        }
    }

    /**
     * Checks for matches on given gem and clears them.
     */
    private boolean checkAndClearMatchAt(int aCol, int aRow)
    {
        // Get match at col/row (just return if null)
        Match match = getMatch(aCol, aRow);
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
        if (aMatch.dx >= 2)
            clearGems(aMatch.col0, aMatch.row, aMatch.col1, aMatch.row);
        if (aMatch.dy >= 2)
            clearGems(aMatch.col, aMatch.row0, aMatch.col, aMatch.row1);
    }

    /**
     * Returns any match found at given grid xy.
     */
    public Match getMatch(int aCol, int aRow)
    {
        // Get gem at col/row and gem info (just return if null)
        Gem gem = getGem(aCol, aRow);
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
        Gem gem = getGem(aCol, aRow);
        return gem != null ? gem.getId() : -1;
    }

    /**
     * Returns the drag change.
     */
    private Size getDragChange(ViewEvent anEvent)
    {
        Point pnt1 = anEvent.getPoint();
        Point pnt0 = ViewUtils.getMouseDown().getPoint(anEvent.getView());
        Size move = new Size(pnt1.x - pnt0.x, pnt1.y - pnt0.y);
        return move;
    }

    /**
     * A class to represent Grid x/y.
     */
    private static class GridXY {
        public int x, y;

        public GridXY(int aX, int aY)
        {
            x = aX;
            y = aY;
        }

        public String toString()
        {
            return "GridXY: " + x + ',' + y;
        }
    }

    /**
     * A class to represent a match.
     */
    private static class Match {

        // The match col/row and extents
        int col, row, col0, row0, col1, row1, dx, dy;

        /**
         * Creates a Match for given col/row and extents.
         */
        public Match(int aCol, int aRow, int aCol0, int aRow0, int aCol1, int aRow1)
        {
            col = aCol;
            row = aRow;
            col0 = aCol0;
            row0 = aRow0;
            col1 = aCol1;
            row1 = aRow1;
            dx = col1 - col0;
            dy = row1 - row0;
        }
    }
}