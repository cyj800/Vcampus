package service;

import model.FileInfo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileService {
    private static final String UPLOAD_BASE_DIR = "uploads/assignments/";

    public FileService() {
        // 确保基础上传目录存在
        createDirectory(UPLOAD_BASE_DIR);
    }

    /**
     * 上传学生作业文件
     * @param tempFile 临时文件
     * @param originalFileName 原始文件名
     * @param assignmentId 作业ID
     * @param studentId 学生ID
     * @return 文件存储路径
     */
    public String uploadAssignmentFile(File tempFile, String originalFileName,
                                       int assignmentId, String studentId) throws ServiceException {
        try {
            // 生成文件存储路径
            String filePath = generateAssignmentFilePath(originalFileName, assignmentId, studentId);

            // 确保目录存在
            Path directory = Paths.get(filePath).getParent();
            if (directory != null) {
                createDirectory(directory.toString());
            }

            // 移动文件到目标位置
            Path targetPath = Paths.get(filePath);
            Files.move(tempFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return filePath;

        } catch (IOException e) {
            throw new ServiceException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成作业文件存储路径
     */
    private String generateAssignmentFilePath(String originalFileName,
                                              int assignmentId, String studentId) {
        // 格式: uploads/assignments/{assignmentId}/{studentId}_{timestamp}_{originalFileName}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String safeFileName = sanitizeFileName(originalFileName);

        return UPLOAD_BASE_DIR + assignmentId + "/" +
                studentId + "_" + timestamp + "_" + safeFileName;
    }

    /**
     * 验证文件类型
     */
    public boolean validateFileType(String fileName, String allowedTypes) {
        if (allowedTypes == null || allowedTypes.trim().isEmpty()) {
            return true; // 如果没有限制，允许所有类型
        }

        String fileExtension = getFileExtension(fileName).toLowerCase();
        String[] allowedExtensions = allowedTypes.toLowerCase().split(",");

        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证文件大小
     */
    public boolean validateFileSize(long fileSize, Integer maxSize) {
        if (maxSize == null || maxSize <= 0) {
            return true; // 如果没有限制，允许任意大小
        }
        return fileSize <= maxSize;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建目录
     */
    private void createDirectory(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 清理文件名中的非法字符
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed_file";
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }

    /**
     * 下载文件到本地
     */
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

    /**
     * 获取文件信息
     */
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

    /**
     * 验证文件权限
     */
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

    /**
     * 读取文件内容为字节数组（用于文件传输）
     */
    public byte[] readFileToBytes(String filePath) throws ServiceException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ServiceException("文件不存在: " + filePath);
            }
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ServiceException("读取文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件大小
     */
    public long getFileSize(String filePath) throws ServiceException {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new ServiceException("文件不存在: " + filePath);
            }
            return Files.size(path);
        } catch (IOException e) {
            throw new ServiceException("获取文件大小失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.exists(path);
        } catch (Exception e) {
            return false;
        }
    }
}