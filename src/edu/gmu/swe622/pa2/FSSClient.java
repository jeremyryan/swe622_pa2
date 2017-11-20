package edu.gmu.swe622.pa2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Implements the client for the File Sharing System.
 */
public class FSSClient {

    private FileSharingSystem fss;

    /**
     * Constructor.
     * @throws IllegalArgumentException  if either hostName or port are null
     */
    public FSSClient(String hostName, Integer port) throws RemoteException, NotBoundException, MalformedURLException {
        if (hostName == null || port == null) {
            throw new IllegalArgumentException("hostName and port must not be null");
        }
        String registeredName = "rmi://" + hostName + ":" + port + "/fss";
        this.fss = (FileSharingSystem) Naming.lookup(registeredName);
    }

    /**
     * Dispatches user input to client request handlers.
     * @param action  the action to carry out
     * @param args  the values required to carry out the action
     * @throws IOException  if there is an error while communicating with the server
     * @throws Exception  if the request could not be completed successfully
     */
    public void doAction(Action action, String[] args) throws Exception {
        switch (action) {
            case RM:
                this.rm(args[0]);
                break;
            case MKDIR:
                this.mkdir(args[0]);
                break;
            case DIR:
                this.dir(args[0]);
                break;
            case RMDIR:
                this.rmdir(args[0]);
                break;
            case UPLOAD:
                this.upload(args[0], args[1]);
                break;
            case DOWNLOAD:
                this.download(args[0], args[1]);
                break;
            case SHUTDOWN:
                this.shutdown();
                break;
            default:
                break;
        }
    }

    /**
     * Sends a request to remove the file specified by fileName from the server.
     * @param fileName the name of the file to remove
     * @throws IOException  if there is an error while communicating with the server
     * @throws IllegalArgumentException
     */
    private void rm(String fileName) throws IOException {
        if (fileName == null || fileName.length() == 0) {
            throw new IllegalArgumentException("fileName cannot be blank");
        }
        this.fss.rm(fileName);
        System.out.println("File removed");
    }

    /**
     * Sends a request to create a directory named by dirName to the server.
     * @param dirName the name of the directory to create
     * @throws IOException  if there is an error while communicating with the server
     * @throws IllegalArgumentException
     */
    private void mkdir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        this.fss.mkdir(dirName);
        System.out.println("Directory created");
    }

    /**
     * Sends a request to list the contents of a directory specified by dirName from the server.
     * The returned list of files and directories is then printed on stdout.
     * @param dirName the name of the directory on the server to list
     * @throws IllegalArgumentException
     * @throws IOException  if there is an error while communicating with the server
     */
    private void dir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        List<String> contents = this.fss.dir(dirName);
        System.out.println("Directory contents:");
        contents.stream().forEach(System.out::println);
    }

    /**
     * Sends a request to remove a directory specified by dirName from the server.
     * @param dirName  the name of the directory to delete.
     * @throws IOException  if there is an error while communicating with the server
     * @throws IllegalArgumentException  if dirName is not a valid path
     */
    private void rmdir(String dirName) throws IOException {
        if (dirName == null || dirName.length() == 0) {
            throw new IllegalArgumentException("dirName cannot be blank");
        }
        this.fss.rmdir(dirName);
        System.out.println("Directory removed");
    }

    /**
     * Sends a request to upload a file specified by localFilePath to the remote directory specified by
     * remoteDestination.
     * @param localFilePath path of the file to upload to the server
     * @param remoteDestination the name of the remote directory where the file should be created on
     *                          the server
     * @throws IOException if there is an error while communicating with the server
     * @throws IllegalArgumentException  if localFilePath is not a valid path
     */
    private void upload(String localFilePath, String remoteDestination)
            throws IllegalArgumentException, IOException {
        Path filePath = Paths.get(localFilePath);
        if (! Files.exists(filePath)) {
            throw new IllegalArgumentException("File could not be found: " + localFilePath);
        } else if (Files.isDirectory(filePath)) {
            throw new IllegalArgumentException("Directories cannot be uploaded: " + localFilePath);
        }

        Long fileSize = Files.size(filePath);

        Upload upload = this.fss.upload(remoteDestination, fileSize);
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath.toFile(), "r")) {
            Long bytesWritten = upload.fileSize();
            System.out.println("Uploading file...");
            float percentDone = 0f;
            if (bytesWritten != null && bytesWritten > 0 && bytesWritten < fileSize) {
                randomAccessFile.seek(bytesWritten);
                percentDone = (((float) bytesWritten) / fileSize) * 100;
                System.out.println(String.format("Skipping %d%% of upload", (int) percentDone));
            }
            int[] bytes = new int[Constants.BUFFER_SIZE];
            while (bytesWritten < fileSize) {
                for (int i = 0; i < Constants.BUFFER_SIZE; i++) {
                    int b = randomAccessFile.read();
                    if (b == -1) break;
                    bytes[i] = b;
                    bytesWritten++;
                    int percent = (int) ((((float) bytesWritten) / fileSize) * 100);
                    if (percent > percentDone) {
                        System.out.println(percent + "% uploaded");
                        System.out.flush();
                        percentDone += 10;
                    }
                }
                upload.write(bytes);
            }
        } finally {
            upload.close();
        }

        System.out.println("File uploaded");
    }

    /**
     * Sends a request to download a file, specified by remoteFile, from the server to the local
     * directory specified by destination.
     * @param remoteFile  the file to download from the server
     * @param destination the destination directory for the downloaded file
     * @throws IOException  if there is an error while communicating with the server
     * @throws IllegalArgumentException  if destination is not a valid path
     */
    private void download(String remoteFile, String destination) throws IOException {
        Path destinationPath = FileSystems.getDefault().getPath(destination);
        if (Files.isDirectory(destinationPath)) {
            throw new IllegalArgumentException("A directory with that name already exists.");
        } else if (! (destinationPath.getParent() == null || Files.exists(destinationPath.getParent()))) {
            throw new IllegalArgumentException("Destination directory could not be found.");
        }
        File file = destinationPath.toFile();
        Download download = this.fss.download(remoteFile, file.length());

        System.out.println("Downloading file...");
        float percentDone = 0f;
        float total = download.fileSize();
        long downloadedBytes = file.length();
        if (downloadedBytes >= total) {
            // if the existing file is >= the remote file size, overwrite it
            downloadedBytes = 0L;
        }
        if (! file.exists()) {
            file.createNewFile();
        }

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            if (downloadedBytes != 0 && downloadedBytes < total) {
                randomAccessFile.seek(downloadedBytes);
                percentDone = (((float) downloadedBytes) / total) * 100;
                System.out.println(String.format("Skipping %d%% of download", (int) percentDone));
            }

            int[] buffer;

            while (downloadedBytes < total) {
                buffer = download.read();

                for (int c : buffer) {
                    if (downloadedBytes++ >= total) break;
                    randomAccessFile.write(c);
                }
                int percent = (int) ((downloadedBytes / total) * 100);
                if (percent > percentDone) {
                    System.out.println(percent + "% downloaded");
                    System.out.flush();
                    percentDone += 10;
                }
            }
        }
        System.out.println("File downloaded");
    }

    /**
     * Sends a shutdown request to the server.
     * @throws RemoteException  if there is an error communicating with the server
     */
    private void shutdown() throws RemoteException {
        this.fss.shutdown();
        System.out.println("File Sharing System shut down.");
    }

}


