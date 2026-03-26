package snapdemos.snapshow;
import snap.geom.*;
import snap.gfx.*;
import snap.text.TextLineStyle;
import snap.text.TextStyle;
import snap.util.Interpolator;
import snap.util.ListUtils;
import snap.util.MarkdownNode;
import snap.view.*;
import snap.viewx.MarkdownView;
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

    // The number of fragments in slide (top level list items)
    private int _fragmentCount;

    // The current selected fragment index
    private int _fragmentIndex;

    // The fragment views
    private List<View> _fragmentViews;

    // Constants
    private static final double SLIDE_WIDTH = 792;
    private static final double SLIDE_HEIGHT = 612;
    private static final Insets BODY_MARGIN = new Insets(18, 36, 40, 50);
    private static final double BODY_WIDTH = SLIDE_WIDTH - BODY_MARGIN.getWidth();

    // Constants for title text view
    private static final Font TITLE_FONT = new Font("Arial Bold", 64);
    private static final Color TITLE_TEXT_COLOR = Color.WHITE;
    private static final TextStyle TITLE_TEXT_STYLE = TextStyle.DEFAULT.copyForStyleValues(TITLE_FONT, TITLE_TEXT_COLOR);
    private static final Effect TITLE_TEXT_EFFECT = new ShadowEffect(15, Color.BLACK, 2, 2);
    private static final Insets TITLE_TEXT_PADDING = new Insets(5, 5, 15, 5);
    private static final Size TITLE_TEXT_SIZE = new Size(SLIDE_WIDTH - 36 * 2, 150);

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
        setMinSize(SLIDE_WIDTH, SLIDE_HEIGHT);
        setMaxWidth(SLIDE_WIDTH + 100);
        setBorder(Color.BLACK, 1);
        setFocusable(true);
        setFocusWhenPressed(true);
        enableEvents(MouseRelease);

        // Create title text area
        _titleText = new TextArea(true);
        _titleText.setWrapLines(true);
        _titleText.setEffect(TITLE_TEXT_EFFECT);
        _titleText.setAlignY(VPos.CENTER);
        _titleText.getTextModel().setAlignX(HPos.CENTER);
        _titleText.setPadding(TITLE_TEXT_PADDING);
        _titleText.setPrefSize(TITLE_TEXT_SIZE);
        _titleText.setSize(TITLE_TEXT_SIZE);

        // Background for title text
        BoxView titleBox = new BoxView(_titleText, true, true);
        titleBox.setFill(Color.LIGHTBLUE);
        titleBox.setBorderRadius(10);
        titleBox.setMargin(new Insets(18, 36, 18, 36));
        addChild(titleBox);

        // Create BodyView
        _bodyView = new BodyView();
        _bodyView.setMargin(BODY_MARGIN);
        addChild(_bodyView);

        // Create PageText
        _pageText = new StringView();
        _pageText.setMargin(8, 8, 12, 8);
        _pageText.setLean(Pos.BOTTOM_CENTER);
        addChild(_pageText);

        // Add badge image to slide
        addBadgeImageToSlide();

        // Set line items
        setItems(lineItems);
    }

    /**
     * Constructor.
     */
    public SlideView(SlidePane slidePane, List<MarkdownNode> markdownNodes)
    {
        this(slidePane, new String[0]);

        MarkdownView markdownView = new MarkdownView();
        markdownView.setMarkdownNodes(markdownNodes);
        if (!markdownNodes.isEmpty()) {
            MarkdownNode firstMarkdownNode = markdownNodes.get(0);
            if (firstMarkdownNode.getNodeType() == MarkdownNode.NodeType.Header) {
                setTitleText(firstMarkdownNode.getText());
                markdownView.getDocumentNode().removeChildNode(0);
                ViewUtils.removeChild(markdownView, 0);
                ViewUtils.removeChild(markdownView, 0);
                _bodyView.setPadding(Insets.EMPTY);

                // Configure fragments
                resetSlide();
            }
        }
        _bodyView.addChild(markdownView);

        _fragmentViews = ListUtils.filter(markdownView.getChildren(), child -> isTopLevelListItemView(child));
        _fragmentCount = _fragmentViews.size();
        resetSlide();

        // Add badge image to slide
        addBadgeImageToSlide();
    }

    /**
     * Resets the slide.
     */
    public void resetSlide()
    {
        if (getFragmentCount() > 0)
            setFragmentIndex(-1);
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
                _slidePane.nextFragment();
            else _slidePane.prevFragment();
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
        imageView.setMargin(new Insets(10, 20, 12, 20));
        imageView.setManaged(false);
        imageView.setLean(Pos.BOTTOM_RIGHT);
        //imageView.setEffect(new ShadowEffect(6, Color.BLACK, 1, 1));
        addChild(imageView);
    }

    /**
     * Returns the fragment count.
     */
    public int getFragmentCount()  { return _fragmentCount; }

    /**
     * Returns the fragment index.
     */
    public int getFragmentIndex()  { return _fragmentIndex; }

    /**
     * Sets the fragment index.
     */
    public void setFragmentIndex(int index)
    {
        if (index == getFragmentIndex()) return;
        _fragmentIndex = index;

        // Get
        ParentView markdownView = (ParentView) _bodyView.getChildren().get(0);
        List<View> childViews = markdownView.getChildren();
        View fragmentView = index >= 0 ? _fragmentViews.get(index) : null;
        int fragmentViewChildIndex = fragmentView != null ? childViews.indexOf(fragmentView) : -1;
        View nextFragmentView = index + 1 < _fragmentCount ? _fragmentViews.get(index + 1) : null;
        int nextFragmentViewChildIndex = nextFragmentView != null ? childViews.indexOf(nextFragmentView) : childViews.size();

        // Reset children
        for (int i = 0; i < childViews.size(); i++) {

            // Get view and make sure anim not playing
            View childView = childViews.get(i);
            childView.getAnim(0).finish().clear();

            if (i < fragmentViewChildIndex) {
                childView.setOpacity(1);
                childView.setPickable(true);
                childView.setPaintable(true);
            }
            else if (i >= nextFragmentViewChildIndex) {
                childView.setOpacity(0);
                childView.setPickable(false);
                childView.setPaintable(false);
            }
            else {
                childView.getAnim(500).setInterpolator(Interpolator.EASE_OUT).setOpacity(1).play();
                childView.setPickable(true);
                childView.setPaintable(true);
            }
        }
    }

    /**
     * Returns whether there are more fragments.
     */
    public boolean hasFragments()  { return _fragmentIndex + 1 < _fragmentCount; }

    /**
     * Sets the next fragment.
     */
    public void nextFragment()  { setFragmentIndex(_fragmentIndex + 1); }

    /**
     * Sets the previous fragment.
     */
    public void prevFragment()  { setFragmentIndex(Math.max(_fragmentIndex - 1, 0)); }

    /**
     * Shows all fragments.
     */
    public void showAllFragments()  { setFragmentIndex(_fragmentCount - 1); }

    /**
     * Returns whether given view represents a top level list item node.
     */
    private static boolean isTopLevelListItemView(View aView)
    {
        MarkdownNode markdownNode = MarkdownView.getMarkdownNodeForView(aView);
        return markdownNode != null && markdownNode.getNodeType() == MarkdownNode.NodeType.ListItem && markdownNode.getIndentLevel() == 0;
    }

    /**
     * Override to return column layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new ColViewLayout(this); }

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