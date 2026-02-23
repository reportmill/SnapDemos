package snapdemos.snapshow;
import snap.geom.HPos;
import snap.gfx.*;
import snap.util.ListUtils;
import snap.view.*;
import snap.web.RecentFiles;
import snap.web.WebURL;
import java.util.List;

/**
 * A UI panel to show recent files.
 */
public class RecentFilesPane extends ViewOwner {

    // The slide pane
    private SlidePane _slidePane;

    // Recent files list view
    private ColView _recentFilesListView;

    // The default recent file url
    private static final WebURL DEFAULT_URL1 = WebURL.createUrl("https://reportmill.com/SlideShow/samples/SamplePres.md");
    private static final WebURL DEFAULT_URL2 = WebURL.createUrl("https://reportmill.com/SlideShow/samples/JavaOne.md");

    // Constants
    private static final Color BUTTON_BACKGROUND_COLOR = Color.get("#F8F8FC");
    private static final Effect LINK_HIGHLIGHT_EFFECT = new ShadowEffect(10, Color.get("#8888FF"), 0, 0);

    /**
     * Constructor.
     */
    public RecentFilesPane(SlidePane slidePane)
    {
        _slidePane = slidePane;
        RecentFiles.setPrefsKey("RecentSlideShows");
        _slidePane.addPropChangeListener(pc -> rebuildRecentFilesListView(), SlidePane.SlideShowUrl_Prop);
    }

    /**
     * Returns the Recent files URLs.
     */
    public List<WebURL> getRecentFilesUrls()
    {
        List<WebURL> recentFileUrls = RecentFiles.getUrls();
        if (recentFileUrls.size() > 8)
            recentFileUrls = recentFileUrls.subList(0, 8);
        if (recentFileUrls.isEmpty()) {
            recentFileUrls = List.of(DEFAULT_URL1, DEFAULT_URL2);
            recentFileUrls.forEach(RecentFiles::addURL);
        }
        return recentFileUrls;
    }

    /**
     * Opens a recent file URL.
     */
    public void openRecentFileUrl(WebURL recentFileUrl)
    {
        _slidePane.setSlideShowUrl(recentFileUrl);
        rebuildRecentFilesListView();
        _slidePane.resetLater();
    }

    /**
     * Removes a recent file URL.
     */
    public void removeRecentFileUrl(WebURL recentFileUrl)
    {
        RecentFiles.removeURL(recentFileUrl);
        rebuildRecentFilesListView();
    }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()
    {
        _recentFilesListView = new ColView();
        _recentFilesListView.setMargin(8, 8, 8, 8);
        _recentFilesListView.setGrowWidth(true);
        return _recentFilesListView;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        rebuildRecentFilesListView();
    }

    /**
     * Rebuilds the recent files list.
     */
    private void rebuildRecentFilesListView()
    {
        // Get recent files and create views
        List<WebURL> recentFileUrls = getRecentFilesUrls();
        List<View> recentFileViews = ListUtils.map(recentFileUrls, this::createViewForRecentFileUrl);

        // Reset children
        _recentFilesListView.setChildren(recentFileViews.toArray(new View[0]));

        // If no files, add label
        if (recentFileUrls.isEmpty()) {
            Label noRecentFilesLabel = new Label("(No recent files)");
            noRecentFilesLabel.setPropsString("Font:Arial Italic 16; Margin:5");
            noRecentFilesLabel.setTextColor(Color.GRAY);
            _recentFilesListView.addChild(noRecentFilesLabel);
        }
    }

    /**
     * Creates a view for recent file url.
     */
    private ChildView createViewForRecentFileUrl(WebURL recentFileUrl)
    {
        // Create label for name
        Label nameLabel = new Label(recentFileUrl.getFilenameSimple());
        nameLabel.setPropsString("Font:Arial 13; MinWidth: 120");

        // Create separator
        Separator separator = new Separator();
        separator.setVertical(true);
        separator.setPrefSize(28, 14);

        // Create label for address
        String projectSourceAddress = recentFileUrl.getParent().getString();
        Label addressLabel = new Label(projectSourceAddress);
        addressLabel.setPropsString("Font:Arial 11");
        addressLabel.setGrowWidth(true);
        addressLabel.setClipToBounds(true);

        // Create close box
        CloseBox closeBox = new CloseBox();
        closeBox.setMargin(5, 8, 5, 5);
        closeBox.setLeanX(HPos.RIGHT);
        closeBox.setVisible(false);
        closeBox.addEventHandler(e -> removeRecentFileUrl(recentFileUrl), Action);

        // Create recent file view and add children
        RowView recentFileView = new RowView();
        recentFileView.setFill(BUTTON_BACKGROUND_COLOR);
        recentFileView.setPropsString("Fill:#F0; BorderRadius:5; Margin:5; Padding:5;");
        recentFileView.setCursor(Cursor.HAND);
        recentFileView.setGrowWidth(true);
        recentFileView.setChildren(nameLabel, separator, addressLabel, closeBox);

        // Configure click action
        recentFileView.addEventHandler(e -> openRecentFileUrl(recentFileUrl), MouseRelease);
        recentFileView.addEventFilter(this::handleRecentFileViewMouseEnterAndExitEvents, MouseEnter, MouseExit);

        // Return
        return recentFileView;
    }

    /**
     * Called when recent file view has mouse enter/exit.
     */
    private void handleRecentFileViewMouseEnterAndExitEvents(ViewEvent anEvent)
    {
        // Do normal version
        if (anEvent.isMouseEnter())
            anEvent.getView().setEffect(LINK_HIGHLIGHT_EFFECT);
        else if (anEvent.isMouseExit())
            anEvent.getView().setEffect(null);

        // Set close box visible on mouse enter
        RowView recentFileView = anEvent.getView(RowView.class);
        recentFileView.getChild(3).setVisible(anEvent.isMouseEnter());
    }
}
