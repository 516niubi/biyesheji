package com.example.backend.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.backend.common.enums.CodeEnum;
import com.example.backend.common.result.BaseResponse;
import com.example.backend.common.result.Result;
import com.example.backend.exception.BusinessException;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author 
 * @Description 文件接口控制层
 * @Version 1.0
 */
@RestController
@RequestMapping({"/file"})
public class FileController {

    private static final String FILES_DIR = "/files/";

    @Value("${file.upload.path:}")
    private String uploadPath;

    /**
     * 上传文件到OSS存储
     * @param file 文件
     * @return 文件访问地址
     */
    @ApiOperation("上传文件")
    @PostMapping("/upload")
    private BaseResponse<String> uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename(); // 文件完整名称
        String extName = FileUtil.extName(originalFilename);  // 文件后缀名
        String uniFileFlag = IdUtil.fastSimpleUUID(); // 随机生成文件名
        String fileFullName = uniFileFlag + StrUtil.DOT + extName;
        // 封装完整的文件路径获取方法 完整的上传路径
        String fileUploadPath = getFileUploadPath(fileFullName);
        try {
            File uploadFile = new File((fileUploadPath));
            File parentFile = uploadFile.getParentFile();
            // 如果父级目录不存在，就是files目录不存在，就创建files目录
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            file.transferTo(uploadFile);
        } catch (IOException e) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR, "文件上传失败");
        }
        String uploadPath = "/file/files/" + fileFullName;
        return Result.success(uploadPath);
    }

    /**
     * wang-editor编辑器文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename(); // 文件完整名称
        String extName = FileUtil.extName(originalFilename);  // 文件后缀名
        String uniFileFlag = IdUtil.fastSimpleUUID(); // 随机生成文件名
        String fileFullName = uniFileFlag + StrUtil.DOT + extName;
        // 封装完整的文件路径获取方法 完整的上传路径：E:\个人项目\2022年\partner交友项目\files\hjsadfjahskdfhkasdhf.jpg
        String fileUploadPath = getFileUploadPath(fileFullName);
        try {
            File uploadFile = new File(fileUploadPath);
            File parentFile = uploadFile.getParentFile();
            if (!parentFile.exists()) { // 如果父级目录不存在，就是files目录不存在，就创建files目录
                parentFile.mkdirs();
            }
            file.transferTo(uploadFile);
        }catch (Exception e) {
            throw new BusinessException(CodeEnum.SYSTEM_ERROR, "文件上传失败");
        }
        String uploadPath = "/file/files/" + fileFullName; // 文件上传后的访问网址

        Map<String, Object> resMap = new HashMap<>();
        // wangEditor上传图片成功后， 需要返回的参数
        resMap.put("errno", 0);
        Map<String, Object> data = new HashMap<>();
        data.put("url", "http://localhost:8088/api" + uploadPath);
        resMap.put("data", data);
        return resMap;
    }

    @GetMapping("/files/{fileFullName}")
    public void downloadFile(@PathVariable String fileFullName,
                             HttpServletResponse response) throws IOException {
        String extName = FileUtil.extName(fileFullName);
        String fileUploadPath = getFileUploadPath(fileFullName);
        byte[] bytes = FileUtil.readBytes(fileUploadPath);
        response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileFullName, "UTF-8"));  // 预览
        ArrayList<String> attachmentFileExtNames = CollUtil.newArrayList("docx", "doc", "xlsx", "xls", "mp4", "mp3");
        if (attachmentFileExtNames.contains(extName)) {
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileFullName, "UTF-8"));  // 附件下载
        }
        ServletOutputStream os = response.getOutputStream();
        os.write(bytes);
        os.flush();
        os.close();
    }

    /**
     * 获取文件上传路径
     * @param fileFullName 文件完成名称
     * @return
     */
    public String getFileUploadPath(String fileFullName) {
        // 如果系统文件上传了，路径不存在，那么就使用项目所在路径
        if (CharSequenceUtil.isBlank(uploadPath)) {
            uploadPath = System.getProperty("user.dir");
        }
        return uploadPath + FILES_DIR + fileFullName;
    }
}
