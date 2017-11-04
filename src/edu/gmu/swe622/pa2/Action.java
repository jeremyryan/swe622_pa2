package edu.gmu.swe622.pa2;

/**
 * Actions representing functionality implemented by the File Sharing System.
 */
public enum Action {
    SHUTDOWN("shutdown", 0),
    RM("rm", 1),
    RMDIR("rmdir", 1),
    UPLOAD("upload", 2),
    DOWNLOAD("download", 2),
    DIR("dir", 1),
    MKDIR("mkdir", 1);

    private int numargs;
    private String name;

    /**
     * Constructor initializing instance with a name and the number of command line arguments required by the
     * action.
     * @param name  the action name that should be used on the command line when invoking the client
     * @param numargs  the number of arguments required by this action
     */
    Action(String name, int numargs) {
        this.name = name;
        this.numargs = numargs;
    }

    /**
     * Getter for action name.
     * @return  the name of the action as it should appear on the command line
     */
    public String getName() {
        return this.name;
    }

    /**
     * Getter for the number of arguments required by the action.
     * @return  the number of command line arguments required by the action
     */
    public int getNumArgs() {
        return this.numargs;
    }

    /**
     * Finds the instance of Action using the name.
     * @param name  the name of the action to find
     * @return  the action with the specified name, or null if no action with that name exists
     */
    public static Action findByName(String name) {
        for (Action action : Action.values()) {
            if (name.equalsIgnoreCase(action.name)) {
                return action;
            }
        }
        return null;
    }
}
