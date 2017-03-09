package com.example.amar.mcar.protocol;

import com.example.amar.mcar.enums.AvailableCommandNames;

public class AvailablePidsCommand_21_40 extends AvailablePidsCommand {

    /**
     * Default ctor.
     */
    public AvailablePidsCommand_21_40() {
        super("01 20");
    }


    public AvailablePidsCommand_21_40(AvailablePidsCommand_21_40 other) {
        super(other);
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.PIDS_21_40.getValue();
    }
}
