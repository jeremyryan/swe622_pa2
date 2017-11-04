package edu.gmu.swe622.pa2;

/**
 * Request to send from the client to the server.
 */
public class Request extends Message {

    private Action action;

    /**
     * Constructor.
     * @param action  the action to be carried out by the client.
     * @throws IllegalArgumentException  if action is null
     */
    public Request(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("action cannot be null");
        }
        this.action = action;
    }

    /**
     * Getter for request action.
     * @return  the action set for this request
     */
    public Action getAction() {
        return this.action;
    }

    /**
     * Setter for request action.
     * @param action  the action to set for this request
     */
    public void setAction(Action action) {
        this.action = action;
    }

}
