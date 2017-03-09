package com.example.amar.mcar.protocol;

public class LineFeedOffCommand extends ObdProtocolCommand {

    /**
     * <p>Constructor for LineFeedOffCommand.</p>
     */
    public LineFeedOffCommand() {
        super("AT L0");
    }

    public LineFeedOffCommand(LineFeedOffCommand other) {
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
        return "Line Feed Off";
    }

}
