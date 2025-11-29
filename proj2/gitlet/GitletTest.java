 package gitlet;

 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import java.io.File;
 import java.nio.file.Files;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;

 public class GitletTest {
     private final int filenum = 15;
     private void gitlet(String... args) {
         Main.main(args);
     }

     private void delete(File f) {
         if (f.isDirectory()) {
             for (File c : f.listFiles()) {
                 delete(c);
             }
         }
         f.delete();
     }

     private void append2File(String path, String content) {
         try {
             // 使用 FileWriter 向文件追加内容
             FileWriter writer = new FileWriter(path, true);  // true 表示追加模式
             writer.write(content);
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     private void writeFile(String path, String content) {
         File f = new File(path);
         Utils.writeContents(f, content);
     }

     private void addAndCommit(String message, String... files) {
         for(String s : files) {
             gitlet("add", s);
         }
         gitlet("commit", message);
     }

     @Before
     public void setup() {
         if(Repository.GITLET_DIR.exists()) {
             delete(Repository.GITLET_DIR);
         }
         for(int i = 1; i <= filenum; i++) {
             String path = String.format("f%d.txt", i);
             File f = new File(path);
             if(!f.exists()) {
                 try {
                     f.createNewFile();
                 } catch (IOException e) {

                 }
             }
             try { //清空文件内容
                 FileWriter writer = new FileWriter(path);
                 writer.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         gitlet("init");
     }

     @Test
     public void Clear() {
         if(Repository.GITLET_DIR.exists()) {
             delete(Repository.GITLET_DIR);
         }
         for(int i = 1; i <= filenum; i++) {
             String path = String.format("f%d.txt", i);
             File f = new File(path);
             if(f.exists()) {
                 delete(f);
             }
         }
     }

     @Test
     public void test1() {
         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         Blob b1 = new Blob("f1.txt");
         Blob b2 = Blob.getBlob(b1.getID());
 //        System.out.println("b1 id: " + b1.getID());
 //        System.out.println("b2 id: " + b2.getID());
 //        System.out.println("b1: " + b1.content);
 //        System.out.println("b2: " + b2.content);
         assertTrue(b1.getID().equals(b2.getID()));


         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         Blob b3 = new Blob("f2.txt");
         Blob b4 = Blob.getBlob(b3.getID());
 //        System.out.println("id: " + b3.getID());
 //        System.out.println("b3: " + b3.content);
 //        System.out.println("b4: " + b4.content);
         assertTrue(b3.getID().equals(b4.getID()));
         System.out.println("add finished");

         String commitMessage = "first";
         Stage s = Stage.getStage();
         System.out.println("stage's addition: " + s.addition.toString());
         Commit pa = Commit.getHeadCommit();
         Commit c1 = new Commit(commitMessage, pa, s, new Date());
         gitlet("commit", commitMessage);
         Commit c2 = Commit.getHeadCommit();
         System.out.println("c1: " + c1.map.toString());
         System.out.println("c2: " + c2.map.toString());
         assertTrue(c1.equals(c2));
         System.out.println("commit finished");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("rm", "f2.txt");
         s = Stage.getStage();
         System.out.println("here");
         System.out.println("stage's removal: " + s.removal.toString());
         assertTrue(s.removal.containsKey("f2.txt"));
         gitlet("commit", "second");
         Commit c = Commit.getHeadCommit();
         assertFalse(c.map.containsKey("f2.txt"));
         System.out.println("rm finished");
     }

     @Test
     public void testBranch() {
         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit1");
         gitlet("branch", "branch2");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit2");

         String head = Utils.readContentsAsString(Repository.HEAD);
         assertTrue(head.equals("branch2"));
         Branch.printAllBranches();
     }

     @Test
     public void testLog() {
         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit1");
         //gitlet("branch", "branch2");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit2");

         gitlet("log");
     }

     @Test
     public void testGlobalLog() {
         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit1");
         gitlet("branch", "branch2");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit2");

         gitlet("global-log");
     }

     @Test
     public void testFind() {
         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit1");
         gitlet("branch", "branch2");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit2");

         writeFile("f1.txt", "hello1");
         gitlet("add", "f1.txt");
         writeFile("f2.txt", "hello2");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit1");
         gitlet("branch", "branch3");

         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");
         gitlet("commit", "commit3");

         gitlet("find", "initial commit");
         gitlet("find", "commit1");
     }

     @Test
     public void testStatus() {
         for(int i = 1; i <= filenum; i++) {
             writeFile(String.format("f%d.txt", i), String.format("hello%d", i));
             gitlet("add", String.format("f%d.txt", i));
         }
         gitlet("commit", "commit1");


         //Staged file
         append2File("f1.txt", "hello1");
         append2File("f2.txt", "hello2");
         gitlet("add", "f1.txt");
         gitlet("add", "f2.txt");

         //removed file
         gitlet("rm", "f3.txt");

         //Modifications Not Staged For Commit
         //修改了文件
         append2File("f4.txt", "hello4");
         //不通过rm删除文件
         File f = new File("f6.txt");
         f.delete();
         //add之后又修改
         append2File("f5.txt", "hello5");
         gitlet("add", "f5.txt");
         append2File("f5.txt", "hello5");
         //add之后删除
         append2File("f7.txt", "hello7");
         gitlet("add", "f7.txt");
         File f7 = new File("f7.txt");
         f7.delete();


         //Untracked file: 本来就有一些

         gitlet("status");
     }

     @Test
     public void testCheckoutBranch() {
         /* master switch to second
          * f1 共有
          * f2 second独有，,需重新创建，之后提示并终止运行
          * f3 共有，被覆写
          * f4 master独有
          *
          */
         writeFile("f1.txt", "1");
         addAndCommit("master commit 1", "f1.txt");;
         append2File("f1.txt", "1");
         writeFile("f3.txt", "content for master");
         addAndCommit( "master commit 2", "f1.txt", "f3.txt");


         gitlet("branch", "second");
         gitlet("checkout", "second");


         writeFile("f1.txt", "second content");
         writeFile("f2.txt", "2");
         addAndCommit( "second branch commit 1", "f2.txt");
         append2File("f2.txt", "2");
         writeFile("f3.txt", "content for second branch");
         addAndCommit("second branch commit 2", "f2.txt", "f3.txt");


         gitlet("checkout", "master");
         writeFile("f4.txt", "4");
         addAndCommit( "master commit 3", "f4.txt");
         //writeFile("f2.txt", "another f2.txt");
         gitlet("checkout", "second");
     }

     @Test
     public void testCheckoutReplaceFile() {
         writeFile("f1.txt", "version1 of f1");
         addAndCommit("commit1", "f1.txt");
         Commit c = Commit.getHeadCommit();
         writeFile("f1.txt", "version2 of f1");
         addAndCommit("commit1", "f1.txt");
         writeFile("f1.txt", "version3 of f1");
         gitlet("checkout", "--", "f1.txt");
         String id = c.getID().substring(0, 6);
         gitlet("checkout", id, "--", "f1.txt");

     }

     @Test
     public void testMerge() {
         /* f1.txt sp有 master没改 given改了 情况1
          * f2.txt sp有 master改了 given没改 情况2
          * f3.txt sp有 master改了 given也改 改的一样 情况3
          * f4.txt sp有 master删了 given没改 情况7
          * f5.txt sp有 master没改 given删了 情况6
          * f6.txt sp有 master删了 given改了 情况8  可以在merge之前在master创建来查看是否终止
          * f7.txt sp有 master改了 given删了 情况8
          * f8.txt sp有 master删了 given删了 情况3
          * f9.txt sp有 master改了 given改了 改的不一样 情况8
          *
          * f10.txt sp无 master添加了 given未添加 情况6
          * f11.txt sp无 master未添加 given添加了 情况5 可以在merge之前在master创建来查看是否终止
          */
         for(int i = 1; i <= 9; i++) {
             writeFile(String.format("f%d.txt", i), String.format("hello%d", i));
             gitlet("add", String.format("f%d.txt", i));
         }
         gitlet("commit", "commit1 split point");
         //given
         {
             gitlet("branch", "given");
             gitlet("checkout", "given");
             writeFile("f1.txt", "given modified f1");
             writeFile("f3.txt", "both master and given modified but same");
             writeFile("f6.txt", "given modified f6");
             writeFile("f9.txt", "given modified f9");
             gitlet("rm", "f5.txt");
             gitlet("rm", "f7.txt");
             gitlet("rm", "f8.txt");

             writeFile("f11.txt", "given added f11");

             addAndCommit("given commit", "f1.txt", "f3.txt", "f6.txt", "f9.txt", "f11.txt");
         }
         // master
         {
             gitlet("checkout", "master");
             writeFile("f2.txt", "master modified f2");
             writeFile("f3.txt", "both master and given modified but same");
             writeFile("f7.txt", "master modified f7");
             writeFile("f9.txt", "master modified f9");
             gitlet("rm", "f4.txt");
             gitlet("rm", "f6.txt");
             gitlet("rm", "f8.txt");

             writeFile("f10.txt", "master added f10");

             addAndCommit("given commit", "f2.txt", "f3.txt", "f7.txt", "f9.txt", "f10.txt");
         }
 //        File f = Utils.join(Repository.CWD, "f6.txt");
 //        Utils.createFile(f);

 //        File f = Utils.join(Repository.CWD, "f11.txt");
 //        Utils.createFile(f);

         gitlet("merge", "given");

         Commit c = Commit.getHeadCommit();
         assertTrue(c.getMessage().equals("Merged given into master."));


         assertTrue(c.containFile("f1.txt") && c.containFile("f2.txt")
                 && c.containFile("f3.txt") && c.containFile("f6.txt")
                 && c.containFile("f7.txt") && c.containFile("f9.txt")
                 && c.containFile("f10.txt") && c.containFile("f11.txt"));
         assertFalse(c.containFile("f4.txt") || c.containFile("f5.txt")
                 || c.containFile("f8.txt"));

         File f1 = Utils.join(Repository.CWD, "f1.txt");
         String contentF1 = Utils.readContentsAsString(f1);
         assertTrue(contentF1.equals("given modified f1"));

         File f2 = Utils.join(Repository.CWD, "f2.txt");
         String contentF2 = Utils.readContentsAsString(f2);
         assertTrue(contentF2.equals("master modified f2"));

         File f3 = Utils.join(Repository.CWD, "f3.txt");
         String contentF3 = Utils.readContentsAsString(f3);
         assertTrue(contentF3.equals("both master and given modified but same"));

         File f4 = Utils.join(Repository.CWD, "f4.txt");
         assertFalse(f4.exists());

         File f5 = Utils.join(Repository.CWD, "f5.txt");
         assertFalse(f5.exists());

         File f6 = Utils.join(Repository.CWD, "f6.txt");
         String contentF6 = Utils.readContentsAsString(f6);
         assertTrue(contentF6.equals("<<<<<<< HEAD\n" +
                 "" +
                 "=======\n" +
                 "given modified f6" +
                 ">>>>>>>"));

         File f7 = Utils.join(Repository.CWD, "f7.txt");
         String contentF7 = Utils.readContentsAsString(f7);
         assertTrue(contentF7.equals("<<<<<<< HEAD\n" +
                 "master modified f7" +
                 "=======\n" +
                 "" +
                 ">>>>>>>"));

         File f8 = Utils.join(Repository.CWD, "f8.txt");
         assertFalse(f8.exists());

         File f9 = Utils.join(Repository.CWD, "f9.txt");
         String contentF9 = Utils.readContentsAsString(f9);
         assertTrue(contentF9.equals("<<<<<<< HEAD\n" +
                 "master modified f9" +
                 "=======\n" +
                 "given modified f9" +
                 ">>>>>>>"));

         File f10 = Utils.join(Repository.CWD, "f10.txt");
         String contentF10 = Utils.readContentsAsString(f10);
         assertTrue(contentF10.equals("master added f10"));

         File f11 = Utils.join(Repository.CWD, "f11.txt");
         String contentF11 = Utils.readContentsAsString(f11);
         assertTrue(contentF11.equals("given added f11"));
     }



 }
