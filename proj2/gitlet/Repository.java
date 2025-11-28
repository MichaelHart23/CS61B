package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static gitlet.Utils.join;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Michael Hart
 */
public class Repository {
    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /*存放所有blobs和commits的目录 */
    public static final File OBJECTS = join(GITLET_DIR, "objects");
    /*存放所有分支的目录，每个以分支名命名的文件中存的是head commit的哈希值 */
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    /*当前的commit head是哪个分支，存的是分支名 */
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    /*暂存区 用来存放要添加的文件，要修改的文件，要删除的文件*/
    public static final File STAGE = join(GITLET_DIR, "stage");

    //创建各个所需的文件，用于初始化
    private static void setupPersistence() {
        if(GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(-1);
        }
        GITLET_DIR.mkdir();
        OBJECTS.mkdir();
        BRANCHES.mkdir();
        try {
            HEAD.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }

        try {
            STAGE.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }
    //初始化时创建initial commit
    private static void zeroCommit(Commit commit) {
        File f = join(OBJECTS, commit.getID());
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
        Utils.writeObject(f, commit);
    }
    //初始化时创建master分支
    private static void makeMasterBranch(String id) {
        File master = join(BRANCHES, "master");
        try {
            master.createNewFile();
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
        Utils.writeContents(master, id);
        Utils.writeContents(HEAD, "master"); //初始commit为master分支的commit head
    }

    public static void init() { //用于完成gitlet init
        setupPersistence();
        Commit initialCommit = Commit.initialCommit();
        zeroCommit(initialCommit);
        makeMasterBranch(initialCommit.getID());
    }

    public static boolean initialized() {
        return GITLET_DIR.exists() && OBJECTS.exists() && BRANCHES.exists() && HEAD.exists() && STAGE.exists();
    }

    public static void add(String filename) { //用于完成gitlet add
        File f = join(Repository.CWD, filename);
        if(!f.isFile()) {
            Utils.exitWith(filename + " is not a file.");
        }
        if(!f.exists()) {
            Utils.exitWith("File does not exist.");
        }
        Blob b = new Blob(f);
        Stage s = Stage.getStage();
        Commit c = Commit.getHeadCommit();
        //检查父commit是否有一模一样的文件
        if(c.hasBlob(filename, b.getID())) {
            s.removeFromAddition(filename);
            s.saveStage();
            System.exit(0);
        }
        //检查暂存区中是否已经暂存了一模一样的文件
        if(s.hasAddedBlob(filename, b.getID())) {
            System.exit(0);
        }
        s.addItem(filename, b.getID());
        b.saveBolb();
        s.saveStage();
    }
    
    public static void commit(String commitMessage) {
        Stage s = Stage.getStage();
        if(s.addition.isEmpty() && s.removal.isEmpty()) {
            Utils.exitWith("No changes added to the commit.");
        }
        Commit pa = Commit.getHeadCommit();
        Commit c = new Commit(commitMessage, pa, s, new Date());
        c.saveCommit();
        Commit.updateHeadCommit(c.getID());  //更新commit head
        s.clearStageAndSave();   //清空暂存区
    }

    public static void rm(String filename) {
        Stage s = Stage.getStage();
        Commit c = Commit.getHeadCommit();
        File f = Utils.join(CWD, filename);
        Blob b = new Blob(f);
        if(!s.hasAddedFile(filename) && !c.hasFile(filename)) {
            Utils.exitWith("No reason to remove the file.");
        }
        if(f.exists()) {
            f.delete(); //若该文件仍在工作区，删除该文件
        }
        if(s.hasAddedFile(filename)) {
            s.addition.remove(filename);
        }
        if(c.hasFile(filename)) {
            s.removal.put(filename, b.getID());
        }
        s.saveStage();
    }

    /**
     * @param c the head of a list of commits
     * @param visited those commits traversed
     * @param meet commit需满足的条件
     * @param action the action applied to the commit
     * 遍历以c为首的commit链表，当commit满足某个条件时，对每个commit执行相应操作
     */
    private static void traverseBranch(Commit c, HashSet<String> visited,
                                       Predicate<Commit> meet, Consumer<Commit> action) {
        while(!visited.contains(c.getID())) {
            if(meet.test(c)) {
                action.accept(c);
            }
            visited.add(c.getID());
            if(c.getParentID() == null) { //当前commit是initial commit
                break;
            }
            c = Commit.getCommit(c.getParentID());
        }
    }

    public static void log() {
        Commit c = Commit.getHeadCommit();
        traverseBranch(c, new HashSet<>(), (commit)->{ return true; }, Commit::printCommit);
    }

    public static void global_log() {
        HashSet<String> visited = new HashSet<>();
        for(File f : Repository.BRANCHES.listFiles()) {
            String id = Utils.readContentsAsString(f);
            Commit c = Commit.getCommit(id);
            traverseBranch(c, visited, (commit)->{ return true; }, Commit::printCommit);
        }
    }

    public static void find(String commitMessage) {
        HashSet<String> visited = new HashSet<>();
        for(File f : Repository.BRANCHES.listFiles()) {
            String id = Utils.readContentsAsString(f);
            Commit c = Commit.getCommit(id);
            traverseBranch(c, visited, (commit)->{return commit.getMessage().equals(commitMessage);},
                    (commit)->System.out.println(commit.getID()));
        }
    }

    public static void status() {
        Branch.branchesStatus();
        Stage s = Stage.getStage();
        s.stageStatus();
        Commit c = Commit.getHeadCommit();
        c.ModificationsNotStaged(s);
        c.untracked();
    }



    public static void checkout(String[] args) {
        if(args.length == 2) {
            Branch.switchBranch(args[1]);
            return;
        }

        Commit c = null;
        if(!args[args.length - 2].equals("--")) {
            Utils.exitWith("Incorrect operands.");
        }
        File f = Utils.join(Repository.CWD, args[args.length - 1]);
        if(args.length == 3) {
            c = Commit.getHeadCommit();
        } else {
            String id = args[1];
            c = Commit.getCommit(id);
            if(c == null) {
                Utils.exitWith("No commit with that id exists.");
            }
        }

        if(!c.hasFile(f.getName())) {
            Utils.exitWith("File does not exist in that commit.");
        }

        c.replaceFile(f);
    }

    public static void branch(String branchName) {
        Branch.createBranch(branchName);
    }

    public static void rmBranch(String branchName) {
        String currentBranch = Utils.readContentsAsString(Repository.HEAD);
        if(currentBranch.equals(branchName)) {
            Utils.exitWith("Cannot remove the current branch.");
        }
        File f = Utils.join(Repository.BRANCHES, branchName);
        if(!f.exists()) {
            Utils.exitWith("A branch with that name does not exist.");
        }
        f.delete();
    }

    public static void reset(String commitID) {
        Commit c = Commit.getCommit(commitID);
        if(c == null) {
            Utils.exitWith("No commit with that id exists.");
        }
        Commit.replaceFiles(c);
        File f = Branch.getCurrentBranchFile();
        Utils.writeContents(f, c.getID());
        Stage.getStage().clearStageAndSave();
    }

    public static void merge(String branchName) {
        Stage s = Stage.getStage();
        if(!s.isEmpty()) {
            Utils.exitWith("You have uncommitted changes.");
        }
        File givenBranch = Branch.getBranchFile(branchName);
        if(givenBranch == null) {
            Utils.exitWith("A branch with that name does not exist.");
        }
        File currentBranch = Branch.getCurrentBranchFile();
        if(givenBranch.getName().equals(currentBranch.getName())) {
            Utils.exitWith("Cannot merge a branch with itself.");
        }
        Commit cur = Commit.getHeadCommit();
        Commit given = Commit.getHeadCommitOfBranch(branchName);
        Commit sp = Commit.getSplitPoint(cur, given);
        if(sp.getID().equals(cur.getID())) {
            Branch.switchBranch(branchName);
            Utils.exitWith("Current branch fast-forwarded.");
        }
        if(sp.getID().equals(given.getID())) {
            Utils.exitWith("Given branch is an ancestor of the current branch.");
        }

        Commit c = Commit.merge(cur, given, sp);
        c.setMessage("Merged " + givenBranch.getName() + " into " + currentBranch.getName() + ".");
        c.updateID();
        c.saveCommit();
        Commit.updateHeadCommit(c.getID());

        //remove branch
        File f = Utils.join(Repository.BRANCHES, branchName);
        f.delete();

    }
}
