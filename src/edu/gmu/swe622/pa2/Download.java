package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;

/**
 * Created by jmr on 11/12/17.
 */
public interface Download extends Remote {

    public long fileSize() throws IOException;
    public int[] read() throws IOException;

}
