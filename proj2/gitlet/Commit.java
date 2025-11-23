package gitlet;

import java.io.File;
import java.io.Serializable; // TODO: You'll likely use this in this class
import java.util.Date;
import java.util.HashMap;
/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Michael Hart
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    /*bolbs的文件名与哈希值的键值对 */
    private HashMap<String, String> map;
    /*父commit的id */
    private String parent;
    private String secondParent;
    /*时间戳 */
    private Date timeStamp;
    /*自身的id */
    private String id;

    public Commit() {}
    public static Commit initialCommit() { //gitlet init时创建的第一个commit
        Commit commit = new Commit();
        commit.message = "initial commit";
        commit.timeStamp = new Date(0);
        commit.id = Utils.sha1(commit.message, commit.timeStamp.toString());
        return commit;
    }

    public static Commit getHeadCommit() {
        String branch = Utils.readContentsAsString(Repository.HEAD); //获取现在所处的分支名
        File f = Utils.join(Repository.BRACNCHES, branch); //打开该分支文件
        String headID = Utils.readContentsAsString(f);     //获取该分支的head commit的id
        File F = Utils.join(Repository.OBJECTS, headID);   //打开该commit对应的文件
        Commit commit = Utils.readObject(F, Commit.class); //读取head commit
        return commit;
    }

    public boolean hasBlob(String filename, String blobID) {
        return map.containsKey(filename) && map.get(filename).equals(blobID);
    }

    public static void saveCommit(Commit commit) {}

    public String getID() {
        return id;
    }
}