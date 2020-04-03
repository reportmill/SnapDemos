package snapdemos.facetris;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.geom.Rect;
import snap.view.*;

/**
 * A View to show multiple children on top of each other.
 */
public class StackView extends ChildView {

    /**
     * Returns the default alignment.
     */
    public Pos getDefaultAlign()  { return Pos.TOP_LEFT; }

    /**
     * Returns the preferred width.
     */
    protected double getPrefWidthImpl(double aH){ return getPrefWidth(this, null, aH); }

    /**
     * Returns the preferred height.
     */
    protected double getPrefHeightImpl(double aW)  { return getPrefHeight(this, null, aW); }

    /**
     * Layout children.
     */
    protected void layoutImpl()  { layout(this, null, null); }

    /**
     * Returns preferred width of given parent with given children.
     */
    public static final double getPrefWidth(ParentView aPar, View theChildren[], double aH)
    {
        return ColView.getPrefWidth(aPar, null, aH);
    }

    /**
     * Returns preferred height of given parent with given children.
     */
    public static final double getPrefHeight(ParentView aPar, View theChildren[], double aW)
    {
        return RowView.getPrefHeight(aPar, null, aW);
    }

    /**
     * Performs layout for given parent with given children.
     */
    public static void layout(ParentView aPar, View theChilds[], Insets theIns)
    {
        // Get children (just return if empty)
        View children[] = theChilds!=null? theChilds : aPar.getChildrenManaged(); if(children.length==0) return;

        // Get parent bounds for insets
        Insets ins = theIns!=null? theIns : aPar.getInsetsAll();
        double px = ins.left, pw = aPar.getWidth() - ins.getWidth(); if(pw<0) pw = 0; //if(pw<=0) return;
        double py = ins.top, ph = aPar.getHeight() - ins.getHeight(); if(ph<0) ph = 0; //if(ph<=0) return;

        // Get child bounds
        Rect cbnds[] = new Rect[children.length];
        double ax = ViewUtils.getAlignX(aPar);
        double ay = ViewUtils.getAlignY(aPar);

        // Layout children
        for(int i=0,iMax=children.length;i<iMax;i++) { View child = children[i];

            // Get child margin
            Insets marg = child.getMargin();

            // Get child width
            double maxW = Math.max(pw - marg.getWidth(), 0);
            double cw = child.isGrowWidth() ? maxW : Math.min(child.getBestWidth(-1), maxW);

            // Calc x accounting for margin and alignment
            double cx = px + marg.left;
            if(cw<maxW) {
                double ax2 = Math.max(ax,ViewUtils.getLeanX(child));
                cx = Math.max(cx, px + Math.round((pw-cw)*ax2));
            }

            // Get child height
            double maxH = Math.max(ph - marg.getHeight(), 0);
            double ch = child.isGrowHeight() ? maxH : Math.min(child.getBestHeight(-1), maxH);

            // Calc y accounting for margin and alignment
            double cy = py + marg.top;
            if(ch<maxH) {
                double ay2 = Math.max(ay,ViewUtils.getLeanY(child));
                cy = Math.max(cy, py + Math.round((ph-ch)*ay2));
            }

            // Set child bounds
            cbnds[i] = new Rect(cx, cy, cw, ch);
        }

        // Reset child bounds
        for(int i=0;i<children.length;i++) children[i].setBounds(cbnds[i]);
    }
}
