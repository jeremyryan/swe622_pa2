package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by jmr on 11/12/17.
 */
public class DownloadServer implements Download {

    private RandomAccessFile randomAccessFile;
    private Path filePath;


    public DownloadServer(Path filePath, long startAt) throws IOException {
        this.filePath = filePath;
        this.randomAccessFile = new RandomAccessFile(filePath.toFile(), "r");
        if (startAt != 0) {
            this.randomAccessFile.seek(startAt);
        }
    }

    @Override
    public long fileSize() throws IOException {
        return Files.size(this.filePath);
    }

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
