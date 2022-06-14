import com.itheima.reggie.ReggieApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {ReggieApplication.class})
public class UploadFileTest {
    @Test
    public void test1(){
        String fileName="evening.jpg";
        String substring = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(substring);
    }
}
