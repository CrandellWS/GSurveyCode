package videoworker.vlc;

/**
 * Created by Martin on 04.02.2016.
 */
public enum VWFormat {
    FORMAT_360P(360),
    FORMAT_480P(480),
    FORMAT_720P(720),
    FORMAT_1080P(1080),
    FORMAT_2160P(2160);
    private final int value;

    private VWFormat(int value)
    {
        this.value=value;
    }

    public int getValue() {
        return value;
    }
}
