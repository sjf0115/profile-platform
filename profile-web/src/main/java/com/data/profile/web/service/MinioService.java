package com.data.profile.web.service;

import com.data.profile.web.config.MinioConfig;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * MinIO 文件上传服务
 */
@Slf4j
@Service
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 上传文件到 MinIO
     *
     * @param file 上传的文件
     * @param prefix 路径前缀（如 upload_group）
     * @return 文件在 MinIO 中的完整路径（objectName）
     */
    public String uploadFile(MultipartFile file, String prefix) {
        try {
            // 确保 bucket 存在
            ensureBucketExists();

            // 生成唯一文件名：prefix/yyyyMMdd/uuid.ext
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String uuid = UUID.randomUUID().toString();
            String objectName = prefix + "/" + dateStr + "/" + uuid + ext;

            // 上传文件
            // TODO MultipartUpload 上传模式
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("文件上传成功: bucket={}, objectName={}", minioConfig.getBucket(), objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 以流方式上传文件到 MinIO（投递/数据管道场景，自定义完整路径）。
     *
     * @param inputStream 输入流
     * @param objectName  完整对象路径（由调用方控制，如 export/xxx.csv）
     * @param contentType MIME 类型
     * @return 文件在 MinIO 中的完整路径（objectName）
     */
    public String uploadStream(InputStream inputStream, String objectName, String contentType) {
        try {
            // 确保 bucket 存在
            ensureBucketExists();

            // 未知长度走分块上传（-1 + 默认 partSize）
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .stream(inputStream, -1, PutObjectArgs.MIN_MULTIPART_SIZE)
                            .contentType(contentType)
                            .build()
            );

            log.info("流式上传成功: bucket={}, objectName={}", minioConfig.getBucket(), objectName);
            return objectName;
        } catch (Exception e) {
            log.error("流式上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("流式上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除 MinIO 中的文件
     *
     * @param objectName 文件路径
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: bucket={}, objectName={}", minioConfig.getBucket(), objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 从 MinIO 读取文件（返回 InputStream）
     */
    public InputStream getFileAsStream(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("读取文件失败: {}", objectName, e);
            throw new RuntimeException("读取 MinIO 文件失败: " + e.getMessage());
        }
    }

    /**
     * 确保 bucket 存在，不存在则创建
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucket())
                            .build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucket())
                                .build()
                );
                log.info("创建 MinIO bucket: {}", minioConfig.getBucket());
            }
        } catch (Exception e) {
            log.error("检查/创建 bucket 失败: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO bucket 初始化失败");
        }
    }
}
