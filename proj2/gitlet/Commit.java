package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Represents a gitlet commit object.
 *
 *  @author Michael Hart
 */
public class Commit implements Serializable {
    /**
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

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
        for (Map.Entry<String, String> entry : s.addition.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        // for (Map.Entry<String, String> entry : s.removal.entrySet()) {
        //     map.remove(entry.getKey());
        // }
        for (String str : s.removal) {
            map.remove(str);
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
        File F = Utils.join(Repository.COMMITS, headID);   //打开该commit对应的文件
        Commit commit = Utils.readObject(F, Commit.class); //读取head commit
        return commit;
    }

    public static Commit getHeadCommitOfBranch(String branchName) {
        File f = Utils.join(Repository.BRANCHES, branchName);
        String headID = Utils.readContentsAsString(f);
        File F = Utils.join(Repository.COMMITS, headID);   //打开该commit对应的文件
        Commit commit = Utils.readObject(F, Commit.class); //读取head commit
        return commit;
    }

    public static Commit getCommit(String id) {
        if (id.length() < 40) {
            int len = id.length();
            for (File f : Repository.COMMITS.listFiles()) {
                String fullID = f.getName();
                if (fullID.substring(0, len).equals(id)) {
                    id = fullID;
                    break;
                }
            }
        }
        File f = Utils.join(Repository.COMMITS, id);
        if (!f.exists()) {
            return null;
        }
        Commit commit = Utils.readObject(f, Commit.class); //读取head commit
        return commit;
    }

    /*
     * 对于split point的计算，还要考虑second parent的那条路径
     */
    public static Commit getSplitPoint(Commit a, Commit b) {
        HashSet<String> set = new HashSet<>();
        Deque<Commit> deque = new ArrayDeque<>();
        set.add(a.id);
        set.add(b.id);
        deque.add(a);
        deque.add(b);
        while (true) {
            Deque<Commit> newDeque = new ArrayDeque<>();
            while (!deque.isEmpty()) {
                Commit c = deque.poll();
                if (c.parent != null) {
                    if (set.contains(c.getParentID())) {
                        return getCommit(c.getParentID());
                    }
                    newDeque.add(getCommit(c.getParentID()));
                    if (c.secondParent != null) {
                        if (set.contains(c.secondParent)) {
                            return getCommit(c.secondParent);
                        }
                        newDeque.add(getCommit(c.secondParent));
                        set.add(c.secondParent);
                    }
                    set.add(c.parent);
                }
            }
            deque = newDeque;

            // if (a.parent != null) {
            //     if (set.contains(a.parent)) {
            //         return getCommit(a.parent);
            //     }
            //     a = getCommit(a.parent);
            //     set.add(a.id);
            // }
            // if (b.parent != null) {
            //     if (set.contains(b.parent)) {
            //         return Commit.getCommit(b.parent);
            //     }
            //     b = getCommit(b.parent);
            //     set.add(b.id);
            // }
        }
    }

    public static void updateHeadCommit(String id) { //更新当前分支的head commit
        File f = Branch.getCurrentBranchFile(); //获取当前分支文件
        Utils.writeContents(f, id); //写入当前commit head的id
    }

    public static void printCommit(Commit c) {
        c.print();
    }

    /**
     * 完成从当前分支切换到目标分支的工作中与文件相关的部分，比如文件的创建，删除，更新
     * @param target the head of the target branch
     */
    public static void replaceFiles(Commit target) {
        Commit current = Commit.getHeadCommit();
        ArrayList<File> toBeDeleted = new ArrayList<>();
        if (target.id.equals(current.id)) { //commit 是同一个
            return;
        }
        for (File f : Repository.CWD.listFiles()) {
            if (f.isDirectory()) { //跳过 .gitlet
                continue;
            }
            String filename = f.getName();
            if (!current.containFile(filename) && target.containFile(filename)) {
                Utils.exitWith(
                    "There is an untracked file in the way; delete it, " 
                    + "or add and commit it first.");
            } else if (current.containFile(filename) && !target.containFile(filename)) {
                toBeDeleted.add(f); //不能在这里删除，要等到全部检查完之后再删除
            }
        }
        for (File f : toBeDeleted) {
            f.delete();
        }
        for (Map.Entry<String, String> entry : target.map.entrySet()) {
            String filename = entry.getKey();
            File f = Utils.join(Repository.CWD, filename);
            if (!f.exists()) {
                Utils.createFile(f);
            }
            Blob b = Blob.getBlob(entry.getValue());
            Utils.writeContents(f, b.content);
        }
    }

