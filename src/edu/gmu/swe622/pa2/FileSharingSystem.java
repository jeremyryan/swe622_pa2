package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by jmr on 11/4/17.
 */
public interface FileSharingSystem extends Remote {

    Download download(String remoteFile, long startAt) throws IOException;
    Upload upload(String remoteFile, long length) throws IOException;
    void rm(String fileName) throws IOException;
    List<String> dir(String dirName) throws IllegalArgumentException, IOException;
    void rmdir(String dirName) throws IOException;
    void mkdir(String dirName) throws IOException;
    void shutdown() throws RemoteException;
}
