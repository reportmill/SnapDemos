package snapdemos.snapshow;
import java.util.*;
import snap.gfx.Color;
import snap.gfx.Image;
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

    /**
     * Constructor.
     */
    public SlidePane()
    {
        super();
    }

    /**
     * Sets the slides from source.
     */
    public void setSource(Object aSource)
    {
        String text = SnapUtils.getText(aSource);
        text = text.replace("\n\n", "\n").replace("\n\n", "\n");
        String[] items = text.split("\n");

        int i = 0;
        while (i < items.length && items[i].trim().length() == 0) i++;
        while (i < items.length) {

            // Get range of items for slide
            int start = i, end = i + 1;
            while (end < items.length && items[end].startsWith("\t")) end++;

            // Get slide items and create/add slide
            String[] slideItems = Arrays.copyOfRange(items, start, end);
            SlideView slideView = new SlideView(this);
            slideView.setItems(slideItems);
            _slides.add(slideView);

            // Skip to next slide start
            i = end;
            while (i < items.length && items[i].trim().length() == 0) i++;
        }

        getUI();
        setSlideIndex(0);
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
        ScaleBox box = new ScaleBox(_mainBox, true, true);
        box.setPadding(5, 5, 5, 5);
        box.setGrowWidth(true);
        return box;
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        SlidePane slidePane = new SlidePane();
        slidePane.setSource(SlidePane.class.getResource("Show1.txt"));
        if (SnapUtils.isWebVM)
            slidePane.getWindow().setMaximized(true);
        slidePane.setWindowVisible(true);
    }
}