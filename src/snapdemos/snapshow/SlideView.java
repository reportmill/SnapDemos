package snapdemos.snapshow;
import snap.geom.*;
import snap.gfx.*;
import snap.text.TextLineStyle;
import snap.text.TextStyle;
import snap.util.MDNode;
import snap.view.*;
import snap.viewx.MarkDownView;

import java.util.List;

/**
 * A view to show a slide.
 */
public class SlideView extends ChildView {

    // The SlidePane
    private SlidePane _slidePane;

    // The title view
    private TextArea _titleText;

    // The body view
    private BodyView _bodyView;

    // The page text
    private StringView _pageText;

    // Constants
    private static final double SLIDE_WIDTH = 792;
    private static final double SLIDE_HEIGHT = 612;
    private static final Insets BODY_MARGIN = new Insets(182, 36, 50, 50);
    private static final double BODY_WIDTH = SLIDE_WIDTH - BODY_MARGIN.getWidth();
    private static final double BODY_HEIGHT = SLIDE_HEIGHT - BODY_MARGIN.getHeight();

    // Constants for title text view
    private static final Font TITLE_FONT = new Font("Arial Bold", 64);
    private static final Color TITLE_TEXT_COLOR = Color.WHITE;
    private static final TextStyle TITLE_TEXT_STYLE = TextStyle.DEFAULT.copyForStyleValues(TITLE_FONT, TITLE_TEXT_COLOR);
    private static final Effect TITLE_TEXT_EFFECT = new ShadowEffect(15, Color.BLACK, 2, 2);
    private static final Insets TITLE_TEXT_PADDING = new Insets(5, 5, 15, 5);
    private static final Rect TITLE_VIEW_BOUNDS = new Rect(36, 18, SLIDE_WIDTH - 36 * 2, 150);

    // Constants for body text
    private static final Font BODY_FONT = new Font("Arial", 36);
    private static final Font BODY_FONT2 = BODY_FONT.copyForSize(30);
    private static final Font BODY_FONT3 = BODY_FONT.copyForSize(24);
    private static final TextStyle BODY_TEXT_STYLE = TextStyle.DEFAULT.copyForStyleValues(BODY_FONT);
    private static final TextStyle BODY_TEXT_STYLE2 = BODY_TEXT_STYLE.copyForStyleValues(BODY_FONT2);
    private static final TextStyle BODY_TEXT_STYLE3 = BODY_TEXT_STYLE.copyForStyleValues(BODY_FONT3);
    private static final TextLineStyle BODY_TEXT_LINE_STYLE = TextLineStyle.DEFAULT.copyForPropKeyValue(TextLineStyle.LeftIndent_Prop, 20);

    /**
     * Constructor.
     */
    public SlideView(SlidePane slidePane, String[] lineItems)
    {
        super();
        _slidePane = slidePane;
        setAlignY(VPos.CENTER);
        setPrefSize(SLIDE_WIDTH, SLIDE_HEIGHT);
        setBorder(Color.BLACK, 1);
        enableEvents(MouseRelease);

        // Background for title text
        Label titleView = new Label();
        titleView.setFill(Color.LIGHTBLUE);
        titleView.setBorderRadius(10);
        titleView.setBounds(TITLE_VIEW_BOUNDS);
        addChild(titleView);

        // Create title text area
        _titleText = new TextArea(true);
        _titleText.setWrapLines(true);
        _titleText.setEffect(TITLE_TEXT_EFFECT);
        _titleText.setAlignY(VPos.CENTER);
        _titleText.getTextModel().setAlignX(HPos.CENTER);
        _titleText.setPadding(TITLE_TEXT_PADDING);
        _titleText.setBounds(TITLE_VIEW_BOUNDS);
        addChild(_titleText);

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
        Image badgeImage = slidePane._image;
        if (badgeImage.isLoaded())
            addBadgeImageToSlide();
        else badgeImage.addLoadListener(() -> addBadgeImageToSlide());

        // Set line items
        setItems(lineItems);
    }

    /**
     * Constructor.
     */
    public SlideView(SlidePane slidePane, String markDown)
    {
        this(slidePane, new String[0]);

        MarkDownView markDownView = new MarkDownView();
        markDownView.setMarkDown(markDown);
        List<MDNode> markDownNodes = markDownView.getMarkdownNodes();
        if (!markDownNodes.isEmpty()) {
            MDNode firstMarkdownNode = markDownNodes.get(0);
            if (firstMarkdownNode.getNodeType() == MDNode.NodeType.Header2 || firstMarkdownNode.getNodeType() == MDNode.NodeType.Header1) {
                setTitleText(firstMarkdownNode.getText());
                markDownView.getRootMarkdownNode().removeChildNode(0);
                ViewUtils.removeChild(markDownView, 0);
                ViewUtils.removeChild(markDownView, 0);
                _bodyView.setPadding(Insets.EMPTY);
            }
        }
        _bodyView.addChild(markDownView);
    }

    /**
     * Returns the page number.
     */
    public int getPageNum()
    {
        return (_slidePane._slides.indexOf(this) + 1);
    }

    /**
     * Sets the TitleText.
     */
    public void setTitleText(String aValue)
    {
        _titleText.addCharsWithStyle(aValue, TITLE_TEXT_STYLE);
        _titleText.scaleTextToFit();
    }

    /**
     * Sets the slide text items.
     */
    public void setItems(String[] theItems)
    {
        // Set TitleText
        if (theItems.length > 0)
            setTitleText(theItems[0]);

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
    private static class BodyView extends ColView {

        /**
         * Constructor.
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
            // Get string
            String string = "\u2022 " + anItem.trim();

            // Get indent level
            int indentLevel = 0;
            while (indentLevel < anItem.length() && anItem.charAt(indentLevel) == '\t')
                indentLevel++;

            // Get body text style for indent level
            TextStyle itemTextStyle = switch (indentLevel) {
                case 0 -> BODY_TEXT_STYLE;
                case 1 -> BODY_TEXT_STYLE2;
                default -> BODY_TEXT_STYLE3;
            };

            // Create TextView
            TextArea textArea = new TextArea(true);
            textArea.setWrapLines(true);
            textArea.setMargin(0, 0, 0, indentLevel * 30);
            textArea.setDefaultLineStyle(BODY_TEXT_LINE_STYLE);
            textArea.addCharsWithStyle(string, itemTextStyle);
            addChild(textArea);
        }
    }
}