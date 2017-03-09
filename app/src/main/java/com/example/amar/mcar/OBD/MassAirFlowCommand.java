package com.example.amar.mcar.OBD;

import com.example.amar.mcar.enums.AvailableCommandNames;

/**
 * Mass Air Flow (MAF)
 */
public class MassAirFlowCommand extends ObdCommand {

    private float maf = -1.0f;

    /**
     * Default ctor.
     */
    public MassAirFlowCommand() {
        super("01 10");
    }

    /**
     * Copy ctor.
     *
     *
     */
    public MassAirFlowCommand(MassAirFlowCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        maf = (buffer.get(2) * 256 + buffer.get(3)) / 100.0f;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormattedResult() {
        return String.format("%.2f%s", maf, getResultUnit());
    }

    /** {@inheritDoc} */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(maf);
    }

    /** {@inheritDoc} */
    @Override
    public String getResultUnit() {
        return "g/s";
    }

    /**
     * <p>getMAF.</p>
     *
     * @return MAF value for further calculus.
     */
    public double getMAF() {
        return maf;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.MAF.getValue();
    }

}
