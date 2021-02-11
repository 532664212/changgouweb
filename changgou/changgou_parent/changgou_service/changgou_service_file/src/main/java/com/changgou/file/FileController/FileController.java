package com.changgou.file.FileController;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.file.util.FastDFSClient;
import com.changgou.file.util.FastDFSFile;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/***
 * 文件上传操作
 */
@RestController
@RequestMapping("/file")
public class FileController {
    @PostMapping("upload")
    public Result uploadFile(MultipartFile file) {
        try {
            if (file==null){
                throw  new RuntimeException("文件不存在");
            }
            String originalFilename = file.getOriginalFilename();
            if (StringUtils.isEmpty(originalFilename)){
                throw  new RuntimeException("文件不存在");
            }
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            byte[] content = file.getBytes();
            FastDFSFile fastDFSFile= new FastDFSFile(originalFilename,content,extName);
            String[] upload = FastDFSClient.upload(fastDFSFile);
            String url = FastDFSClient.getTrackerUrl()+upload[0]+"/"+upload[1];
            return new Result(true,StatusCode.OK,"文件上传成功",url);
        } catch (Exception e) {
            e.printStackTrace();
        }
            return new Result(false,StatusCode.ERROR,"文件上传失败");
    }
}













