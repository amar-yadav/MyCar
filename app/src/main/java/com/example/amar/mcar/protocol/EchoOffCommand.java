package com.example.amar.mcar.protocol;

public class EchoOffCommand extends ObdProtocolCommand {

    /**
     * <p>Constructor for EchoOffCommand.</p>
     */
    public EchoOffCommand() {
        super("AT E0");
    }

    public EchoOffCommand(EchoOffCommand other) {
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
        return "Echo Off";
    }

}
