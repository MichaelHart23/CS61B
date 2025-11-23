package gitlet;

import java.io.Serializable;
import java.util.HashMap;

public class Stage implements Serializable {
    HashMap<String, String> addition; //相比于父commit，添加或修改了哪些内容，文件名做键值，id做值
    HashMap<String, String> removal;  //相比于父commit，删除了哪些内容
    public static Stage getStage() {
        Stage s;
        if(Repository.STAGE.length() == 0) { //暂存区为空
            s = new Stage();
            return s;
        }
        s = Utils.readObject(Repository.STAGE, Stage.class);
        return s;
    }

    public boolean hasAddedBlob(String filename, String blobID) {
        return addition.containsKey(filename) && addition.get(filename).equals(blobID);
    }

    public void addItem(String filename, String id) {
        addition.put(filename, id);
        if(removal.containsKey(filename)) {
            removal.remove(filename);
        }
    }

    public void removeFromAddition(String filename) {
        if(addition.containsKey(filename)) {
            addition.remove(filename);
        }
    }

    public void saveStage() {
        Utils.writeObject(Repository.STAGE, this);
    }
}