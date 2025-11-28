package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage implements Serializable {
    HashMap<String, String> addition; //相比于父commit，添加或修改了哪些内容，文件名做键值，id做值
    HashMap<String, String> removal;  //相比于父commit，删除了哪些内容
    public Stage() {
        addition = new HashMap<>();
        removal = new HashMap<>();
    }
    public static Stage getStage() { //返回当前的暂存区
        Stage s;
        if (Repository.STAGE.length() == 0) { //暂存区为空
            s = new Stage();
            return s;
        }
        s = Utils.readObject(Repository.STAGE, Stage.class);
        return s;
    }

    public void stageStatus() {
        System.out.println("=== Staged Files ===");
        List<String> listForAddition = new ArrayList<>();
        for (Map.Entry<String, String> entry : addition.entrySet()) {
            listForAddition.add(entry.getKey());
        }
        Utils.printList(listForAddition);
        System.out.print("\n");

        System.out.println("=== Removed Files ===");
        List<String> listForRemoval = new ArrayList<>();
        for (Map.Entry<String, String> entry : removal.entrySet()) {
            listForRemoval.add(entry.getKey());
        }
        Utils.printList(listForRemoval);
        System.out.print("\n");
    }

    public void clearStageAndSave() { //清空暂存区并保存
        addition.clear();
        removal.clear();
        saveStage();
    }

    public boolean hasAddedBlob(String filename, String blobID) {
        return addition.containsKey(filename) && addition.get(filename).equals(blobID);
    }

    public boolean hasAddedFile(String filename) {
        if (addition == null) {
            return false;
        }
        return addition.containsKey(filename);
    }

    public void addItem(String filename, String id) { //有一个blob要加进暂存区
        addition.put(filename, id);
        if (removal.containsKey(filename)) {
            removal.remove(filename);
        }
    }

    public void removeFromAddition(String filename) {
        if (addition.containsKey(filename)) {
            addition.remove(filename);
        }
    }

    public void saveStage() {
        Utils.writeObject(Repository.STAGE, this);
    }

    public boolean isEmpty() {
        return addition.isEmpty() && removal.isEmpty();
    }
}