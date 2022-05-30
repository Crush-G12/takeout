package com.xie.reggie.controller;

import com.xie.reggie.comon.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.chrono.IsoChronology;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws Exception{
        log.info(file.toString());
        //MultipartFile接收到文件之后，会自动保存为临时文件，如果不转存，一次请求之后会自动清理掉
        //transferTo可以将文件转存到新的目录

        File fileName = new File(basePath);
        if(!fileName.exists()){
            //如果目录不存在，则创建目录
            fileName.mkdir();
        }

        //使用UUID生成文件名
        String filename = UUID.randomUUID().toString();
        //获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        //拼接字符串
        file.transferTo(new File(basePath + filename + suffix));
        //返回文件名
        return R.success(filename + suffix);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        try {
            //通过文件输入流读取文件
            InputStream is = new FileInputStream(new File(basePath + name));

            //设置返回的文件格式
            response.setContentType("image/jpeg");

            //将输入流的数据读到缓冲区，再将缓冲区的数据写到输出流中
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len=is.read(buffer)) != -1){
                //通过输出流输出到浏览器
                //通过浏览器的输出流将数据响应回去(从第0个开始，后面的len个数据)
                response.getOutputStream().write(buffer,0,len);
                response.getOutputStream().flush();
            }

            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
