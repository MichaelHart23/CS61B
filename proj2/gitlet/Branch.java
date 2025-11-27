package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Branch {
    public static File getCurrentBranchFile() {
        String branch = Utils.readContentsAsString(Repository.HEAD); //获取现在所处的分支名
        return Utils.join(Repository.BRACNCHES, branch); //打开该分支文件
    }

    public static void createBranch(String branchName) {
        File f = Utils.join(Repository.BRACNCHES, branchName);
        if(f.exists()) {
            Utils.exitWithError("A branch with that name already exists.");
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            Utils.exitWithError(e.getMessage());
        }
        Commit c = Commit.getHeadCommit();
        Utils.writeContents(f, c.getID());
    }

    /* 该函数并不完善，并不能体现出各个分支之间的merge关系. 也并不反映commit时间顺序*/
    public static void printAllBranches() { //for test
        HashSet<String> visited = new HashSet<>();
        ArrayDeque<Commit> headQueue = new ArrayDeque<>();
        ArrayDeque<ArrayDeque<Commit>> queues = new ArrayDeque<>();

        for(File f : Repository.BRACNCHES.listFiles()) {
            String id = Utils.readContentsAsString(f);
            File F = Utils.join(Repository.OBJECTS, id);
            Commit c = Utils.readObject(F, Commit.class);
            headQueue.add(c);
            visited.add(c.getID());
        }

        queues.add(headQueue);

        while(true) {
            if((queues.peekLast().peek().isInitial())) {
                break;
            }
            ArrayDeque<Commit> queue = new ArrayDeque<>();
            for(Commit c : queues.peekLast()) {
                if(visited.contains(c.getParentID())) {
                    continue;
                }
                Commit commit = Commit.getCommit(c.getParentID());
                visited.add(commit.getID());
                queue.add(commit);
            }
            queues.add(queue);
        }

        for(ArrayDeque<Commit> queue : queues) {
            int i = 1;
            for(Commit c : queue) {
                System.out.printf("%" + (20 * i) + "s" , c.getMessage());
                i++;
            }
            System.out.print("\n");
        }
    }

    public static void branchesStatus() {
        System.out.println("=== Branches ===");
        String currentBranchName = Utils.readContentsAsString(Repository.HEAD);
        List<String> list = new ArrayList<>();
        for(File f : Repository.BRACNCHES.listFiles()) {
            list.add(f.getName());
        }
        list.sort(String::compareTo);
        for(String s : list) {
            if(s.equals(currentBranchName)) {
                System.out.print("*");
            }
            System.out.println(s);
        }
        System.out.print("\n");
    }

    public static void switchBranch(String branchName) {
        File f = Utils.join(Repository.BRACNCHES, branchName);
        if(!f.exists()) {
            Utils.exitWithError("No such branch exists.");
        }
        String currentBranch = Utils.readContentsAsString(Repository.HEAD); //获取现在所处的分支名
        if(currentBranch.equals(branchName)) {
            Utils.exitWithError("No need to checkout the current branch.");
        }
        Commit taget = Commit.getHeadCommitOfBranch(branchName);
        Commit.replaceFiles(taget);
        Utils.writeContents(Repository.HEAD, branchName);
        Stage.getStage().clearStageAndSave();
    }
}