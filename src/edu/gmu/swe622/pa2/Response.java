package edu.gmu.swe622.pa2;

/**
 * Class representing a response from the FSS server.
 */
public class Response extends Message {

    // some reusable Response instances
    public static final Response FILE_NOT_FOUND = new Response("File was not found");
    public static final Response DIRECTORY_NOT_FOUND = new Response("Directory was not found");
    public static final Response SUCCESSFUL = new Response();

    private boolean valid = true;
    private String errorMessage;

    /**
     * Constructor for responses for successful actions. valid is set to true and the error message is null.
     */
    public Response() {
    }

    /**
     * Constructor for responses which carry error information. volid is set to false to indicate an error.
     * @param errorMessage  message to be returned with the response
     */
    public Response(String errorMessage) {
        this.valid = false;
        this.errorMessage = errorMessage;
    }

    /**
     * Returns flag indicating whether the request could be completed or not.
     * @return  flag indicating whether request could be completed
     */
    public boolean isValid() {
        return this.valid;
    }

    /**
     * Returns error message.
     * @return  the error message
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

}
