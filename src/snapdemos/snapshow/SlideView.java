package snapdemos.snapshow;
import snap.geom.HPos;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.VPos;
import snap.gfx.*;
import snap.text.TextLineStyle;
import snap.view.*;

/**
 * A view to show a slide.
 */
public class SlideView extends ChildView {

    // The SlidePane
    private SlidePane _slidePane;

    // The header view
    private TextArea _headerText;

    // The body view
    private BodyView _bodyView;

    // The page text
    private StringView _pageText;

    // The fonts
    private static final Font TITLE_FONT = new Font("Arial Bold", 64);
    private static final Font BODY_FONT = new Font("Arial", 36);
    private static final Font BODY_FONT2 = BODY_FONT.copyForSize(30);
    private static final Font BODY_FONT3 = BODY_FONT.copyForSize(24);

    // Constants
    private static final double SLIDE_WIDTH = 792;
    private static final double SLIDE_HEIGHT = 612;
    private static final Insets BODY_MARGIN = new Insets(182, 36, 50, 50);
    private static final double BODY_WIDTH = SLIDE_WIDTH - BODY_MARGIN.getWidth();
    private static final double BODY_HEIGHT = SLIDE_HEIGHT - BODY_MARGIN.getHeight();

    /**
     * Constructor for given SlidePane.
     */
    public SlideView(SlidePane aSP, String[] lineItems)
    {
        super();
        _slidePane = aSP;
        setAlignY(VPos.CENTER);
        setPrefSize(SLIDE_WIDTH, SLIDE_HEIGHT);
        setBorder(Color.BLACK, 1);
        enableEvents(MouseRelease);

        // Background for title box
        Label rectView = new Label();
        rectView.setFill(Color.LIGHTBLUE);
        rectView.setBorderRadius(10);
        rectView.setBounds(36, 18, 720, 150);
        addChild(rectView);

        // Create HeaderTextArea
        _headerText = new TextArea();
        _headerText.setWrapLines(true);
        _headerText.setFont(TITLE_FONT); //_headerView.setFill(Color.LIGHTBLUE);
        _headerText.setTextColor(Color.WHITE);
        _headerText.setBorderRadius(10);
        _headerText.setEffect(new ShadowEffect(15, Color.BLACK, 2, 2));
        _headerText.setAlignY(VPos.CENTER);
        _headerText.getTextModel().setAlignX(HPos.CENTER);
        _headerText.setPadding(5, 5, 15, 5);
        _headerText.setBounds(36, 18, 720, 150);
        addChild(_headerText);

        // Create BodyView
        _bodyView = new BodyView();
        _bodyView.setBounds(BODY_MARGIN.left, BODY_MARGIN.top, BODY_WIDTH, BODY_HEIGHT);
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

        // Set line items
        setItems(lineItems);
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
        _headerText.addChars(aValue);
        _headerText.scaleTextToFit();
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
            _bodyView.addItem(item);
        }

        // Shrink body to fit slide
        while (_bodyView.getPrefHeight(BODY_WIDTH) > _bodyView.getHeight()) {
            for (View child : _bodyView.getChildren()) {
                TextArea textArea = (TextArea) child;
                textArea.setFontScale(textArea.getFontScale() - .05);
            }
        }

        // If more than a half inch of space left, add to padding
        if (_bodyView.getPrefHeight(BODY_WIDTH) + 36 < _bodyView.getHeight())
            _bodyView.setPadding(24, 0, 0, 0);
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
            setPickable(false);
        }

        /**
         * Adds a new item.
         */
        public void addItem(String anItem)
        {
            // Get indent level
            int indentLevel = 0;
            while (indentLevel < anItem.length() && anItem.charAt(indentLevel) == '\t')
                indentLevel++;

            // Get string
            String string = "\u2022 " + anItem.trim();

            // Get font
            Font itemFont = indentLevel == 0 ? BODY_FONT : indentLevel == 1 ? BODY_FONT2 : BODY_FONT3;
            double leftMargin = indentLevel * 30;

            // Create TextView
            TextArea textArea = new TextArea();
            textArea.setWrapLines(true);
            textArea.setFont(itemFont);
            textArea.setMargin(0, 0, 0, leftMargin);
            textArea.setDefaultLineStyle(textArea.getDefaultLineStyle().copyForPropKeyValue(TextLineStyle.LeftIndent_Prop, 20));
            textArea.addChars(string);
            addChild(textArea);
        }
    }
}