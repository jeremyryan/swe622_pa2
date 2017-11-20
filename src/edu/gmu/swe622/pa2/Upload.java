package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Created by jmr on 11/14/17.
 */
public interface Upload extends Remote {

    void write(int[] bytes) throws IOException;

    long fileSize() throws IOException;

    void close() throws IOException;
}
