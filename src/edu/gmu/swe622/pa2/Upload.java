package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Interface for uploading a file remotely.
 */
public interface Upload extends Remote {

    void write(int[] bytes) throws IOException;

    long fileSize() throws IOException;

    void close() throws IOException;
}
