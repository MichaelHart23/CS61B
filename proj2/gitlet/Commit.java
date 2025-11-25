package gitlet;

import java.io.File;
import java.io.Serializable; // TODO: You'll likely use this in this class
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Michael Hart
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    //当更改Commit的结构时可能会改变
    public static final String initialCommitID = "a497e1842e2865d93b97cf6e38802025bd776121";

    /** The message of this Commit. */
    private String message;
    /*bolbs的文件名与哈希值的键值对 */
    HashMap<String, String> map;
    /*父commit的id */
    private String parent;
    String secondParent;
    /*时间戳 */
    private Date timeStamp;
    /*自身的id */
    private String id;

    public Commit() {
        map = new HashMap<>();
    }

    public Commit(String ms, Commit pa, Stage s, Date date) { //通过message和父commit和暂存区构建一个commit
        timeStamp = date;
        map = new HashMap<>(pa.map);
        for(Map.Entry<String, String> entry : s.addition.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        for(Map.Entry<String, String> entry : s.removal.entrySet()) {
            map.remove(entry.getKey());
        }
        parent = pa.id;
        message = ms;
        id = Utils.sha1(message, map.toString(), parent, timeStamp.toString());
    }

    public static Commit initialCommit() { //gitlet init时创建的第一个commit
        Commit commit = new Commit();
        commit.message = "initial commit";
        commit.timeStamp = new Date(0);
        commit.parent = null;
        commit.secondParent = null;
        commit.id = Utils.sha1(commit.message, commit.timeStamp.toString());
        return commit;
    }

    public static Commit getHeadCommit() {
        File f = Branch.getCurrentBranchFile();
        String headID = Utils.readContentsAsString(f);     //获取该分支的head commit的id
        File F = Utils.join(Repository.OBJECTS, headID);   //打开该commit对应的文件
        Commit commit = Utils.readObject(F, Commit.class); //读取head commit
        return commit;
    }

    public static Commit getHeadCommitOfBranch(String branchName) {
        File f = Utils.join(Repository.BRACNCHES, branchName);
        String headID = Utils.readContentsAsString(f);
        File F = Utils.join(Repository.OBJECTS, headID);   //打开该commit对应的文件
        Commit commit = Utils.readObject(F, Commit.class); //读取head commit
        return commit;
    }

    public static Commit getCommit(String id) {
        File f = Utils.join(Repository.OBJECTS, id);
        if(f == null) {
            return null;
        }
        Commit commit = Utils.readObject(f, Commit.class); //读取head commit
        return commit;
    }

    public static void updateHeadCommit(String id) {//更新当前分支的head commit
        File f = Branch.getCurrentBranchFile(); //获取当前分支文件
        Utils.writeContents(f, id); //写入当前commit head的id
    }

    public static void printCommit(Commit c) {
        c.print();
    }

    public boolean hasBlob(String filename, String blobID) {
        return map.containsKey(filename) && map.get(filename).equals(blobID);
    }

    public boolean hasFile(String filename) {
        return map.containsKey(filename);
    }


    public void saveCommit() {
        File f = Utils.join(Repository.OBJECTS, id);
        Utils.writeObject(f, this);
    }

    public void print() {
        System.out.println("===");
        System.out.println("commit " + id);
        if(secondParent != null) {
            System.out.println("Merge " + parent.substring(0, 7) + " " + secondParent.substring(0, 7));
        }
        System.out.println("Date: " + timeStamp.toString());
        System.out.println(message);
        System.out.print("\n");
    }

    public String getID() {
        return id;
    }

    public boolean equals(Commit other) {
        return message.equals(other.message) && map.equals(other.map) && parent.equals(other.parent);
    }

    public boolean isInitial() {
        return parent == null;
    }

    public String getParentID() {
        return parent;
    }

    public String getMessage() {
        return message;
    }
}