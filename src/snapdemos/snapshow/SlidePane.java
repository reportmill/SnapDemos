package snapdemos.snapshow;
import java.util.*;
import snap.geom.Insets;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.ShadowEffect;
import snap.util.*;
import snap.view.*;
import snap.viewx.TransitionPane;
import snap.web.RecentFiles;
import snap.web.WebURL;

/**
 * A class to display and manage a list of slides.
 */
public class SlidePane extends ViewOwner {

    // The selected URL
    private WebURL _slideShowUrl;

    // The list of slides
    protected List<SlideView> _slides = new ArrayList<>();

    // The current slide index
    private int _slideIndex = -1;

    // The Slide box
    private TransitionPane _mainBox;

    // The recent files pane
    private RecentFilesPane _recentFilesPane;

    // The image
    protected Image _image = getImage("badge.png");

    // Constants for properties
    public static final String SlideShowUrl_Prop = "SlideShowUrl";

    /**
     * Constructor.
     */
    public SlidePane()
    {
        super();
        _recentFilesPane = new RecentFilesPane(this);
    }

    /**
     * Returns the current slide show URL.
     */
    public WebURL getSlideShowUrl()  { return _slideShowUrl; }

    /**
     * Sets the current slide show URL.
     */
    public void setSlideShowUrl(WebURL slideShowUrl)
    {
        if (Objects.equals(_slideShowUrl, slideShowUrl)) return;
        batchPropChange(SlideShowUrl_Prop, _slideShowUrl, _slideShowUrl = slideShowUrl);
        _slides.clear();
        setSlideView(null);

        // Get text
        String sourceText = SnapUtils.getText(slideShowUrl);
        if (sourceText == null)
            sourceText = "File not found for " + slideShowUrl.getString();

        // Add slides
        if (slideShowUrl.getPath().endsWith(".md"))
            addSlidesForMarkdownString(sourceText);
        else addSlidesForPlainText(sourceText);

        // Install show text
        TextView textView = getView("TextView", TextView.class);
        textView.setBorder(ViewTheme.get().getContentBorder());
        textView.setPadding(new Insets(8));
        textView.setText(sourceText);
        setSlideIndex(0);

        // Set window title
        getWindow().setTitle("SlideShow - " + slideShowUrl.getFilename());
        RecentFiles.addURL(slideShowUrl);
        fireBatchPropChanges();
    }

    /**
     * Returns whether controls are showing.
     */
    public boolean isSlideShowMode()
    {
        SplitView splitView = getUI(SplitView.class);
        return !splitView.getItem(0).isVisible();
    }

    /**
     * Sets whether controls are showing.
     */
    public void setSlideShowMode(boolean aValue)
    {
        if (aValue == isSlideShowMode()) return;
        SplitView splitView = getUI(SplitView.class);
        splitView.setItemVisibleWithAnim(splitView.getItem(0), !aValue);
    }

    /**
     * Adds slides for given plain text string.
     */
    public void addSlidesForPlainText(String text)
    {
        // Get text without
        while (text.contains("\n\n")) text = text.replace("\n\n", "\n");
        text = text.replace("    ", "\t");

        // Get items (filter out empty items)
        String[] items = text.split("\n");
        items = ArrayUtils.filter(items, item -> !item.trim().isBlank());
        int itemIndex = 0;

        // Iterate over items
        while (itemIndex < items.length) {

            // Get end index for next slide (last item index with tab indent)
            int endIndex = itemIndex + 1;
            while (endIndex < items.length && items[endIndex].startsWith("\t"))
                endIndex++;

            // Get slide items and create/add slide
            String[] slideItems = Arrays.copyOfRange(items, itemIndex, endIndex);
            SlideView slideView = new SlideView(this, slideItems);
            _slides.add(slideView);

            // Skip to next slide start
            itemIndex = endIndex;
        }
    }

    /**
     * Adds slides for given markdown string.
     */
    public void addSlidesForMarkdownString(String markdownString)
    {
        // Get items (filter out empty items)
        String[] slideBlocks = markdownString.split("\\s*---\\s*");
        slideBlocks = ArrayUtils.filter(slideBlocks, item -> !item.trim().isBlank());
        int itemIndex = 0;

        // Iterate over items
        for (String slideBlock : slideBlocks) {

            // Skip empty block
            if (slideBlock.trim().isEmpty()) continue;

            // Get slide items and create/add slide
            SlideView slideView = new SlideView(this, slideBlock);
            _slides.add(slideView);
        }
    }

    /**
     * Returns the number of slides.
     */
    public int getSlideCount()  { return _slides.size(); }

    /**
     * Returns the individual slide at given index.
     */
    public SlideView getSlide(int anIndex)  { return _slides.get(anIndex); }

    /**
     * Returns the slide index.
     */
    public int getSlideIndex()  { return _slideIndex; }

    /**
     * Sets the slide index.
     */
    public void setSlideIndex(int anIndex)
    {
        if (anIndex < 0 || anIndex >= getSlideCount()) return;
        _slideIndex = anIndex;
        SlideView slideView = getSlide(anIndex);
        setSlideView(slideView);
    }

    /**
     * Sets the current slide view.
     */
    public void setSlideView(SlideView aSV)
    {
        if (_mainBox == null) return;
        _mainBox.setContent(aSV);
    }

    /**
     * Sets the next slide.
     */
    public void nextSlide()
    {
        _mainBox.setTransition(TransitionPane.MoveRight);
        setSlideIndex(getSlideIndex() + 1);
    }

    /**
     * Sets the previous slide.
     */
    public void prevSlide()
    {
        _mainBox.setTransition(TransitionPane.MoveLeft);
        setSlideIndex(getSlideIndex() - 1);
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        getView("TitleText").setTextColor(new Color(.96));
        getView("TitleText").setEffect(new ShadowEffect(16, Color.BLACK, 2, 2));

        View recentFilesView = getView("RecentFilesView");
        ViewUtils.replaceView(recentFilesView, _recentFilesPane.getUI());

        // Create transition box
        _mainBox = new TransitionPane();
        _mainBox.setFill(Color.WHITE);
        _mainBox.addEventFilter(this::handleMainBoxKeyPressEvent, KeyPress);
        setFirstFocus(_mainBox);

        // Wrap main box in scale box
        ScaleBox scaleBox = getView("ScaleBox", ScaleBox.class);
        scaleBox.setContent(_mainBox);

        addKeyActionFilter("EscapeAction", "ESCAPE");
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        WebURL slideShowUrl = getSlideShowUrl();
        setViewValue("ShowUrlText", slideShowUrl != null ? slideShowUrl.getString() : null);
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        switch (anEvent.getName()) {
            case "ShowUrlText" -> setSlideShowUrl(WebURL.getUrl(anEvent.getStringValue()));
            case "PlayButton" -> setSlideShowMode(true);
            case "EscapeAction" -> setSlideShowMode(false);
        }
    }

    /**
     * Called when main box gets key press event.
     */
    private void handleMainBoxKeyPressEvent(ViewEvent anEvent)
    {
        switch (anEvent.getKeyCode()) {
            case KeyCode.LEFT -> prevSlide();
            case KeyCode.RIGHT -> nextSlide();
        }
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        SlidePane slidePane = new SlidePane();
        slidePane.getWindow().setMaximized(SnapEnv.isWebVM);
        slidePane.setWindowVisible(true);
    }
}