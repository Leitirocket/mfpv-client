package com.gluecode.fpvdrone.input;
import com.gluecode.fpvdrone.modern.FpvConfig;
public final class ControllerConfig {
    public static float getYawRate() { return FpvConfig.current.rcRate; }
    public static float getPitchRate() { return FpvConfig.current.rcRate; }
    public static float getRollRate() { return FpvConfig.current.rcRate; }
    public static float getYawSuper() { return FpvConfig.current.superRate; }
    public static float getPitchSuper() { return FpvConfig.current.superRate; }
    public static float getRollSuper() { return FpvConfig.current.superRate; }
    public static float getYawExpo() { return FpvConfig.current.expo; }
    public static float getPitchExpo() { return FpvConfig.current.expo; }
    public static float getRollExpo() { return FpvConfig.current.expo; }
}
