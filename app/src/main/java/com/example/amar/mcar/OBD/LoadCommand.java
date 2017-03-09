package com.example.amar.mcar.OBD;

import com.example.amar.mcar.enums.AvailableCommandNames;

public class LoadCommand extends PercentageObdCommand {

    public LoadCommand() {
        super("01 04");
    }

    public LoadCommand(LoadCommand other) {
        super(other);
    }

    @Override
    public String getName() {
        return AvailableCommandNames.ENGINE_LOAD.getValue();
    }

}
