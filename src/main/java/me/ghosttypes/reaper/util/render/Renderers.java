package me.ghosttypes.reaper.util.render;

import me.ghosttypes.reaper.util.misc.MathUtil;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.util.math.BlockPos;

/**
 * Rendering utilities for block ESP with fade effects.
 * Contains SimpleBlockRender for basic block fade rendering.
 *
 * Additional render classes (SimpleBedRender, SimpleAnchorRender, SimpleBlockFadeIn)
 * will be added when porting combat modules.
 *
 * Ported from 1.19.4 to 1.21.11.
 */
public class Renderers {

    /**
     * Standard block rendering with fade-out effect.
     * Used by WideScaffold and other modules for temporary block highlighting.
     */
    public static class SimpleBlockRender {
        private final BlockPos pos;
        private final Color sideColor1;
        private final Color lineColor1;
        private int renderTicks;
        private final int fadeFactor;

        public SimpleBlockRender(BlockPos p, int renderTime, Color sideColor, Color lineColor, int fade) {
            pos = p;
            sideColor1 = new Color(sideColor.r, sideColor.g, sideColor.b, sideColor.a);
            lineColor1 = new Color(lineColor.r, lineColor.g, lineColor.b, lineColor.a);
            fadeFactor = fade;
            renderTicks = MathUtil.intToTicks(renderTime);
        }

        public void tick() {
            renderTicks--;
            sideColor1.a -= fadeFactor;
            lineColor1.a -= fadeFactor;
        }


        public boolean shouldRemove() {
            return renderTicks < 0;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Color getSideColor() {
            return sideColor1;
        }

        public Color getLineColor() {
            return lineColor1;
        }
    }
}
