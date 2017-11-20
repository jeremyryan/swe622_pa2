package edu.gmu.swe622.pa2;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

/**
 * Created by jmr on 11/13/17.
 */
public class UploadServer implements Upload {

    private RandomAccessFile randomAccessFile;
    private Path filePath;
    private long length;
    private long bytesUploaded;

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


    @Override
    public long fileSize() throws IOException {
        return this.randomAccessFile.length();
    }

    @Override
    public void write(int[] bytes) throws IOException {
        for (int i = 0; i < bytes.length; i++) {
            if (this.bytesUploaded++ >= this.length) break;
            this.randomAccessFile.write(bytes[i]);
        }
    }

    @Override
    public void close() throws IOException {
        this.randomAccessFile.close();
    }
}
