package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.modern.FpvConfig;
import com.gluecode.fpvdrone.modern.FpvClient;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.GLFW;

/** Raw GLFW joystick support includes RC transmitters without a gamepad mapping. */
public final class ControllerReader {
    public static float throttle = -1, yaw, pitch, roll;
    private static boolean armButtonWasDown;
    public static boolean connected = true;

    public static void poll() {
        FpvConfig c = FpvConfig.current;
        connected = true;
        if (c.controller < 0) {
            throttle = FpvClient.throttleUp.isDown() ? 1 : FpvClient.throttleDown.isDown() ? -1 : -0.35f;
            yaw = axis(FpvClient.yawRight.isDown(), FpvClient.yawLeft.isDown());
            pitch = axis(FpvClient.pitchForward.isDown(), FpvClient.pitchBack.isDown());
            roll = axis(FpvClient.rollRight.isDown(), FpvClient.rollLeft.isDown());
            return;
        }
        if (!GLFW.glfwJoystickPresent(c.controller)) { reset(); connected = false; return; }
        FloatBuffer values = GLFW.glfwGetJoystickAxes(c.controller);
        int maxAxis = Math.max(Math.max(c.throttleAxis, c.rollAxis), Math.max(c.pitchAxis, c.yawAxis));
        if (values == null || values.remaining() <= maxAxis) { reset(); connected = false; return; }
        throttle = read(values, c.throttleAxis, c.invertThrottle, c.gamepad ? c.deadzone : 0);
        // A centered gamepad stick means motors idle. Push upward to add thrust.
        if (c.gamepad && !c.mode3d) throttle = Math.max(0, throttle) * 2 - 1;
        yaw = read(values, c.yawAxis, c.invertYaw, c.deadzone);
        pitch = read(values, c.pitchAxis, c.invertPitch, c.deadzone);
        roll = read(values, c.rollAxis, c.invertRoll, c.deadzone);
        if (c.armButton >= 0) {
            ByteBuffer buttons = GLFW.glfwGetJoystickButtons(c.controller);
            boolean down = buttons != null && c.armButton < buttons.remaining() && buttons.get(c.armButton) == GLFW.GLFW_PRESS;
            if (down && !armButtonWasDown) FpvClient.flight.toggle();
            armButtonWasDown = down;
        }
    }
    private static float axis(boolean plus, boolean minus) { return (plus ? 1f : 0f) - (minus ? 1f : 0f); }
    private static float read(FloatBuffer values, int channel, boolean invert, float deadzone) {
        float value = values.get(channel) * (invert ? -1 : 1);
        if (!Float.isFinite(value)) return 0;
        value = Math.clamp(value, -1f, 1f);
        if (Math.abs(value) <= deadzone) return 0;
        return Math.copySign((Math.abs(value) - deadzone) / (1 - deadzone), value);
    }
    public static void reset() { throttle = -1; yaw = pitch = roll = 0; armButtonWasDown = false; }
    public static float getThrottle() { return throttle; }
    public static float getYaw() { return yaw; }
    public static float getPitch() { return pitch; }
    public static float getRoll() { return roll; }
}