    private static String mergeContent(String filename, Commit cur, 
                                        Commit given, Boolean curHave, Boolean givenHave) {
        String s1;
        String s2;
        if (curHave) {
            Blob b = Blob.getBlob(cur.map.get(filename));
            s1 = b.content;
        } else {
            s1 = "";
        }
        if (givenHave) {
            Blob b = Blob.getBlob(given.map.get(filename));
            s2 = b.content;
        } else {
            s2 = "";
        }
        return "<<<<<<< HEAD\n" + s1 + "=======\n" + s2 + ">>>>>>>\n";
    }

    private static Boolean checkSP(Commit cur, Commit given, Commit sp,
                                    HashMap<String, String> conflictFile, 
                                    HashSet<String> visited,
                                    HashSet<String> wd,
                                    Stage s) {
        Boolean conflict = false;

        for (Map.Entry<String, String> entry : sp.map.entrySet()) { //在sp中存在文件
            String filename = entry.getKey();
            if (cur.containFile(filename) && given.containFile(filename)) { //文件在两者皆存在
                if (entry.getValue().equals(cur.map.get(filename))
                        && !entry.getValue().equals(given.map.get(filename))) {
                    //given修改，cur未修改 情况1
                    Blob b = Blob.getBlob(given.map.get(filename));
                    s.addItem(filename, b.getID());
                } else if (!entry.getValue().equals(cur.map.get(filename)) //
                        && entry.getValue().equals(given.map.get(filename))) {
                    //given未修改，cur修改, 不用处理 情况2
                    continue;
                } else if (!entry.getValue().equals(cur.map.get(filename))
                        && !entry.getValue().equals(given.map.get(filename))
                        && cur.map.get(filename).equals(given.map.get(filename))) {
                    //二者皆修改，但改成一样的, 是不用处理的，先放在这里 情况3
                    continue;
                } else if (!entry.getValue().equals(cur.map.get(filename))
                        && !entry.getValue().equals(given.map.get(filename))
                        && !cur.map.get(filename).equals(given.map.get(filename))) {
                    //二者皆修改，且改的不一样 conflict 情况8
                    String content = mergeContent(filename, cur, given, true, true);
                    conflictFile.put(filename, content);
                    conflict = true;
                }
            } else if (cur.containFile(filename) || given.containFile(filename)) { //有且仅有一个把该文件删了
                if (cur.containFile(filename) && entry.getValue().equals(cur.map.get(filename))) {
                    //given删了，cur未修改 情况6
                    s.removal.add(filename);
                } else if (given.containFile(filename) 
                    && entry.getValue().equals(given.map.get(filename))) {
                    //cur删了，given未修改 情况7
                    //若此时工作区中仍有该文件，该不该删呢？感觉这种情况不必处理, 也不用报错
                    continue;
                } else if (cur.containFile(filename) 
                    && !entry.getValue().equals(cur.map.get(filename))) {
                    //given删了，cur修改了 情况8
                    String content = mergeContent(filename, cur, given, true, false);
                    conflictFile.put(filename, content);
                    conflict = true;
                } else if (given.containFile(filename) 
                    && !entry.getValue().equals(given.map.get(filename))) {
                    //cur删了，given修改了 情况8
                    //若此时工作区中仍有该文件，那就要报错并退出了
                    if (wd.contains(filename)) {
                        Utils.exitWith("There is an untracked file in the way; delete " 
                            + "it, or add and commit it first.");
                    }
                    String content = mergeContent(filename, cur, given, false, true);
                    conflictFile.put(filename, content);
                    conflict = true;
                }
            } //二者都删了情况不用处理，保持不变 情况3

            visited.add(filename);
        }
        return conflict;
    }

