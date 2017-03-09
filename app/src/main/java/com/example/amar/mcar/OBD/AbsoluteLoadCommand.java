package com.example.amar.mcar.OBD;

import com.example.amar.mcar.enums.AvailableCommandNames;

public class AbsoluteLoadCommand extends PercentageObdCommand {

    public AbsoluteLoadCommand() {
        super("01 43");
    }

    public AbsoluteLoadCommand(AbsoluteLoadCommand other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        int a = buffer.get(2);
        int b = buffer.get(3);
        percentage = (a * 256 + b) * 100 / 255;
    }

    public double getRatio() {
        return percentage;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.ABS_LOAD.getValue();
    }

}
