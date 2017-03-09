package com.example.amar.mcar.protocol;

public class SpacesOffCommand extends ObdProtocolCommand {

    public SpacesOffCommand() {
        super("ATS0");
    }

    public SpacesOffCommand(SpacesOffCommand other) {
        super(other);
    }

    @Override
    public String getFormattedResult() {
        return getResult();
    }

    @Override
    public String getName() {
        return "Spaces Off";
    }
}
