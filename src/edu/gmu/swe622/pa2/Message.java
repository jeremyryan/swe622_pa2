package edu.gmu.swe622.pa2;

import java.io.Serializable;

/**
 * Base class for messages sent by FSS clients and servers.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object value;
    private Long fileSize;

    /**
     * A value passed with the message.
     * @return  the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value to be passed with the message.
     * @param value  the value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the size of the file to be uploaded/downloaded.
     * @return  the file size in bytes
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the size of the file to be uploaded/downloaded.
     * @param fileSize  the file size in bytes
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

}
