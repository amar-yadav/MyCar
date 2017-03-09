package com.example.amar.mcar.protocol;

public class TimeoutCommand extends ObdProtocolCommand {

    /**
     * <p>Constructor for TimeoutCommand.</p>
     *
     * @param timeout value between 0 and 255 that multiplied by 4 results in the
     *                desired timeout in milliseconds (ms).
     */
    public TimeoutCommand(int timeout) {
        super("AT ST " + Integer.toHexString(0xFF & timeout));
    }

    public TimeoutCommand(TimeoutCommand other) {
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
        return "Timeout";
    }

}
