package com.gluecode.fpvdrone.modern;
import com.gluecode.fpvdrone.input.ControllerReader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import java.util.Locale;

public final class FpvHud {
    public static void render(GuiGraphicsExtractor g, DeltaTracker tracker) {
        FlightController f = FpvClient.flight;
        if (!f.isArmed() || !FpvConfig.current.showHud) return;
        var font = Minecraft.getInstance().font;
        int x = 10, y = 10;
        g.fill(x - 4, y - 4, x + 177, y + 63, 0xB0101826);
        g.text(font, "FPV  /  ARMED", x, y, 0xFF60DFCA);
        g.text(font, String.format(Locale.ROOT, "%5.1f km/h   MAX %5.1f", f.speed() * 3.6f, f.peakSpeed * 3.6f), x, y + 14, 0xFFFFFFFF);
        g.text(font, String.format(Locale.ROOT, "%02d:%02d   %.0f m", (int)f.flightSeconds / 60, (int)f.flightSeconds % 60, f.distance), x, y + 27, 0xFFCFD9E8);
        g.text(font, f.overheated() ? "MOTOR TEMPERATURE HIGH" : "R disarm  /  O settings", x, y + 43, f.overheated() ? 0xFFFF8B8B : 0xFFCFD9E8);
        sticks(g, g.guiWidth() - 104, g.guiHeight() - 72, ControllerReader.getYaw(), ControllerReader.getThrottle());
        sticks(g, g.guiWidth() - 52, g.guiHeight() - 72, ControllerReader.getRoll(), ControllerReader.getPitch());
    }
    private static void sticks(GuiGraphicsExtractor g, int x, int y, float horizontal, float vertical) {
        g.fill(x, y, x + 44, y + 44, 0xA0101826);
        g.fill(x + 21, y, x + 22, y + 44, 0xFF334155);
        g.fill(x, y + 21, x + 44, y + 22, 0xFF334155);
        int sx = x + 21 + Math.round(horizontal * 18), sy = y + 21 - Math.round(vertical * 18);
        g.fill(sx - 2, sy - 2, sx + 3, sy + 3, 0xFF60DFCA);
    }
}
