import com.minigit.MinigitApplication;
import com.minigit.Service.MailService;
import com.minigit.entityService.UserService;
import com.minigit.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.MessagingException;
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
    public void fileTest() throws IOException {
        /*String path = DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "p1_2.txt";
        System.out.println(path.substring(0,path.lastIndexOf(File.separator)));*/
        File file = new File(DIR + File.separator + "dir1111111" + File.separator + "dir1111" +
                File.separator + "p1_2.txt");
        if(!file.exists()){
            file.getParentFile().mkdirs(); // 创建父目录
            file.createNewFile(); // 创建文件
        }
    }

    @Test
    public void commitTest() throws NoSuchAlgorithmException, IOException {
        initTest();
        addTest();
        GitUtils.commit("m","zlji");
    }


    // test 修改file
    @Test
    public void commitAgainTest() throws IOException, NoSuchAlgorithmException {
        GitUtils.init(DIR);
        File file3 = new File(DIR + File.separator + "dir0" + File.separator + "dir1_2" +
                File.separator + "第二次commit新增的文件.txt");
        File[] files = new File[1];
        // 另外删除了p0.txt
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

    // 回溯测试
    @Test
    public void backTest(){
        GitUtils.init(DIR);
        String commitHash = "131ae94464aabaa59841183c26928a49aab6195a";
        GitUtils.back(commitHash);
    }

    @Autowired
    private UserService userService;
    @Autowired
    private MailService mailService;

    // User注册测试
    @Test
    public void registerTest(){

    }

    @Test
    public void mailTest() throws MessagingException {
        mailService.sendMail("2875786463@qq.com", "测试@value", "shabi！");
    }
}
