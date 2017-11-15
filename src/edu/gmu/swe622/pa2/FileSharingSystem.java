package edu.gmu.swe622.pa2;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by jmr on 11/4/17.
 */
public interface FileSharingSystem extends Remote {

    public Download download(String remoteFile, long startAt) throws IOException;
    public Upload upload(String remoteFile, long length) throws IOException;
    public void rm(String fileName) throws IOException;
    public List<String> dir(String dirName) throws IllegalArgumentException, IOException;
    public void rmdir(String dirName) throws IOException;
    public void mkdir(String dirName) throws IOException;
    public void shutdown() throws RemoteException;
    public Long getFileSize(String fileName) throws IOException;
}
