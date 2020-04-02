package snapdemos.facetris;
import snap.util.SnapUtils;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The Face Index.
 */
public class FaceIndex {

    // The FaceInfos
    private FaceInfo  _finfos[];

    // The FaceInfo pool
    private List<FaceInfo> _facePool;

    // Random
    private Random _random = new Random();

    // The root
    public static String ROOT = "/Users/jeff/Gamma/Images";

    // Shared index
    private static FaceIndex _shared;

    public FaceIndex()
    {
        if(SnapUtils.isTeaVM)
            ROOT = "http://localhost";
    }

    /**
     * Returns the next face.
     */
    public FaceInfo getNextFace()
    {
        List <FaceInfo> facePool = getFacePool();
        int index = _random.nextInt(facePool.size());
        FaceInfo face = facePool.remove(index);
        return face;
    }

    /**
     * Returns the faceInfo pool.
     */
    private List<FaceInfo> getFacePool()
    {
        if (_facePool!=null) return _facePool;
        FaceInfo finfos[] = getFaceInfos();
        List <FaceInfo> facePool = new ArrayList<>(finfos.length);
        Collections.addAll(facePool, finfos);
        return _facePool = facePool;
    }

    /**
     * Returns the array of FaceInfo.
     */
    public FaceInfo[] getFaceInfos()
    {
        // If already set, just return
        if (_finfos!=null) return _finfos;

        // Get index and entries
        String index = getIndexText();
        String entries[] = index.split("\n");

        // Iterate over entries and add to list
        List<FaceInfo> finfos = new ArrayList<>();
        for (String entry : entries)
        {
            String fname = entry.trim();
            if (fname.length()>0) {
                FaceInfo finfo = new FaceInfo();
                finfo.fname = fname;
                finfo.name = fname.replace(".jpg", "").replace(".JPG","");
                finfos.add(finfo);
            }
        }
        return _finfos = finfos.toArray(new FaceInfo[0]);
    }

    /**
     * Returns the index text.
     */
    public String getIndexText()
    {
        String urls = ROOT + "/Index.txt";
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
