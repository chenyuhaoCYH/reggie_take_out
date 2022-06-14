package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String bathPath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file){
        //MultipartFile 的变量名必须和form-data的名字保持一致
        //file是一个临时文件，需要转存，否则此次请求结束文件就会消失

        //使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String originalFilename = file.getOriginalFilename();
        assert originalFilename != null;
        String suffix = originalFilename.substring(originalFilename.lastIndexOf(".")); //获取文件后缀
        String fileName = UUID.randomUUID().toString()+suffix;

        File dir = new File(bathPath);
        if (!dir.exists()){
            //目录不存在
            dir.mkdirs();
        }
        try {
            //转存到指定位置
            file.transferTo(new File(bathPath+ fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(file.toString());
        return R.success(fileName);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //
        try {
            FileInputStream inputStream=new FileInputStream(new File(bathPath+name));

            ServletOutputStream outputStream=response.getOutputStream();

            response.setContentType("image/jpeg");

            int len;
            byte[] bytes=new byte[1024];
            while ((len=inputStream.read(bytes))!=-1) {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }

            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
