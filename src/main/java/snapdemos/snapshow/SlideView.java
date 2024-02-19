package snapdemos.snapshow;
import snap.geom.HPos;
import snap.geom.Pos;
import snap.geom.VPos;
import snap.gfx.*;
import snap.view.*;

/**
 * A view to show a slide.
 */
public class SlideView extends ChildView {

    // The SlidePane
    private SlidePane _slidePane;

    // The header view
    private TextArea _headerView;

    // The body view
    private BodyView _bodyView;

    // The page text
    private StringView _pageText;

    // The fonts
    private static final Font TITLE_FONT = Font.Arial14.copyForSize(64).getBold();
    private static final Font BODY_FONT = Font.Arial14.copyForSize(32);

    /**
     * Constructor for given SlidePane.
     */
    public SlideView(SlidePane aSP)
    {
        super();
        _slidePane = aSP;
        setAlignY(VPos.CENTER);
        setPrefSize(792, 612);
        setSize(792, 612);
        setBorder(Color.BLACK, 1);
        enableEvents(MouseRelease);

        // Background for title box
        Label rectView = new Label();
        rectView.setFill(Color.LIGHTBLUE);
        rectView.setBorderRadius(10);
        rectView.setBounds(36, 18, 720, 150);
        addChild(rectView);

        // Create HeaderView
        _headerView = new TextArea();
        _headerView.getTextBlock().setRichText(true);
        _headerView.setAlignY(VPos.CENTER);
        _headerView.getTextBlock().setAlignX(HPos.CENTER);
        _headerView.setBorderRadius(10);
        _headerView.setWrapLines(true);
        _headerView.setFont(TITLE_FONT); //_headerView.setFill(Color.LIGHTBLUE);
        _headerView.setTextFill(Color.WHITE);
        _headerView.setEffect(new ShadowEffect(15, Color.BLACK, 2, 2));
        _headerView.setPadding(5, 5, 15, 5);
        _headerView.setBounds(36, 18, 720, 150);
        addChild(_headerView);

        // Create BodyView
        _bodyView = new BodyView();
        _bodyView.setAlignY(VPos.CENTER);
        _bodyView.setPickable(false);
        _bodyView.setBounds(72, 182, 684, 380);
        addChild(_bodyView);

        // Create PageText
        _pageText = new StringView();
        _pageText.setAlign(Pos.CENTER);
        _pageText.setBounds(350, 580, 92, 20);
        addChild(_pageText);

        // Add badge image to slide
        Image badgeImage = aSP._image;
        if (badgeImage.isLoaded())
            addBadgeImageToSlide();
        else badgeImage.addLoadListener(() -> addBadgeImageToSlide());
    }

    /**
     * Returns the page number.
     */
    public int getPageNum()
    {
        return (_slidePane._slides.indexOf(this) + 1);
    }

    /**
     * Sets the HeaderText.
     */
    public void setHeaderText(String aValue)
    {
        _headerView.addChars(aValue, TITLE_FONT, Color.WHITE);
        _headerView.scaleTextToFit();
    }

    /**
     * Sets the slide text items.
     */
    public void setItems(String[] theItems)
    {
        // Set HeaderText
        if (theItems.length > 0)
            setHeaderText(theItems[0]);

        // Add Body items
        for (int i = 1; i < theItems.length; i++) {
            String item = theItems[i];
            if (item.trim().length() == 0) continue;
            _bodyView.addItem(item);
        }

        while (_bodyView.getPrefHeight(_bodyView.getWidth()) > _bodyView.getHeight()) {
            for (View child : _bodyView.getChildren()) {
                TextArea textArea = (TextArea) child;
                double scale = textArea.getFontScale() - .05;
                textArea.setFontScale(scale);
            }
            _bodyView.relayout();
        }
    }

    /**
     * Handle events.
     */
    protected void processEvent(ViewEvent anEvent)
    {
        // Handle mouse click
        if (anEvent.isMouseClick()) {
            if (anEvent.getX() > getWidth() / 3)
                _slidePane.nextSlide();
            else _slidePane.prevSlide();
        }
    }

    /**
     * Override to set page number.
     */
    protected void setParent(ParentView aPar)
    {
        super.setParent(aPar);
        if (aPar == null) return;
        String pageNumberingString = getPageNum() + " of " + _slidePane.getSlideCount();
        _pageText.setText(pageNumberingString);
    }

    /**
     * Adds the SlidePane badge image to slide (after image is loaded).
     */
    private void addBadgeImageToSlide()
    {
        ImageView imageView = new ImageView(_slidePane._image);
        imageView.setSize(imageView.getPrefSize());
        imageView.setXY(getWidth() - imageView.getWidth() - 10, getHeight() - imageView.getHeight() - 10);
        imageView.setEffect(new ShadowEffect(6, Color.BLACK, 1, 1));
        addChild(imageView);
    }

    /**
     * The view for the body.
     */
    public class BodyView extends ColView {

        /**
         * Creates a new BodyView.
         */
        public BodyView()
        {
            setSpacing(12);
            setFillWidth(true);
        }

        /**
         * Adds a new item.
         */
        public void addItem(String anItem)
        {
            // Get string
            String string = anItem;
            if (string.startsWith("\t\t"))
                string = string.replace("\t\t", "\t\u2022 ");
            else if (string.startsWith("\t"))
                string = string.replace("\t", "\u2022 ");

            // Create TextView
            TextArea text = new TextArea();
            text.setFont(BODY_FONT);
            text.setWrapLines(true);
            text.addChars(string, BODY_FONT);
            addChild(text);
        }
    }
}