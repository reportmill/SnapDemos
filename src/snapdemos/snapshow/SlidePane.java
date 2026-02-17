package snapdemos.snapshow;
import java.net.URL;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.util.ArrayUtils;
import snap.util.SnapEnv;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.TransitionPane;

/**
 * A class to display and manage a list of slides.
 */
public class SlidePane extends ViewOwner {

    // The list of slides
    protected List<SlideView> _slides = new ArrayList<>();

    // The current slide index
    private int _slideIndex = -1;

    // The Slide box
    private TransitionPane _mainBox;

    // The image
    protected Image _image = getImage("badge.png");

    // Embedded presentations
    private static final String SHOW1 = "Show1.txt";
    private static final String SNAPCODE_PRES = "SnapCodePres.md";

    /**
     * Constructor.
     */
    public SlidePane(String presentationName)
    {
        super();
        URL sourceUrl = SlidePane.class.getResource(presentationName); assert sourceUrl != null;
        String sourceText = SnapUtils.getText(sourceUrl); assert sourceText != null;
        if (sourceUrl.getPath().endsWith(".md"))
            addSlidesForMarkdownString(sourceText);
        else addSlidesForPlainText(sourceText);
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
     * Creates the UI.
     */
    @Override
    protected View createUI()
    {
        _mainBox = new TransitionPane();
        _mainBox.setFill(Color.WHITE);
        _mainBox.addEventFilter(this::handleMainBoxKeyPressEvent, KeyPress);

        // Wrap main box in scale box
        ScaleBox scaleBox = new ScaleBox(_mainBox, true, true);
        scaleBox.setPadding(5, 5, 5, 5);
        scaleBox.setGrowWidth(true);

        // Return
        return scaleBox;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        setFirstFocus(_mainBox);
        setSlideIndex(0);
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
        SlidePane slidePane = new SlidePane(SNAPCODE_PRES);
        slidePane.getWindow().setMaximized(SnapEnv.isWebVM);
        slidePane.setWindowVisible(true);
    }
}