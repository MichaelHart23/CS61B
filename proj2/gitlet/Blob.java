package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Utils.join;

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
        Utils.writeObject(f, this);
    }

    public Blob() {}

    public Blob(File file) { //通过文本文件构造Blob
        content = Utils.readContentsAsString(file);
        id = Utils.sha1(file.getName(), content); //文件名也作为判别不同文件的依据
    }

    //for testing
    public Blob(String filename) {
        File f = Utils.join(Repository.CWD, filename);
        content = Utils.readContentsAsString(f);
        id = Utils.sha1(filename, content);
    }

    public static Blob getBlob(File file) { //通过object-blob文件来读取blob对象
        Blob b = Utils.readObject(file, Blob.class);
        return b;
    }

    public static Blob getBlob(String bid) {
        File f = Utils.join(Repository.OBJECTS, bid);
        Blob b = Utils.readObject(f, Blob.class);
        return b;
    }
}
