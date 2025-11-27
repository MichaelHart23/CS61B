package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable; // TODO: You'll likely use this in this class
import java.util.*;

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
        if(id.length() < 40) {
            int len = id.length();
            for(File f : Repository.OBJECTS.listFiles()) {
                String fullID = f.getName();
                if(fullID.substring(0, len).equals(id)) {
                    id = fullID;
                    break;
                }
            }
        }
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

    /**
     * 完成从当前分支切换到目标分支的工作中与文件相关的部分，比如文件的创建，删除，更新
     * @param branchName the branch to check out
     */
    public static void replaceFiles(String branchName) {
        Commit current = Commit.getHeadCommit();
        Commit target = Commit.getHeadCommitOfBranch(branchName);
        if(target.id.equals(current.id)) { //commit 是同一个
            return;
        }
        for(File f : Repository.CWD.listFiles()) {
            if(f.isDirectory()) { //跳过 .gitlet
                continue;
            }
            String filename = f.getName();
            if(!current.containFile(filename) && target.containFile(filename)) {
                Utils.exitWithError("There is an untracked file in the way; delete it, or add and commit it first.");
            } else if(current.containFile(filename) && !target.containFile(filename)) {
                f.delete();
            }
        }
        for(Map.Entry<String, String> entry : target.map.entrySet()) {
            String filename = entry.getKey();
            File f = Utils.join(Repository.CWD, filename);
            if(!f.exists()) {
                Utils.createFile(f);
            }
            Blob b = Blob.getBlob(entry.getValue());
            Utils.writeContents(f, b.content);
        }
    }

    public void ModificationsNotStaged(Stage s) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit presentFiles = new Commit();
        List<String> list = new ArrayList<>();
        for(File f : Repository.CWD.listFiles()) {
            if(f.isDirectory()) { //跳过 .gitlet 目录
                continue;
            }
            Blob b = new Blob(f);
            presentFiles.map.put(f.getName(), b.id);
        }

        for(Map.Entry<String, String> entry : map.entrySet()) {
            String filename = entry.getKey();
            if(!presentFiles.map.containsKey(filename)) { //现在这个文件没有了
                if(!s.removal.containsKey(filename)) { //这个文件不是通过rm命令删除的
                    list.add(filename + "(deleted)");
                }
            } else if(!entry.getValue().equals(presentFiles.map.get(filename)) && !s.addition.containsKey(filename)) {
                //内容相比current commit改变了却没有被add
                list.add(filename + "(modified)");
            } else if(s.addition.containsKey(filename)
                    && !s.addition.get(filename).equals(presentFiles.map.get(filename))) {
                //add之后又改内容了
                list.add(filename + "(modified)");
            }
        }
        Utils.printList(list);
        System.out.print("\n");
    }

    public void untracked() {
        System.out.println("=== Untracked Files ===");
        List<String> list = new ArrayList<>();
        for(File f : Repository.CWD.listFiles()) {
            if(f.isDirectory()) { //跳过 .gitlet 目录
                continue;
            }
            if(!map.containsKey(f.getName())) {
                list.add(f.getName());
            }
        }
        Utils.printList(list);
        System.out.print("\n");
    }

    public void replaceFile(File f) { //将工作区的文件f换为本commit中对应的文件
        String filename = f.getName();
        if(!map.containsKey(filename)) {
            Utils.exitWithError("File does not exist in that commit.");
        }
        Blob b = Blob.getBlob(map.get(filename));
        Utils.writeContents(f, b.content);
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

    public boolean containFile(String filename) {
        return map.containsKey(filename);
    }
}