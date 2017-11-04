package edu.gmu.swe622.pa2;

import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for carrying out client requests within a separate thread. Handles verifying the request,
 * carrying out the request and reporting errors to the client.
 */
public class RequestHandler extends Thread {

    private ObjectInput objectIn;
    private ObjectOutput objectOut;
    private Socket sock;
    private FSSServer server;

    /**
     * Constructor.
     * @param sock  the socket used to communicate with the client.
     * @param server  the server instance which initiated this thread
     * @throws IOException  if there is an error while communicating with the client
     */
    public RequestHandler(Socket sock, FSSServer server) throws IOException {
        this.server = server;
        this.sock = sock;
        this.objectOut = new ObjectOutputStream(sock.getOutputStream());
        this.objectOut.flush();
        this.objectIn = new ObjectInputStream(sock.getInputStream());
    }

    /**
     * Overridden implementation of Thread.run.
     */
    @Override
    public void run() {
        try {
            this.handle();
        } finally {
            try {
                this.sock.close();
            } catch (IOException exp) {
                exp.printStackTrace();
            }
        }
    }

    /**
     * Reads the client request and dispatches it to the appropriate handler method for the action requested.
     */
    private void handle() {
        try {
            Request request = (Request) this.objectIn.readObject();
            Response response = null;
            switch (request.getAction()) {
                case RM:
                    response = this.rm(request);
                    break;
                case MKDIR:
                    response = this.mkdir(request);
                    break;
                case DIR:
                    response = this.dir(request);
                    break;
                case RMDIR:
                    response = this.rmdir(request);
                    break;
                case UPLOAD:
                    response = this.upload(request);
                    break;
                case DOWNLOAD:
                    response = this.download(request);
                    break;
                case SHUTDOWN:
                    this.shutdown();
                    break;
                default:
                    response = new Response("Invalid request");
                    break;
            }
            if (response != null) {
                this.writeResponse(response);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    /**
     * Returns a Path object representing the file named by fileName.
     * @param fileName  the name of the file
     * @return  a Path object representing the file named by fileName
     */
    private Path getPath(String fileName) {
        return FileSystems.getDefault().getPath(System.getProperty("user.dir"), fileName);
    }

    /**
     * Verifies that the path sent by the client is not a relative path. Returns a response to be sent
     * back to the client with an appropriate error message if the path is not valid.
     * @param path  the filesystem path to validate
     * @return  a response with an error message if not valid, otherwise null
     */
    private Response validatePath(Path path) {
        Response response = null;
        Path parent = Paths.get("..");
        Path cwd = Paths.get(".");
        for (int i = 0, count = path.getNameCount(); i < count; i++) {
            Path fileName = path.getName(i);
            if (fileName.equals(cwd) || fileName.equals(parent)) {
                response = new Response("Relative file paths are not supported");
                break;
            }
        }
        return response;
    }

    /**
     * Removes a file from the server disk.
     * @param request the client request, which should contain the name of the file to remove
     * @return a response indicating success if the file was removed, otherwise includes an error message
     * @throws IOException  if there is an error while communicating with the client
     */
    private Response rm(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("File name not specified");
        } else {
            String fileName = (String) request.getValue();
            Path filePath = this.getPath(fileName);
            response = this.validatePath(filePath);
            if (response == null) {
                if (Files.exists(filePath)) {
                    Files.deleteIfExists(filePath);
                    response = Response.SUCCESSFUL;
                } else {
                    response = Response.FILE_NOT_FOUND;
                }
            }
        }
        return response;
    }

    /**
     * Writes the uploaded file to the server disk.
     * @param request  the client request, which should contain the file being uploaded and the destination
     *                 directory
     * @return  a response indicating success if the file was uploaded, otherwise including an error message
     * @throws IOException  if there is an error while communicating with the client
     */
    private Response upload(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("No destination directory or file name specified");
        } else {
            String destination = (String) request.getValue();

            Path destinationPath = this.getPath(destination);
            response = this.validatePath(destinationPath);
            if (response == null) {
                if (Files.isDirectory(destinationPath)) {
                    response = new Response("A directory with that name already exists.");
                } else if (destinationPath.getParent() == null || Files.exists(destinationPath.getParent())) {
                    File uploadedFile = destinationPath.toFile();
                    Long fileSize = request.getFileSize();
                    response = new Response();
                    if (uploadedFile.exists()) {
                        response.setFileSize(uploadedFile.length());
                    } else {
                        uploadedFile.createNewFile();
                        response.setFileSize(0L);
                    }

                    this.objectOut.writeObject(response);
                    BufferedInputStream inStream = new BufferedInputStream(this.sock.getInputStream());
                    try (RandomAccessFile randomAccessFile = new RandomAccessFile(uploadedFile, "rw")) {
                        long uploadedBytes = uploadedFile.length();
                        if (fileSize != null && uploadedBytes > 0 && uploadedBytes < fileSize) {
                            randomAccessFile.seek(uploadedBytes);
                        } else {
                            // if the existing file is >= to the size of the file being uploaded, overwrite it
                            uploadedBytes = 0;
                        }
                        while (uploadedBytes++ < fileSize) {
                            int b = inStream.read();
                            randomAccessFile.write(b);
                        }
                    }
                    response = Response.SUCCESSFUL;
                } else {
                    response = Response.DIRECTORY_NOT_FOUND;
                }
            }
        }
        return response;
    }

    /**
     * Downloads a file from the server.
     * @param request the client request, which should contain the file name
     * @return a response indicating success if the file was downloaded, otherwise includes an error message
     * @throws IOException  if there is an error while communicating with the client
     */
    private Response download(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("No file name specified");
        } else {
            String fileName = (String) request.getValue();
            Path filePath = this.getPath(fileName);

            response = this.validatePath(filePath);
            if (response == null) {
                if (! Files.exists(filePath)) {
                    response = Response.FILE_NOT_FOUND;
                } else if (Files.isDirectory(filePath)) {
                    response = new Response("Directories cannot be downloaded");
                } else {
                    File file = filePath.toFile();
                    long totalBytes = file.length();
                    response = new Response();
                    response.setFileSize(totalBytes);
                    Long bytesUploaded = request.getFileSize();
                    this.writeResponse(response);
                    BufferedOutputStream outStream = new BufferedOutputStream(this.sock.getOutputStream());
                    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                        if (bytesUploaded != null && bytesUploaded > 0 && bytesUploaded < totalBytes) {
                            randomAccessFile.seek(bytesUploaded);
                        }
                        int b;
                        while (-1 != (b = randomAccessFile.read())) {
                            outStream.write(b);
                        }
                        outStream.flush();
                    }
                    response = Response.SUCCESSFUL;
                }
            }
        }
        return response;
    }

    /**
     * Creates a directory on the server.
     * @param request  the client request, which should contain the name of the directory
     * @return a response indicating success if the directory was created, otherwise includes an error message
     * @throws IOException  if there is an error while communicating with the client
     */
    private Response mkdir(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("No directory specified");
        } else {
            String dirName = (String) request.getValue();
            Path newDirPath = this.getPath(dirName);
            response = this.validatePath(newDirPath);
            if (response == null) {
                if (!Files.exists(newDirPath)) {
                    Files.createDirectory(newDirPath);
                    response = Response.SUCCESSFUL;
                } else {
                    response = new Response("Directory already exists");
                }
            }
        }
        return response;
    }

    /**
     * Lists files and directories in the directory specified by the client request.
     * @param request  request sent by client, which should contain the directory name
     * @return a response including the items in the directory if successful, otherwise includes an error message
     * @throws IOException
     */
    private Response dir(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("No directory specified");
        } else {
            String dirName = (String) request.getValue();
            Path dirPath = this.getPath(dirName);
            response = this.validatePath(dirPath);
            if (response == null) {
                if (Files.exists(dirPath)) {
                    if (Files.isDirectory(dirPath)) {
                        List<String> fileNames = new ArrayList<>();
                        Files.list(dirPath).forEach((path) -> fileNames.add(path.getFileName().toString()));
                        response = new Response();
                        response.setValue(fileNames);
                    } else {
                        response = new Response("Specified file is not a directory: " + dirPath);
                    }
                } else {
                    response = Response.DIRECTORY_NOT_FOUND;
                }
            }
        }
        return response;
    }

    /**
     * Removes a directory based on a client request, or reports if the directory does not exist.
     * @param request  the request object sent by the client, which should contain the directory name
     * @return a response indicating success if the directory was removed, otherwise includes an error message
     * @throws IOException  if there is an error while communicating with the client
     */
    private Response rmdir(Request request) throws IOException {
        Response response;
        if (request.getValue() == null) {
            response = new Response("Directory name not specified");
        } else {
            String dirName = (String) request.getValue();
            Path dirPath = this.getPath(dirName);
            response = this.validatePath(dirPath);
            if (response == null) {
                if (Files.exists(dirPath)) {
                    if (Files.isDirectory(dirPath)) {
                        if (Files.list(dirPath).count() != 0) {
                            response = new Response("The directory is not empty: " + dirPath);
                        } else {
                            Files.delete(dirPath);
                            response = Response.SUCCESSFUL;
                        }
                    } else {
                        response = new Response("The specified file is not a directory: " + dirPath);
                    }
                } else {
                    response = Response.DIRECTORY_NOT_FOUND;
                }
            }
        }
        return response;
    }

    /**
     * Calls the shutdown method on the server instance.
     * @throws IOException  if there is an error while communicating with the client
     */
    private void shutdown() throws IOException {
        Response response = new Response();
        this.writeResponse(response);
        this.server.shutdown();
    }

    /**
     * Writes a response to the client.
     * @param response  the response to send
     * @throws IOException  if there is an error while communicating with the client
     */
    private void writeResponse(Response response) throws IOException {
        this.objectOut.writeObject(response);
        this.objectOut.flush();
    }
}
