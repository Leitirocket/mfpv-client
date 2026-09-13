package com.gluecode.fpvdrone.modern;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class FpvClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fpvdrone");
    public static final FlightController flight = new FlightController();
    public static KeyMapping arm, settings, throttleUp, throttleDown, yawLeft, yawRight, pitchForward, pitchBack, rollLeft, rollRight;
    @Override
    public void onInitializeClient() {
        FpvConfig.load();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fpvdrone", "controls"));
        arm = key("arm", GLFW.GLFW_KEY_R, category); settings = key("settings", GLFW.GLFW_KEY_O, category);
        throttleUp = key("throttle_up", GLFW.GLFW_KEY_SPACE, category); throttleDown = key("throttle_down", GLFW.GLFW_KEY_LEFT_SHIFT, category);
        yawLeft = key("yaw_left", GLFW.GLFW_KEY_A, category); yawRight = key("yaw_right", GLFW.GLFW_KEY_D, category);
        pitchForward = key("pitch_forward", GLFW.GLFW_KEY_W, category); pitchBack = key("pitch_back", GLFW.GLFW_KEY_S, category);
        rollLeft = key("roll_left", GLFW.GLFW_KEY_Q, category); rollRight = key("roll_right", GLFW.GLFW_KEY_E, category);
        ClientTickEvents.START_CLIENT_TICK.register(mc -> {
            if (mc.player != null && mc.gui.screen() == null && mc.isWindowActive()) ControllerReader.poll();
            flight.checkState();
            while (arm.consumeClick()) { if (mc.gui.screen() == null) flight.toggle(); }
            while (settings.consumeClick()) {
                flight.disarm();
                mc.gui.setScreen(new FpvSettingsScreen(mc.gui.screen()));
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath("fpvdrone", "telemetry"), FpvHud::render);
    }
    private static KeyMapping key(String name, int key, KeyMapping.Category category) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.fpvdrone." + name, InputConstants.Type.KEYSYM, key, category));
    }
}
