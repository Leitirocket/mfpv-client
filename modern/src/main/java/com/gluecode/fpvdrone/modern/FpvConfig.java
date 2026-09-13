package com.gluecode.fpvdrone.modern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Values exposed to pilots are in kg, mm, inches, degrees and rpm/V. */
public final class FpvConfig {
    public static FpvConfig current = new FpvConfig();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public int controller = -1;
    public int throttleAxis = 1, rollAxis = 2, pitchAxis = 3, yawAxis = 0;
    public int armButton = -1;
    public boolean invertThrottle = true, invertRoll, invertPitch, invertYaw;
    public boolean gamepad = true, advancedPhysics = true, mode3d = false;
    public float deadzone = 0.04f;
    public float rcRate = 1.0f, superRate = 0.7f, expo = 0.2f;
    public float massKg = 0.65f, motorKv = 2400f, motorWidthMm = 22f, motorHeightMm = 7f;
    public int batteryCells = 4, blades = 3;
    public float propDiameterInches = 5f, propPitchInches = 4.6f, propWidthMm = 11f;
    public float cameraAngle = 25f;
    public boolean showHud = true;

    public static void load() {
        Path path = path();
        try {
            if (Files.exists(path)) {
                FpvConfig loaded = GSON.fromJson(Files.readString(path), FpvConfig.class);
                if (loaded != null) current = loaded;
            }
        } catch (Exception ex) {
            FpvClient.LOGGER.warn("Cannot load FPV settings; using defaults", ex);
        }
        current.validate();
    }

    public static void save() {
        current.validate();
        try {
            Path path = path(), temp = path.resolveSibling(path.getFileName() + ".tmp");
            Files.createDirectories(path.getParent());
            Files.writeString(temp, GSON.toJson(current));
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            FpvClient.LOGGER.error("Cannot save FPV settings", ex);
        }
    }

    private static Path path() { return FabricLoader.getInstance().getConfigDir().resolve("fpvdrone-26.json"); }
    public void validate() {
        controller = Math.clamp(controller, -1, 15);
        throttleAxis = Math.clamp(throttleAxis, 0, 31); rollAxis = Math.clamp(rollAxis, 0, 31);
        pitchAxis = Math.clamp(pitchAxis, 0, 31); yawAxis = Math.clamp(yawAxis, 0, 31);
        armButton = Math.clamp(armButton, -1, 63);
        deadzone = bound(deadzone, 0f, 0.3f, 0.04f);
        rcRate = bound(rcRate, 0.1f, 3f, 1f); superRate = bound(superRate, 0f, 0.95f, 0.7f);
        expo = bound(expo, 0f, 1f, 0.2f); cameraAngle = bound(cameraAngle, -45f, 85f, 25f);
        massKg = bound(massKg, 0.02f, 10f, 0.65f); motorKv = bound(motorKv, 100f, 30000f, 2400f);
        motorWidthMm = bound(motorWidthMm, 8f, 60f, 22f); motorHeightMm = bound(motorHeightMm, 3f, 40f, 7f);
        propDiameterInches = bound(propDiameterInches, 1f, 15f, 5f); propPitchInches = bound(propPitchInches, 0.5f, 15f, 4.6f);
        propWidthMm = bound(propWidthMm, 2f, 40f, 11f);
        batteryCells = Math.clamp(batteryCells, 1, 12); blades = Math.clamp(blades, 2, 6);
    }
    private static float bound(float n, float min, float max, float fallback) {
        return Float.isFinite(n) ? Math.clamp(n, min, max) : fallback;
    }
}
