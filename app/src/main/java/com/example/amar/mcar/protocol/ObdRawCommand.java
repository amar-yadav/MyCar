package com.example.amar.mcar.protocol;

/**
 * This class allows for an unspecified command to be sent.
 */
public class ObdRawCommand extends ObdProtocolCommand {

    public ObdRawCommand(String command) {
        super(command);
    }

    /** {@inheritDoc} */
    @Override
    public String getFormattedResult() {
        return getResult();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Custom command " + getCommandPID();
    }

}
