package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Blob implements Serializable {
    String content;
    String id;
    public String getID() {
        return id;
    }
    
    public void saveBolb() {
        File f = Utils.join(Repository.OBJECTS, id);
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }

    public Blob() {}
    public Blob(File file) {
        content = Utils.readContentsAsString(file);
        id = Utils.sha1(file.getName(), content); //文件名也作为判别不同文件的依据
    }
    
}
