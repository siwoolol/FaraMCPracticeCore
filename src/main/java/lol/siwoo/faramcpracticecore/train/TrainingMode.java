package lol.siwoo.faramcpracticecore.train;

public enum TrainingMode {
    STRAFE("Strafe Training", "Practice your movement skills"),
    AIM_TRACKER("Aim Tracker", "Improve your aim precision"),
    CPS_TESTER("CPS Tester", "Test your clicks per second"),
    W_TAP_TRAINER("W-Tap Trainer", "Master the w-tap technique");

    private final String displayName;
    private final String description;

    TrainingMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}