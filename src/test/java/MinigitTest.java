import com.minigit.MinigitApplication;
import com.minigit.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


@SpringBootTest(classes = MinigitApplication.class)
public class MinigitTest {
    String DIR = "D:\\Learning Data\\minigit-test";

    @Test
    public void initTest() throws IOException {
        FileUtils.deleteFileOrDirectory(DIR + File.separator + ".minigit");
        GitUtils.init(DIR);
    }

    @Test
    public void sha1Test() throws IOException {
        /*FileUtils.deleteFileOrDirectory(DIR + File.separator + ".minigit");
        GitUtils.init(DIR);
        File file = new File(DIR + File.separator + "dir0");
        System.out.println(Sha1Utils.calculateSha1(file));*/
    }

    @Test
    public void deleteTest() throws IOException {
        FileUtils.deleteFileOrDirectory(DIR + File.separator + ".minigit");
    }


    @Test
    public void addTest() throws IOException {

        GitUtils.init(DIR);
        File file = new File(DIR + File.separator + "dir0" + File.separator + "p1.txt");
        System.out.println(file.exists());
        File[] files = new File[1];
        files[0] = file;
        AddUtils.addFile(files);
        /*GitUtils.init(DIR);
        File file = new File(DIR + File.separator + "dir0");
        List<File> files  = new ArrayList<>();
        files.add(file);
        GitUtils.add(files);*/
    }

    @Test
    public void pathTest(){
        GitUtils.init(DIR);
        File file = new File(DIR + File.separator + "dir0");
        System.out.println(file.getPath());
    }

    @Test
    public void commitTest() throws NoSuchAlgorithmException, IOException {
        GitUtils.init(DIR);
    }

}
