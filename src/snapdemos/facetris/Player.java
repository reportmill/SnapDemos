package snapdemos.facetris;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages state of the player.
 */
public class Player {

    // The Won faces
    private List<FaceEntry>  _wonFaces = new ArrayList<>();

    // The Lost Faces
    private List<FaceEntry>  _lostFaces = new ArrayList<>();

    /**
     * Returns the faces we've won.
     */
    public List<FaceEntry> getWonFaces()
    {
        return _wonFaces;
    }

    /**
     * Add familiar face.
     */
    public void addWonFace(FaceEntry aFace)
    {
        _wonFaces.add(aFace);
        aFace.setStatus(FaceEntry.Status.Won);
    }

    /**
     * Returns the faces we've lost.
     */
    public List<FaceEntry> getLostFaces()
    {
        return _lostFaces;
    }

    /**
     * Add lost face.
     */
    void addLostFace(FaceEntry aFace)
    {
        _lostFaces.add(aFace);
        aFace.setStatus(FaceEntry.Status.Lost);
   }

    /**
     * Resets all faces lists.
     */
    public void reset()
    {
        _lostFaces.clear();
        _wonFaces.clear();
    }
}
