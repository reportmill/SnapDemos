package snapdemos.facetris;
import snap.util.ArrayUtils;
import snap.web.WebURL;
import java.util.*;

/**
 * The Face Index.
 */
public class FaceIndex {

    // The FaceInfos
    private Face[] _faces;

    // The FaceInfo pool
    private List<Face> _facePool;

    // A queue of the next 5 faces
    private Queue<Face> _faceQueue;

    // The names
    private String[] _names;

    // Random
    private Random _random = new Random();

    // The root
    public static String ROOT = "https://reportmill.com/images/Facetris";

    // Shared index
    private static FaceIndex _shared;

    /**
     * Constructor.
     */
    public FaceIndex()
    {
    }

    /**
     * Returns the next face.
     */
    private Face getNextFace()
    {
        List <Face> facePool = getFacePool();
        if (facePool.size() == 0) return null;
        int nextIndex = _random.nextInt(facePool.size());
        return facePool.remove(nextIndex);
    }

    /**
     * Returns the next face.
     */
    public Face getNextFaceFromQueue()
    {
        // Add new face to queue
        addFaceToQueue();

        // Return face from front
        Queue<Face> faceQueue = getNextQueue();
        Face face = faceQueue.poll();
        return face;
    }

    /**
     * Returns the faceInfo pool.
     */
    private List<Face> getFacePool()
    {
        if (_facePool != null) return _facePool;
        Face[] finfos = getFaceInfos();
        List <Face> facePool = new ArrayList<>(finfos.length);
        Collections.addAll(facePool, finfos);
        return _facePool = facePool;
    }

    /**
     * Returns the next queue.
     */
    public Queue<Face> getNextQueue()
    {
        if (_faceQueue != null) return _faceQueue;
        _faceQueue = new ArrayDeque<>();
        for (int i = 0; i < 4; i++)
            addFaceToQueue();
        return _faceQueue;
    }

    /**
     * Adds a new face to queue.
     */
    private void addFaceToQueue()
    {
        Queue<Face> faceQueue = _faceQueue != null ? _faceQueue : getNextQueue();
        Face face = getNextFace();
        if (face != null) {
            faceQueue.add(face);
            face.getView();
        }
    }

    /**
     * Returns the array of FaceInfo.
     */
    public Face[] getFaceInfos()
    {
        // If already set, just return
        if (_faces != null) return _faces;

        // Get index and entries
        String indexText = getIndexText();
        String[] entryStrings = indexText.split("\n");

        // Iterate over entries and add to list
        List<Face> facesList = new ArrayList<>();
        for (String entryStr : entryStrings)
        {
            String filename = entryStr.trim();
            if (filename.length()>0) {
                Face face = new Face(filename);
                facesList.add(face);
            }
        }

        // Return array
        return _faces = facesList.toArray(new Face[0]);
    }

    /**
     * Returns the names.
     */
    public String[] getNames()
    {
        if (_names != null) return _names;
        Face[] faces = getFaceInfos();
        return ArrayUtils.map(faces, face -> face.getName(), String.class);
    }

    /**
     * Returns a name for prefix.
     */
    public String getNameForPrefix(String aPrefix)
    {
        String prefix = aPrefix.toLowerCase();
        String[] names = getNames();
        return ArrayUtils.findMatch(names, name -> name.toLowerCase().startsWith(prefix));
    }

    /**
     * Returns the index text.
     */
    public String getIndexText()
    {
        String indexUrlAddress = ROOT + "/index.txt";
        WebURL indexUrl = WebURL.getURL(indexUrlAddress); assert (indexUrl != null);
        return indexUrl.getText();
    }

    /**
     * Returns the FaceIndex.
     */
    public static FaceIndex getShared()
    {
        if (_shared != null) return _shared;
        return _shared = new FaceIndex();
    }
}
