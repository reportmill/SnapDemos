package snapdemos.snapshow;
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
    private static final String SNAPCODE_PRES = "SnapCodePres.txt";

    /**
     * Constructor.
     */
    public SlidePane(String presentationName)
    {
        super();
        Object source = SlidePane.class.getResource(presentationName);
        if (source != null)
            setSource(source);
    }

    /**
     * Sets the slides from source.
     */
    public void setSource(Object aSource)
    {
        // Get text without
        String text = SnapUtils.getText(aSource); assert (text != null);
        while (text.contains("\n\n")) text = text.replace("\n\n", "\n");
        text = text.replace("    ", "\t");

        // Get items (filter out empty items)
        String[] items = text.split("\n");
        items = ArrayUtils.filter(items, item -> item.trim().length() > 0);
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
        SlideView sview = getSlide(anIndex);
        setSlideView(sview);
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
        setSlideIndex(0);
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