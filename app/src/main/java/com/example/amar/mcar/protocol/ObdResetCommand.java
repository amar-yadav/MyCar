package com.example.amar.mcar.protocol;

public class ObdResetCommand extends ObdProtocolCommand {

    public ObdResetCommand() {
        super("AT Z");
    }

    public ObdResetCommand(ObdResetCommand other) {
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
        return "Reset OBD";
    }

}
