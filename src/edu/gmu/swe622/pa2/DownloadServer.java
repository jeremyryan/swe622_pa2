package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Implementation of the remote Download interface.
 */
public class DownloadServer implements Download {

    private RandomAccessFile randomAccessFile;
    private Path filePath;

    /**
     * Constructor.
     * @param filePath  the path of the file to be downloaded
     * @param startAt  the place in the file to start downloading
     * @throws IOException if there is a communication error
     */
    public DownloadServer(Path filePath, long startAt) throws IOException {
        this.filePath = filePath;
        this.randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
        if (startAt != 0) {
            this.randomAccessFile.seek(startAt);
        }
    }

    /**
     * Returns the number of bytes in the file to download.
     * @return  the count of bytes in the file
     * @throws IOException if there is a communication error
     */
    @Override
    public long fileSize() throws IOException {
        return Files.size(this.filePath);
    }

    /**
     * Reads Constants.BUFFER_SIZE bytes from the file to be downloaded and returns it in a buffer array. If the array
     * is longer than the total size of the file, the remaining bytes should be ignored.
     * @return  a buffer of Constants.BUFFER_SIZE bytes read from the file to download.
     * @throws IOException if there is a communication error
     */
    @Override
    public int[] read() throws IOException {
        int[] buffer = new int[Constants.BUFFER_SIZE];
        for (int i = 0; i < Constants.BUFFER_SIZE; i++) {
            int c = this.randomAccessFile.read();
            if (c == -1) break;
            buffer[i] = c;
        }
        return buffer;
    }
}
