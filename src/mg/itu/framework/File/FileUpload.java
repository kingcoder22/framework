package mg.itu.framework.File;

public class FileUpload {
    public String fileName;
    public String fileType;
    public long fileSize;
    public byte[] fileData;

    public FileUpload(String fileName, String fileType, long fileSize, byte[] fileData) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileData = fileData;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getFileData() {
        return fileData;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
    public void save(String filePath) throws Exception {
        java.nio.file.Path path = java.nio.file.Paths.get(filePath);
        java.nio.file.Files.write(path, fileData);
    }
}