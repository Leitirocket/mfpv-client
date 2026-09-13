package com.gluecode.fpvdrone.modern;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Three compact pages, with raw input meters for transmitter setup. */
public final class FpvSettingsScreen extends Screen {
    private final Screen parent;
    private int page;
    private int left, top;
    private final List<Runnable> saveFields = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private String error = "";

    public FpvSettingsScreen(Screen parent) { super(Component.literal("Minecraft FPV")); this.parent = parent; }
    @Override protected void init() {
        saveFields.clear(); labels.clear();
        left = width / 2 - 150; top = 52;
        String[] tabs = {"Controller", "Flight", "Drone"};
        for (int i = 0; i < tabs.length; i++) {
            final int target = i;
            Button b = button(tabs[i], left + i * 102, 27, 96, () -> {
                if (save()) { page = target; rebuildWidgets(); }
            });
            b.active = page != i;
        }
        FpvConfig c = FpvConfig.current;
        if (page == 0) {
            button(controllerLabel(), left, top, 300, () -> {
                int next = c.controller + 1;
                while (next < 16 && !GLFW.glfwJoystickPresent(next)) next++;
                c.controller = next >= 16 ? -1 : next;
                FpvConfig.save(); rebuildWidgets();
            });
            button(c.gamepad ? "Input: gamepad (center = idle)" : "Input: RC radio (full throttle axis)", left, top + 24, 300,
                () -> { c.gamepad = !c.gamepad; rebuildWidgets(); });
            channel("Throttle", 2, c.throttleAxis, c.invertThrottle, v -> c.throttleAxis = v, v -> c.invertThrottle = v);
            channel("Roll", 3, c.rollAxis, c.invertRoll, v -> c.rollAxis = v, v -> c.invertRoll = v);
            channel("Pitch", 4, c.pitchAxis, c.invertPitch, v -> c.pitchAxis = v, v -> c.invertPitch = v);
            channel("Yaw", 5, c.yawAxis, c.invertYaw, v -> c.yawAxis = v, v -> c.invertYaw = v);
            button("Arm button: " + (c.armButton < 0 ? "keyboard only" : c.armButton), left, top + 144, 300,
                () -> { c.armButton = c.armButton >= 15 ? -1 : c.armButton + 1; rebuildWidgets(); });
        } else if (page == 1) {
            field("RC rate", 0, c.rcRate, v -> c.rcRate = v);
            field("Super rate", 1, c.superRate, v -> c.superRate = v);
            field("Expo", 2, c.expo, v -> c.expo = v);
            field("Deadzone", 3, c.deadzone, v -> c.deadzone = v);
            field("Camera tilt (degrees)", 4, c.cameraAngle, v -> c.cameraAngle = v);
            button("Physics: " + (c.advancedPhysics ? "advanced" : "simple"), left, top + 120, 146,
                () -> { if (save()) { c.advancedPhysics = !c.advancedPhysics; rebuildWidgets(); } });
            button("3D thrust: " + (c.mode3d ? "on" : "off"), left + 154, top + 120, 146,
                () -> { if (save()) { c.mode3d = !c.mode3d; rebuildWidgets(); } });
            button("Telemetry: " + (c.showHud ? "on" : "off"), left, top + 144, 300,
                () -> { if (save()) { c.showHud = !c.showHud; rebuildWidgets(); } });
        } else {
            field("Mass (kg)", 0, c.massKg, v -> c.massKg = v);
            field("Motor KV (rpm/V)", 1, c.motorKv, v -> c.motorKv = v);
            field("Prop diameter (inches)", 2, c.propDiameterInches, v -> c.propDiameterInches = v);
            field("Prop pitch (inches)", 3, c.propPitchInches, v -> c.propPitchInches = v);
            field("Motor width (mm)", 4, c.motorWidthMm, v -> c.motorWidthMm = v);
            field("Motor height (mm)", 5, c.motorHeightMm, v -> c.motorHeightMm = v);
            button("Battery: " + c.batteryCells + "S", left, top + 144, 146,
                () -> { if (save()) { c.batteryCells = c.batteryCells % 12 + 1; rebuildWidgets(); } });
            button("Blades: " + c.blades, left + 154, top + 144, 146,
                () -> { if (save()) { c.blades = c.blades == 6 ? 2 : c.blades + 1; rebuildWidgets(); } });
        }
        button("Save & close", left + 154, height - 26, 146, this::onClose);
        button("Reset this page", left, height - 26, 146, () -> {
            FpvConfig defaults = new FpvConfig();
            if (page == 0) {
                c.controller = -1; c.throttleAxis = 1; c.rollAxis = 2; c.pitchAxis = 3; c.yawAxis = 0;
                c.invertThrottle = true; c.invertRoll = c.invertPitch = c.invertYaw = false; c.armButton = -1; c.gamepad = true;
            } else if (page == 1) {
                c.rcRate = defaults.rcRate; c.superRate = defaults.superRate; c.expo = defaults.expo;
                c.deadzone = defaults.deadzone; c.cameraAngle = defaults.cameraAngle;
                c.advancedPhysics = true; c.mode3d = false; c.showHud = true;
            } else {
                c.massKg = defaults.massKg; c.motorKv = defaults.motorKv; c.propDiameterInches = defaults.propDiameterInches;
                c.propPitchInches = defaults.propPitchInches; c.motorWidthMm = defaults.motorWidthMm;
                c.motorHeightMm = defaults.motorHeightMm; c.batteryCells = defaults.batteryCells; c.blades = defaults.blades;
            }
            FpvConfig.save(); error = ""; rebuildWidgets();
        });
    }
    private void channel(String name, int row, int axis, boolean invert, Consumer<Integer> setAxis, Consumer<Boolean> setInvert) {
        button(name + ": axis " + axis, left, top + row * 24, 198,
            () -> { setAxis.accept((axis + 1) % 16); rebuildWidgets(); });
        button(invert ? "Reversed" : "Normal", left + 206, top + row * 24, 94,
            () -> { setInvert.accept(!invert); rebuildWidgets(); });
    }
    private void field(String label, int row, float value, Consumer<Float> setter) {
        labels.add(label);
        EditBox box = new EditBox(font, left + 205, top + row * 24, 95, 20, Component.literal(label));
        box.setMaxLength(12); box.setValue(Float.toString(value)); addRenderableWidget(box);
        saveFields.add(() -> {
            float parsed = Float.parseFloat(box.getValue().replace(',', '.'));
            if (!Float.isFinite(parsed)) throw new IllegalArgumentException(label);
            setter.accept(parsed);
        });
    }
    private Button button(String text, int x, int y, int w, Runnable action) {
        return addRenderableWidget(Button.builder(Component.literal(text), b -> action.run()).bounds(x, y, w, 20).build());
    }
    private String controllerLabel() {
        int id = FpvConfig.current.controller;
        if (id < 0) return "Keyboard — click to select USB controller";
        String name = GLFW.glfwGetJoystickName(id);
        return id + ": " + (name == null ? "disconnected" : name);
    }
    private boolean save() {
        try { saveFields.forEach(Runnable::run); FpvConfig.save(); error = ""; return true; }
        catch (NumberFormatException ex) { error = "Please enter valid numbers."; return false; }
        catch (IllegalArgumentException ex) { error = "Values must be finite."; return false; }
    }
    @Override public void onClose() { if (save()) minecraft.gui.setScreen(parent); }
    @Override public boolean isPauseScreen() { return true; }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(font, "MINECRAFT FPV  /  26.2", width / 2, 10, 0xFF60DFCA);
        for (int i = 0; i < labels.size(); i++) g.text(font, labels.get(i), left + 3, top + i * 24 + 6, 0xFFE2E8F0);
        if (page == 0 && height > 285 && FpvConfig.current.controller >= 0) {
            FloatBuffer axes = GLFW.glfwGetJoystickAxes(FpvConfig.current.controller);
            if (axes != null) for (int i = 0; i < Math.min(axes.remaining(), 8); i++) {
                int x = left + (i % 4) * 76, y = top + 176 + (i / 4) * 22;
                g.text(font, Integer.toString(i), x, y, 0xFFCBD5E1);
                g.fill(x + 12, y + 3, x + 69, y + 7, 0xFF334155);
                int marker = x + 12 + Math.round((axes.get(i) + 1) * 28);
                g.fill(marker, y + 1, marker + 2, y + 9, 0xFF60DFCA);
            }
        }
        if (!error.isEmpty()) g.centeredText(font, error, width / 2, height - 39, 0xFFFF8B8B);
    }
}
