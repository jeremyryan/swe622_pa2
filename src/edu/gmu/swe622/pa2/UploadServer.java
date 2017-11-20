package edu.gmu.swe622.pa2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * Implements remote Upload interface.
 */
public class UploadServer implements Upload {

    private RandomAccessFile randomAccessFile;
    private Path filePath;
    private long length;
    private long bytesUploaded;

    /**
     * Constructor.
     * @param filePath the path to upload the file to
     * @param length  the length of the file to upload
     * @throws IOException if there is a communication error
     */
    public UploadServer(Path filePath, long length) throws IOException {
        this.filePath = filePath;
        this.length = length;
        File file = this.filePath.toFile();
        if (! file.exists()) {
            file.createNewFile();
        }
        this.bytesUploaded = file.length();
        this.randomAccessFile = new RandomAccessFile(file, "rw");
        if (this.bytesUploaded > 0 && this.bytesUploaded < this.length) {
            randomAccessFile.seek(this.bytesUploaded);
        } else {
            // if the existing file is >= to the size of the file being uploaded, overwrite it
            this.bytesUploaded = 0;
        }
    }

    /**
     * Returns the size of the existing file.
     * @return  the count of bytes in the file, only > 0 if it was already partly uploaded
     * @throws IOException if there is a communication error
     */
    @Override
    public long fileSize() throws IOException {
        return this.randomAccessFile.length();
    }

    /**
     * Writes the bytes from the buffer to the file being uploaded. If the number of bytes written exceeds the total
     * file size, the remaining bytes in the buffer are ignored.
     * @param bytes  the bytes to write
     * @throws IOException if there is a communication error
     */
    @Override
    public void write(int[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i++) {
            if (this.bytesUploaded++ >= this.length) break;
            this.randomAccessFile.write(bytes[i]);
        }
    }

    /**
     * Closes the file.
     * @throws IOException if there is a communication error
     */
    @Override
    public void close() throws IOException {
        this.randomAccessFile.close();
    }
}
