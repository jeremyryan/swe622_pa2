package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Server for the File Sharing System.
 */
public class FSSServer extends UnicastRemoteObject implements FileSharingSystem {

    protected FSSServer() throws RemoteException {
    }

    @Override
    public Download download(String remoteFile, long startAt) throws IOException {
        Path filePath = this.getPath(remoteFile);
        Download download = new DownloadServer(filePath, startAt);
        return (Download) exportObject(download, 0);
    }

    @Override
    public Upload upload(String destinationPath, long length) throws IOException {
        Path filePath = this.getPath(destinationPath);
        Upload upload = new UploadServer(filePath, length);
        if (! this.validatePath(filePath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("A directory with that name already exists.");
        }
        return (Upload) exportObject(upload, 0);
    }

    @Override
    public void rm(String fileName) throws IOException {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("fileName cannot be blank");
        }
        Path filePath = this.getPath(fileName);
        if (! this.validatePath(filePath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (! Files.exists(filePath)) {
            throw new IllegalArgumentException("fileName could not be found");
        }
        if (Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("fileName is a directory");
        }
        Files.deleteIfExists(filePath);
    }

    @Override
    public List<String> dir(String dirName) throws IllegalArgumentException, IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        Path dirPath = this.getPath(dirName);
        if (! this.validatePath(dirPath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (! Files.exists(dirPath)) {
            throw new IllegalArgumentException("Directory not found");
        }
        if (! Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("Specified file is not a directory: " + dirPath);
        }
        List<String> fileNames = new ArrayList<>();
        Files.list(dirPath).forEach((path) -> fileNames.add(path.getFileName().toString()));
        return fileNames;
    }

    @Override
    public void rmdir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        Path filePath = this.getPath(dirName);
        if (! this.validatePath(filePath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (! Files.exists(filePath)) {
            throw new IllegalArgumentException("dirName could not be found");
        }
        if (! Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("dirName is not a directory");
        }
        Files.deleteIfExists(filePath);
    }

    @Override
    public void mkdir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        Path filePath = this.getPath(dirName);
        Files.createDirectory(filePath);
    }

    @Override
    public void shutdown() throws RemoteException {
        unexportObject(this, true);
    }

    /**
     * Verifies that the path sent by the client is not a relative path. Returns a response to be sent
     * back to the client with an appropriate error message if the path is not valid.
     * @param path  the filesystem path to validate
     * @return  a response with an error message if not valid, otherwise null
     */
    private boolean validatePath(Path path) {
        boolean valid = true;
        Path parent = Paths.get("..");
        Path cwd = Paths.get(".");
        for (int i = 0, count = path.getNameCount(); i < count; i++) {
            Path fileName = path.getName(i);
            if (fileName.equals(cwd) || fileName.equals(parent)) {
                valid = false;
                break;
            }
        }
        return valid;
    }

    /**
     * Returns a Path object representing the file named by fileName.
     * @param fileName  the name of the file
     * @return  a Path object representing the file named by fileName
     */
    private Path getPath(String fileName) {
        return FileSystems.getDefault().getPath(System.getProperty("user.dir"), fileName);
    }
}
