package data;

import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AppState implements Serializable {

    private static final Logger LOG = Logger.getLogger(AppState.class.getName());
    private static final String PATH = "app.state";
    public static final String KEYAPP = "state";
    public static final String KEYHEIGHT = "height";


    public static boolean exists(){
        Path path = Paths.get(PATH);
        if (Files.exists(path))
            return true;
        else
            return false;
    }

    public static void saveAppState(ByteString appState, long height){
        try{
            HashMap<String, Object> hmap = new HashMap<>();
            hmap.put(KEYAPP, appState);
            hmap.put(KEYHEIGHT, height);
            FileOutputStream fos = new FileOutputStream(PATH);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(hmap);
            oos.close();
            fos.close();
        } catch (IOException e){
            LOG.log(Level.INFO, "Error saving Appstate: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> loadAppState() {
        HashMap<String, Object> hmap = new HashMap<>();
        Path path = Paths.get(PATH);
        if (Files.exists(path)) {
            try {
                FileInputStream fis = new FileInputStream(PATH);
                ObjectInputStream ois = new ObjectInputStream(fis);
                hmap = (HashMap<String, Object>) ois.readObject();
                return hmap;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            LOG.log(Level.INFO, "File does not exist");
        }
        return hmap;
    }

}
