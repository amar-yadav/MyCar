package com.example.amar.mcar.protocol;

public class ObdWarmstartCommand extends ObdProtocolCommand {

    public ObdWarmstartCommand() {
        super("AT WS");
    }

    public ObdWarmstartCommand(ObdWarmstartCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getFormattedResult() {
        return getResult();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Warmstart OBD";
    }

}
