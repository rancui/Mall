package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf("."));
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;

        File fileDir = new File(path);//创建upload文件夹
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }

        File targetFile = new File(path,uploadFileName);//路径+文件名，tomcat的webapps/ROOT/upload

        try {
            file.transferTo(targetFile);//上传成功，已上传到tomcat的webapps/ROOT/upload
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));// 上传到ftp服务器
            targetFile.delete();//删除webapps/ROOT/upload文件夹下的文件,防止过多占用空间啊
        } catch (IOException e){
            logger.info("上传文件一场",e);
            return null;
        }

        return targetFile.getName();

    }


}
