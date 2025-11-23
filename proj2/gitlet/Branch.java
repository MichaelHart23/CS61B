package gitlet;

import java.io.File;

public class Branch {
    public static File getCurrentBranchFile() {
        String branch = Utils.readContentsAsString(Repository.HEAD); //获取现在所处的分支名
        return Utils.join(Repository.BRACNCHES, branch); //打开该分支文件
    }
}