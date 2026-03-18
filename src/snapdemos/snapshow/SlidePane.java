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
public class SlidePane extends ViewController {

    // Whether to display slides as conventional slide box
    private boolean _slideMode = true;

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
        setSelSlideView(null);

        // Get text URL
        WebURL textUrl = _slideShowUrl;
        if (textUrl.getFileType().isEmpty() && textUrl.getScheme().startsWith("http"))
            textUrl = textUrl.getChildUrlForPath("index.html");

        // Get text
        String fileType = textUrl.getFileType();
        String sourceText = SnapUtils.getText(slideShowUrl);
        if (sourceText == null)
            sourceText = "File not found for " + slideShowUrl.getString();

        // Add slides
        switch (fileType) {
            case "md" -> addSlidesForMarkdownString(sourceText);
            case "html" -> addSlidesForHtmlString(sourceText);
            default -> addSlidesForPlainText(sourceText);
        }

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
        List<MarkdownNode> markdownNodes = new MarkdownParser().parseMarkdownChars(markdownString).getChildNodes();
        addSlidesForMarkdownNodes(markdownNodes);
    }

    /**
     * Adds slides for given markdown nodes.
     */
    public void addSlidesForMarkdownNodes(List<MarkdownNode> markdownNodes)
    {
        List<MarkdownNode> slideNodes = new ArrayList<>();

        // Iterate over markdown nodes and add slide for prior nodes when separator is encountered
        for (MarkdownNode markdownNode : markdownNodes) {

            // If separator, add new slide
            if (markdownNode.getNodeType() == MarkdownNode.NodeType.Separator) {
                SlideView slideView = new SlideView(this, slideNodes);
                _slides.add(slideView);
                slideNodes.clear();
            }

            // Otherwise add node
            else slideNodes.add(markdownNode);
        }

        // Create last slide
        if (!slideNodes.isEmpty()) {
            SlideView slideView = new SlideView(this, slideNodes);
            _slides.add(slideView);
        }
    }

    /**
     * Adds slides for given HTML string.
     */
    public void addSlidesForHtmlString(String markdownString)
    {
        HtmlParser htmlParser = new HtmlParser();
        htmlParser.setHtmlSourceUrl(_slideShowUrl);
        htmlParser.parseHtmlString(markdownString);
        List<MarkdownNode> markdownNodes = htmlParser.getMarkdownNodesForHtml();
        addSlidesForMarkdownNodes(markdownNodes);
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
        setSelSlideView(slideView);
    }

    /**
     * Returns whether a slide is set.
     */
    public boolean isSlideSet()  { return getSelSlideView() != null; }

    /**
     * Returns the selected slide.
     */
    public SlideView getSelSlideView()  { return (SlideView) _mainBox.getContent(); }

    /**
     * Sets the current slide view.
     */
    public void setSelSlideView(SlideView slideView)
    {
        if (_mainBox == null) return;
        _mainBox.setContent(slideView);
        if (slideView != null) {
            slideView.resetSlide();
            slideView.requestFocus();
        }
    }

    /**
     * Sets the next fragment or slide.
     */
    public void nextFragment()
    {
        SlideView slideView = getSelSlideView();
        if (slideView.hasFragments())
            slideView.nextFragment();
        else nextSlide();
    }

    /**
     * Sets the previous fragment or slide.
     */
    public void prevFragment()
    {
        SlideView slideView = getSelSlideView();
        if (slideView.getFragmentIndex() > 0)
            slideView.prevFragment();
        else prevSlide();
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
        SlideView slideView = getSelSlideView();
        if (slideView.getFragmentCount() > 0)
            slideView.setFragmentIndex(slideView.getFragmentCount() - 1);
    }

    /**
     * Returns whether show is in slide mode (vs scroll mode).
     */
    public boolean isSlideMode()  { return _slideMode; }

    /**
     * Sets whether show is in slide mode (vs scroll mode).
     */
    public void setSlideMode(boolean aValue)
    {
        if (aValue == isSlideMode()) return;
        _slideMode = aValue;

        SplitView splitView = getUI(SplitView.class);

        // Handle SlideMode
        if (aValue) {
            ScrollView scrollView = (ScrollView) splitView.getItem(1);
            ScaleBox scaleBox = new ScaleBox(_mainBox, true, true);
            scaleBox.setPadding(8, 8, 8, 8);
            scaleBox.setGrowWidth(true);
            scaleBox.setPrefWidth(scrollView.getPrefWidth());
            splitView.removeItem(scrollView);
            splitView.addItem(scaleBox);
            _mainBox.setGrowHeight(false);
        }

        // Handle page mode
        else {
            ScaleBox scaleBox = (ScaleBox) splitView.getItem(1);
            ScrollView scrollView = new ScrollView(_mainBox);
            scrollView.setGrowWidth(true);
            scrollView.setPrefWidth(scaleBox.getPrefWidth());
            splitView.removeItem(scaleBox);
            splitView.addItem(scrollView);
            _mainBox.setScale(1);
            _mainBox.setGrowHeight(true);
        }

        _mainBox.requestFocus();
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

        setViewVisible("SlideModeCheckBox", false);

        addKeyActionFilter("EscapeAction", "ESCAPE");
    }

    /**
     * Initialize window.
     */
    @Override
    protected void initWindow(WindowView aWindow)
    {
        if (SnapEnv.isWebVM || SnapEnv.isTeaVM) {
            aWindow.setType(WindowView.Type.PLAIN);
            aWindow.setMaximized(true);
        }
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        WebURL slideShowUrl = getSlideShowUrl();
        setViewValue("ShowUrlText", slideShowUrl != null ? slideShowUrl.getString() : null);
        setViewValue("SlideModeCheckBox", isSlideMode());
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
            case "SlideModeCheckBox" -> setSlideMode(anEvent.getBoolValue());
            case "EscapeAction" -> setSlideShowMode(false);
        }
    }

    /**
     * Called when main box gets key press event.
     */
    private void handleMainBoxKeyPressEvent(ViewEvent anEvent)
    {
        if (!isSlideSet()) return;
        boolean eventHandled = true;
        SlideView slideView = getSelSlideView();

        switch (anEvent.getKeyCode()) {
            case KeyCode.LEFT -> prevFragment();
            case KeyCode.RIGHT, KeyCode.SPACE -> nextFragment();
            case KeyCode.HOME -> setSlideIndex(0);
            case KeyCode.END ->  {
                setSlideIndex(getSlideCount() - 1);
                getSelSlideView().showAllFragments();
            }
            case KeyCode.PAGE_UP, KeyCode.UP -> prevSlide();
            case KeyCode.PAGE_DOWN, KeyCode.DOWN -> {
                if (slideView.getFragmentCount() == 0 || slideView.getFragmentIndex() == slideView.getFragmentCount() - 1)
                    nextSlide();
                else slideView.showAllFragments();
            }
            default -> eventHandled = false;
        }

        if (eventHandled)
            anEvent.consume();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)  { ViewUtils.runLater(() -> runApp(args)); }

    /**
     * Runs the app.
     */
    public static void runApp(String[] args)
    {
        SlidePane slidePane = new SlidePane();
        slidePane.setWindowVisible(true);

        if (args.length > 0) {
            String arg = args[0];
            if (arg.startsWith("https"))
                slidePane.setSlideShowUrl(WebURL.getUrl(arg));
        }
    }
}