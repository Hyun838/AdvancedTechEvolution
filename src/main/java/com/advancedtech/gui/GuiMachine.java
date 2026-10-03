package com.advancedtech.gui;

import com.advancedtech.tile.TileEntityMachine;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;

/** GUI без внешних текстур (рисуется прямоугольниками) — замените на свой .png при желании. */
@SideOnly(Side.CLIENT)
public class GuiMachine extends GuiContainer {
    private final TileEntityMachine tile;

    private static final int EX = 10, EY = 20, EW = 8, EH = 56;     // шкала энергии
    private static final int HX = 24;                               // шкала тепла

    public GuiMachine(InventoryPlayer inv, TileEntityMachine tile) {
        super(new ContainerMachine(inv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float pt, int mx, int my) {
        int l = guiLeft, t = guiTop;
        drawRect(l, t, l + xSize, t + ySize, 0xFFC6C6C6);
        for (Slot s : inventorySlots.inventorySlots) {
            drawRect(l + s.xPos - 1, t + s.yPos - 1, l + s.xPos + 17, t + s.yPos + 17, 0xFF373737);
            drawRect(l + s.xPos, t + s.yPos, l + s.xPos + 16, t + s.yPos + 16, 0xFF8B8B8B);
        }
        // Энергия
        drawBar(l + EX, t + EY, EW, EH, ratio(tile.guiEnergy(), tile.guiMaxEnergy()), 0xFFCC2222);
        // Тепло (20..150)
        float heatRatio = (tile.guiHeat() - TileEntityMachine.AMBIENT)
                / (TileEntityMachine.EXPLODE - TileEntityMachine.AMBIENT);
        int heatColor = tile.guiHeat() >= TileEntityMachine.OVERHEAT ? 0xFFFF2200 : 0xFFFF9900;
        drawBar(l + HX, t + EY, EW, EH, heatRatio, heatColor);
        // Прогресс
        drawBar(l + 80, t + 38, 26, 8, ratio(tile.guiProgress(), tile.guiMaxProgress()), 0xFF22AA22);
    }

    private static float ratio(int v, int max) { return max <= 0 ? 0 : Math.min(1f, (float) v / max); }

    private void drawBar(int x, int y, int w, int h, float r, int color) {
        r = Math.max(0, Math.min(1, r));
        drawRect(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF373737);
        drawRect(x, y, x + w, y + h, 0xFF555555);
        if (w > h) drawRect(x, y, x + Math.round(w * r), y + h, color);               // горизонтальная
        else { int f = Math.round(h * r); drawRect(x, y + h - f, x + w, y + h, color); } // вертикальная
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        super.drawScreen(mx, my, pt);
        renderHoveredToolTip(mx, my);
        int l = guiLeft, t = guiTop;
        if (in(mx, my, l + EX, t + EY, EW, EH))
            drawHoveringText(Arrays.asList(tile.guiEnergy() + " / " + tile.guiMaxEnergy() + " FE",
                    tile.getEnergy().getTier().name()), mx, my);
        else if (in(mx, my, l + HX, t + EY, EW, EH))
            drawHoveringText(Arrays.asList(String.format("Тепло: %.0f °", tile.guiHeat()),
                    tile.guiHeat() >= TileEntityMachine.OVERHEAT ? "ПЕРЕГРЕВ!" : "Норма"), mx, my);
    }

    private static boolean in(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
