package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Interface for downloading files remotely.
 */
public interface Download extends Remote {

    long fileSize() throws IOException;

    int[] read() throws IOException;

    void close() throws IOException;
}
