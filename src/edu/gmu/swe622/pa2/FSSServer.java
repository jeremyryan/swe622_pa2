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

    /**
     * Constructor.
     * @throws RemoteException  if there is a communication error
     */
    protected FSSServer() throws RemoteException {
    }

    /**
     * Validates the passed in parameters and returns a remote object to use for the download.
     * @param remoteFile  path to the file to download
     * @param startAt  the point in the file to start at
     * @return  a remote object to use for the download
     * @throws IOException  if there is a communication error
     * @throws IllegalArgumentException  if remoteFile or startAt does not have a valid value
     */
    @Override
    public Download download(String remoteFile, long startAt) throws IOException {
        if (remoteFile == null || remoteFile.length() == 0) {
            throw new IllegalArgumentException("remoteFile cannot be blank");
        }
        Path filePath = this.getPath(remoteFile);
        if (startAt < 0) {
            throw new IllegalArgumentException("startAt must be >= 0");
        }
        if (! this.validatePath(filePath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("A directory with that name already exists.");
        }
        Download download = new DownloadServer(filePath, startAt);
        return (Download) exportObject(download, 0);
    }

    /**
     * Validates the parameters and returns a remote object to use for the upload.
     * @param destinationPath  the path where the file should be uploaded
     * @param length  the number of bytes in the uploaded file
     * @return  a remote object to use for the upload
     * @throws IOException if there is a communication error
     * @throws IllegalArgumentException  if destinationPath or length does not have a valid value
     */
    @Override
    public Upload upload(String destinationPath, long length) throws IOException {
        if (destinationPath == null || destinationPath.length() == 0) {
            throw new IllegalArgumentException("destinationPath cannot be blank");
        }
        Path filePath = this.getPath(destinationPath);
        if (length < 0) {
            throw new IllegalArgumentException("startAt must be >= 0");
        }
        if (! this.validatePath(filePath)) {
            throw new IllegalArgumentException("Relative file paths are not supported");
        }
        if (Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("A directory with that name already exists.");
        }
        Upload upload = new UploadServer(filePath, length);
        return (Upload) exportObject(upload, 0);
    }

    /**
     * Removes the specified file from the file server repository.
     * @param fileName  the name of the file to remove.
     * @throws IOException if there is a communication error
     * @throws IllegalArgumentException  if fileName is invalid
     */
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

    /**
     * Returns a list of the files and directories in the specified directory.
     * @param dirName  the directory to list
     * @return  a list of the name of the files and directories in the directory specified by dirName
     * @throws IllegalArgumentException  if the path specified by dirName is invalid
     * @throws IOException if there is a communication error
     */
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

    /**
     * Deletes a directory from the FSS repository.
     * @param dirName  the name of the directory to delete
     * @throws IOException  if there is a communication error
     * @throws IllegalArgumentException  if dirName is an invalid path
     */
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

    /**
     * Creates a directory in the FSS repository.
     * @param dirName  the path and name of the directory to created
     * @throws IOException if there is a communication error
     * @throws IllegalArgumentException  if dirName is an invalid path
     */
    @Override
    public void mkdir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        Path filePath = this.getPath(dirName);
        Files.createDirectory(filePath);
    }

    /**
     * Shuts down the FSS server.
     * @throws RemoteException if there is a communication error
     */
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
