package snapdemos.facetris;
import snap.util.ArrayUtils;
import snap.web.WebURL;
import java.util.*;

/**
 * The Face Index.
 */
public class FaceIndex {

    // The FaceInfos
    private FaceEntry[] _faceEntries;

    // The FaceInfo pool
    private List<FaceEntry> _facePool;

    // A queue of the next 5 faces
    private Queue<FaceEntry> _faceQueue;

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
    private FaceEntry getNextFace()
    {
        List <FaceEntry> facePool = getFacePool();
        if (facePool.size() == 0) return null;
        int nextIndex = _random.nextInt(facePool.size());
        return facePool.remove(nextIndex);
    }

    /**
     * Returns the next face.
     */
    public FaceEntry getNextFaceFromQueue()
    {
        // Add new face to queue
        addFaceToQueue();

        // Return face from front
        Queue<FaceEntry> faceQueue = getNextQueue();
        FaceEntry face = faceQueue.poll();
        return face;
    }

    /**
     * Returns the faceInfo pool.
     */
    private List<FaceEntry> getFacePool()
    {
        if (_facePool != null) return _facePool;
        FaceEntry[] finfos = getFaceInfos();
        List <FaceEntry> facePool = new ArrayList<>(finfos.length);
        Collections.addAll(facePool, finfos);
        return _facePool = facePool;
    }

    /**
     * Returns the next queue.
     */
    public Queue<FaceEntry> getNextQueue()
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
        Queue<FaceEntry> faceQueue = _faceQueue != null ? _faceQueue : getNextQueue();
        FaceEntry face = getNextFace();
        if (face != null) {
            faceQueue.add(face);
            face.getView();
        }
    }

    /**
     * Returns the array of FaceInfo.
     */
    public FaceEntry[] getFaceInfos()
    {
        // If already set, just return
        if (_faceEntries != null) return _faceEntries;

        // Get index and entries
        String indexText = getIndexText();
        String[] entryStrings = indexText.split("\n");

        // Iterate over entries and add to list
        List<FaceEntry> facesList = new ArrayList<>();
        for (String entryStr : entryStrings)
        {
            String filename = entryStr.trim();
            if (filename.length()>0) {
                FaceEntry face = new FaceEntry(filename);
                facesList.add(face);
            }
        }

        // Return array
        return _faceEntries = facesList.toArray(new FaceEntry[0]);
    }

    /**
     * Returns the names.
     */
    public String[] getNames()
    {
        if (_names != null) return _names;
        FaceEntry[] faces = getFaceInfos();
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
