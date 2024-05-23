package snapdemos.facetris;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages state of the player.
 */
public class Player {

    // The Won faces
    private List<Face>  _wonFaces = new ArrayList<>();

    // The Lost Faces
    private List<Face>  _lostFaces = new ArrayList<>();

    /**
     * Returns the faces we've won.
     */
    public List<Face> getWonFaces()
    {
        return _wonFaces;
    }

    /**
     * Add familiar face.
     */
    public void addWonFace(Face aFace)
    {
        _wonFaces.add(aFace);
        aFace.setStatus(Face.Status.Won);
    }

    /**
     * Returns the faces we've lost.
     */
    public List<Face> getLostFaces()
    {
        return _lostFaces;
    }

    /**
     * Add lost face.
     */
    void addLostFace(Face aFace)
    {
        _lostFaces.add(aFace);
        aFace.setStatus(Face.Status.Lost);
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
