package snapdemos.facetris;
import snap.gfx.GFXEnv;
import snap.util.SnapUtils;
import snap.web.WebURL;
import java.util.*;

/**
 * The Face Index.
 */
public class FaceIndex {

    // The FaceInfos
    private Face  _finfos[];

    // The FaceInfo pool
    private List<Face>  _facePool;

    // A queue of the next 5 faces
    private Queue<Face>  _faceQueue;

    // The names
    private String  _names[];

    // Random
    private Random  _random = new Random();

    // The root
    public static String ROOT = "/Users/jeff/Gamma/Images";

    // Shared index
    private static FaceIndex _shared;

    public FaceIndex()
    {
        if(SnapUtils.isTeaVM)
            ROOT = GFXEnv.getEnv().getClassRoot() + "/Images";
    }

    /**
     * Returns the next face.
     */
    private Face getNextFace()
    {
        List <Face> facePool = getFacePool();
        if (facePool.size()==0) return null;
        int index = _random.nextInt(facePool.size());
        return facePool.remove(index);
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
        if (_facePool!=null) return _facePool;
        Face finfos[] = getFaceInfos();
        List <Face> facePool = new ArrayList<>(finfos.length);
        Collections.addAll(facePool, finfos);
        return _facePool = facePool;
    }

    /**
     * Returns the next queue.
     */
    public Queue<Face> getNextQueue()
    {
        if (_faceQueue!=null) return _faceQueue;
        _faceQueue = new ArrayDeque<>();
        for (int i=0; i<4; i++) addFaceToQueue();
        return _faceQueue;
    }

    /**
     * Adds a new face to queue.
     */
    private void addFaceToQueue()
    {
        Queue<Face> faceQueue = _faceQueue!=null ? _faceQueue : getNextQueue();
        Face face = getNextFace();
        if (face!=null) {
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
        if (_finfos!=null) return _finfos;

        // Get index and entries
        String index = getIndexText();
        String entries[] = index.split("\n");

        // Iterate over entries and add to list
        List<Face> finfos = new ArrayList<>();
        for (String entry : entries)
        {
            String fname = entry.trim();
            if (fname.length()>0) {
                Face finfo = new Face(fname);
                finfos.add(finfo);
            }
        }
        return _finfos = finfos.toArray(new Face[0]);
    }

    /**
     * Returns the names.
     */
    public String[] getNames()
    {
        if (_names!=null) return _names;
        Face faces[] = getFaceInfos();
        List<String> names = new ArrayList<>(faces.length);
        for (Face face : faces)
            names.add(face.getName());
        return _names = names.toArray(new String[0]);
    }

    /**
     * Returns a name for prefix.
     */
    public String getNameForPrefix(String aPrefix)
    {
        String prefix = aPrefix.toLowerCase();
        String names[] = getNames();
        for (String name : names)
            if (name.toLowerCase().startsWith(prefix))
                return name;
        return null;
    }

    /**
     * Returns the index text.
     */
    public String getIndexText()
    {
        String urls = ROOT + "/index.txt";
        WebURL url = WebURL.getURL(urls);
        String text = url.getText();
        return text;
    }

    /**
     * Returns the FaceIndex.
     */
    public static FaceIndex get()
    {
        if (_shared!=null) return _shared;
        return _shared = new FaceIndex();
    }
}
