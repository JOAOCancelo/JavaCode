package com.heima.minio;

import com.heima.file.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import okhttp3.Credentials;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@SpringBootTest(classes = MinioTest.class)
@RunWith(SpringRunner.class)
public class MinioTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("D:\\list.html");
        String path = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(path);
    }

    /**
     * 把list.html文件上传到minio中，并且可以在浏览器中访问
     * @param args
     */
//    public static void main(String[] args) {
//
//        try {
//            FileInputStream fileInputStream = new FileInputStream("D:\\list.html");
//            //1.获取minio的链接信息 创建一个minio的客户端
//            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://192.168.200.130:9000").build();
//
//            //2.上传
//            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                    .object("list.html")//文件名词
//                    .contentType("text/html")//文件类型
//                    .bucket("leadnews")//桶名称
//                    .stream(fileInputStream, fileInputStream.available(), -1).build() ;
//            minioClient.putObject(putObjectArgs);
//
//            //访问路径
//            System.out.println("http://192.168.200.130:9000/leadnews/list.html");
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
//    }
}
