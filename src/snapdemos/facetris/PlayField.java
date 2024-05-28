package snapdemos.facetris;
import java.util.ArrayList;
import java.util.List;

/**
 * A Class to manage the faces for PlayView.
 */
public class PlayField {

    // The Faces
    private List<FaceEntry>  _fieldFaces = new ArrayList<>();

    // The falling Faces
    private List<FaceEntry>  _fallFaces = new ArrayList<>();

    /**
     * Returns the first face still in play.
     */
    public FaceEntry getMainFace()
    {
        for (FaceEntry face : getFallFaces())
            if (face.inPlay())
                return face;
        return null;
    }

    /**
     * Returns the active faces.
     */
    public List<FaceEntry> getFieldFaces()
    {
        return _fieldFaces;
    }

    /**
     * Adds a face.
     */
    protected void addFieldFace(FaceEntry aFace)
    {
        _fieldFaces.add(aFace);
        _fallFaces.add(aFace);
    }

    /**
     * Removes a face.
     */
    protected void removeFieldFace(FaceEntry aFace)
    {
        _fieldFaces.remove(aFace);
        removeFallFace(aFace);
    }

    /**
     * Returns the falling faces.
     */
    public List<FaceEntry> getFallFaces()  { return _fallFaces; }

    /**
     * Removes a face.
     */
    protected void removeFallFace(FaceEntry aFace)
    {
        _fallFaces.remove(aFace);
    }

    /**
     * Resets all faces lists.
     */
    public void reset()
    {
        _fieldFaces.clear();
        _fallFaces.clear();
    }
}
