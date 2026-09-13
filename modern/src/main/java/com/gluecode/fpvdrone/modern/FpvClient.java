package com.gluecode.fpvdrone.modern;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Initial build harness; gameplay port follows after toolchain verification. */
public final class FpvClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("fpvdrone");
    @Override
    public void onInitializeClient() {
        LOGGER.info("Minecraft FPV 26.2 development build initialized");
    }
}
