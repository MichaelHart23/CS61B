package gitlet;

// TODO: any imports you need here

import java.io.Serializable; 
import java.util.Date; // TODO: You'll likely use this in this class
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
    /*bolbs的哈希值和文件名的键值对 */
    private HashMap<String, String> map;
    /*父commit的id */
    private String parent;
    private String secondParent;
    /*时间戳 */
    private Date timeStamp;
    /*自身的id */
    private String id;

    public Commit() {}
    public static Commit initialCommit() {
        Commit commit = new Commit();
        commit.message = "initial commit";
        commit.timeStamp = new Date(0);
        commit.id = Utils.sha1(commit.message, commit.timeStamp.toString());
        return commit;
    }
    public static void saveCommit(Commit commit) {}

    public String getID() {
        return id;
    }
}