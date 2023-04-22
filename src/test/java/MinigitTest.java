import com.minigit.MinigitApplication;
import com.minigit.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;


@SpringBootTest(classes = MinigitApplication.class)
public class MinigitTest {
    String DIR = "D:\\Learning Data\\minigit-test";

    @Test
    public void initTest() throws IOException {
        FileUtils.deleteFileOrDirectory(DIR + File.separator + ".minigit");
        GitUtils.init(DIR);
    }

    @Test
    public void deleteTest() throws IOException {
        FileUtils.deleteFileOrDirectory(DIR + File.separator + ".minigit");
    }

    @Test
    public void addTest() throws IOException {
        GitUtils.init(DIR);
        File file1 = new File(DIR + File.separator + "dir0" + File.separator + "p0.txt");
        File file2 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_1" +
                File.separator + "p1_1.txt");
        /*File file3 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "p1_2.txt");*/
        File[] files = new File[2];
        files[0] = file1;
        files[1] = file2;
        //files[2] = file3;
        AddUtils.addFile(files);
    }

    @Test
    public void pathTest(){
        GitUtils.init(DIR);
        File file = new File(DIR + File.separator + "dir0");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.getPath());
        System.out.println(Paths.get(file.getAbsolutePath()));
        // 三个路径一模一样
    }

    @Test
    public void commitTest() throws NoSuchAlgorithmException, IOException {
        initTest();
        addTest();
        GitUtils.commit("m","zlji");
    }
    @Test
    public void fileTest() throws IOException {

    }

    // test 修改file
    @Test
    public void commitAgainTest() throws IOException, NoSuchAlgorithmException {
        GitUtils.init(DIR);
        File file3 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "p1_2.txt");
        File[] files = new File[1];
        files[0] = file3;
        AddUtils.addFile(files);
        GitUtils.commit("m2222","zlji");
    }

    // test 增加file
    @Test
    public void addFileCommitAgainTest() throws IOException, NoSuchAlgorithmException {
        GitUtils.init(DIR);
        File file3 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "新建文本文档.txt");
        File[] files = new File[1];
        files[0] = file3;
        AddUtils.addFile(files);
        GitUtils.commit("新增文本文档","zlji");
    }

    // test 删除file
    @Test
    public void deleteFileCommitAgainTest() throws IOException, NoSuchAlgorithmException {
        GitUtils.init(DIR);
        /*File file3 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "新建文本文档.txt");
        File[] files = new File[1];
        files[0] = file3;
        AddUtils.addFile(files);*/
        GitUtils.commit("删除测试","zlji");
    }
}
