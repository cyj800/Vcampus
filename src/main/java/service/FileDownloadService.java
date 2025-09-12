package service;

import model.FileInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloadService {

    // 下载文件到本地
    public boolean downloadFile(String sourceFilePath, String destinationPath) throws ServiceException {
        try {
            Path sourcePath = Paths.get(sourceFilePath);
            Path destPath = Paths.get(destinationPath);

            // 检查源文件是否存在
            if (!Files.exists(sourcePath)) {
                throw new ServiceException("源文件不存在: " + sourceFilePath);
            }

            // 确保目标目录存在
            Path parentDir = destPath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // 复制文件
            Files.copy(sourcePath, destPath);
            return true;

        } catch (IOException e) {
            throw new ServiceException("文件下载失败: " + e.getMessage(), e);
        }
    }

    // 获取文件信息
    public FileInfo getFileInfo(String filePath) throws ServiceException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ServiceException("文件不存在: " + filePath);
            }

            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileName(path.getFileName().toString());
            fileInfo.setFileSize(Files.size(path));
            fileInfo.setLastModified(Files.getLastModifiedTime(path).toInstant());
            fileInfo.setFilePath(filePath);

            return fileInfo;

        } catch (IOException e) {
            throw new ServiceException("获取文件信息失败: " + e.getMessage(), e);
        }
    }

    // 验证文件权限
    public boolean validateFileAccess(String filePath, String userId) throws ServiceException {
        // 这里应该实现文件访问权限验证逻辑
        // 简化实现：检查文件路径是否在允许的目录内
        try {
            Path path = Paths.get(filePath);
            Path uploadsDir = Paths.get("uploads");

            // 检查文件是否在uploads目录下
            return path.startsWith(uploadsDir);

        } catch (Exception e) {
            throw new ServiceException("验证文件访问权限失败: " + e.getMessage(), e);
        }
    }
}