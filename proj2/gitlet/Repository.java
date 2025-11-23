package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.join;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author Michael Hart
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
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
    public static final File BRACNCHES = join(GITLET_DIR, "branches");
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
        BRACNCHES.mkdir();
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
        File master = join(BRACNCHES, "master");
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

    public static void add(String filename) { //用于完成gitlet add
        File f = join(Repository.CWD, filename);
        if(!f.isFile()) {
            System.err.println(filename + " is not a file.");
            System.exit(-1);
        }
        if(!f.exists()) {
            System.err.println("File does not exist.");
            System.exit(-1);
        }
        Blob b = new Blob(f);
        Stage s = Stage.getStage();
        Commit c = Commit.getHeadCommit();
        //检查父commit是否有一模一样的文件
        if(c.hasBlob(filename, b.getID())) {
            s.removeFromAddition(filename);
            s.saveStage();
            System.exit(-1);
        }
        //检查暂存区中是否已经暂存了一模一样的文件
        if(s.hasAddedBlob(filename, b.getID())) {
            System.exit(-1);
        }
        s.addItem(filename, b.getID());
        b.saveBolb();
        s.saveStage();
    }

    public void newBranch(String branchName) { //to be finshed
        File f = join(BRACNCHES, branchName);
        if(f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(-1);
        }
    }
}