    public static Commit merge(Commit cur, Commit given, Commit sp) {
        Stage s = Stage.getStage(); //应为空
        HashMap<String, String> conflictFile = new HashMap<>();
        HashSet<String> visited = new HashSet<>(); //已经处理过的文件名的集合
        HashSet<String> wd = new HashSet<>(); //工作区文件集合
        Boolean conflict = false;
        for (File f : Repository.CWD.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            wd.add(f.getName());
        }

        conflict = checkSP(cur, given, sp, conflictFile, visited, wd, s);

        for (Map.Entry<String, String> entry : given.map.entrySet()) {
            String filename = entry.getKey();
            if (visited.contains(filename)) {
                continue;
            }
            if (!sp.containFile(filename) && !cur.containFile(filename)) {
                //文件仅存在于given 情况5
                //若此时工作区中有该文件，那就要报错了
                if (wd.contains(filename)) {
                    Utils.exitWith(
                        "There is an untracked file in the way; delete it, " 
                        + "or add and commit it first.");
                }
                Blob b = Blob.getBlob(given.map.get(filename));
                s.addItem(filename, b.getID());
            }
            //visited.add(filename);
        }

        //文件仅存在于cur，不用处理 情况4

        for (Map.Entry<String, String> entry : conflictFile.entrySet()) {
            Blob b = new Blob();
            b.content = entry.getValue();
            b.id = Utils.sha1(entry.getKey(), b.content);
            s.addItem(entry.getKey(), b.getID());
            b.saveBolb();
        }

        for (Map.Entry<String, String> entry : s.addition.entrySet()) {
            File f = Utils.join(Repository.CWD, entry.getKey());
            if (!f.exists()) {
                Utils.createFile(f);
            }
            Blob b = Blob.getBlob(entry.getValue());
            Utils.writeContents(f, b.content);
        }
        for (String str : s.removal) {
            File f = Utils.join(Repository.CWD, str);
            if (f.exists()) {
                f.delete();
            }
        }
        Commit c = new Commit("", cur, s, new Date());
        c.secondParent = given.getID();

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }

        return c;
    }

    public void modifiedNotStaged(Stage s) {
        System.out.println("=== Modifications Not Staged For Commit ===");
        Commit presentFiles = new Commit();
        List<String> list = new ArrayList<>();
        for (File f : Repository.CWD.listFiles()) {
            if (f.isDirectory()) { //跳过 .gitlet 目录
                continue;
            }
            Blob b = new Blob(f);
            presentFiles.map.put(f.getName(), b.id);
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String filename = entry.getKey();
            if (!presentFiles.map.containsKey(filename)) { //现在这个文件没有了
                if (!s.removal.contains(filename)) { //这个文件不是通过rm命令删除的
                    list.add(filename + "(deleted)");
                }
            } else if (!entry.getValue().equals(presentFiles.map.get(filename)) 
                && !s.addition.containsKey(filename)) {
                //内容相比current commit改变了却没有被add
                list.add(filename + "(modified)");
            } else if (s.addition.containsKey(filename)
                    && !s.addition.get(filename).equals(presentFiles.map.get(filename))) {
                //add之后又改内容了
                list.add(filename + "(modified)");
            }
        }
        Utils.printList(list);
        System.out.print("\n");
    }

    public void untracked(Stage s) {
        System.out.println("=== Untracked Files ===");
        List<String> list = new ArrayList<>();
        for (File f : Repository.CWD.listFiles()) {
            if (f.isDirectory()) { //跳过 .gitlet 目录
                continue;
            }
            if (!map.containsKey(f.getName()) && !s.hasAddedFile(f.getName())) {
                list.add(f.getName());
            }
        }
        Utils.printList(list);
        System.out.print("\n");
    }

    public void replaceFile(File f) { //将工作区的文件f换为本commit中对应的文件
        String filename = f.getName();
        if (!map.containsKey(filename)) {
            Utils.exitWith("File does not exist in that commit.");
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
        File f = Utils.join(Repository.COMMITS, id);
        Utils.writeObject(f, this);
    }

    public void print() {
        System.out.println("===");
        System.out.println("commit " + id);
        if (secondParent != null) {
            System.out.println("Merge: " + parent.substring(0, 7) 
                + " " + secondParent.substring(0, 7));
        }
        String formatted = new Formatter().format("%ta %tb %td %tT %tY %tz",
                                            timeStamp, timeStamp, timeStamp, 
                                            timeStamp, timeStamp, timeStamp)
                                            .toString();
        System.out.println("Date: " + formatted);
        System.out.println(message);
        System.out.print("\n");
    }

    public String getID() {
        return id;
    }

    public boolean equals(Commit other) {
        return message.equals(other.message) 
            && map.equals(other.map) 
            && parent.equals(other.parent);
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

    public void setMessage(String ms) {
        message = ms;
    }

    public void updateID() {
        if (secondParent == null) {
            id = Utils.sha1(message, map.toString(), parent, timeStamp.toString());
        } else {
            id = Utils.sha1(message, map.toString(), parent, secondParent, timeStamp.toString());
        }
    }

}

