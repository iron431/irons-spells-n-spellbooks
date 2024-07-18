package io.redspace.ironsspellbooks.render;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class VertexHelper {

    public static int color255(int pAlpha, int pRed, int pGreen, int pBlue) {
        return pAlpha << 24 | pRed << 16 | pGreen << 8 | pBlue;
    }

    public static int color255(int pRed, int pGreen, int pBlue) {
        return color255(255, pRed, pGreen, pBlue);
    }

    public static int colorf(float pAlpha, float pRed, float pGreen, float pBlue) {
        return color255((int) (255 * pAlpha), (int) (255 * pRed), (int) (255 * pGreen), (int) (255 * pBlue));
    }

    public static int colorf(float pRed, float pGreen, float pBlue) {
        return colorf(1f, pRed, pGreen, pBlue);
    }

    public static class QuadBuilder2d {
        List<Vector2f> verticies;
        List<Vector4f> colors;

        public QuadBuilder2d() {
            this.verticies = new ArrayList<>();
            this.colors = new ArrayList<>();
        }

        public QuadBuilder2d vertex(float x, float y) {
            this.verticies.add(new Vector2f(x, y));
            return this;
        }

        /**
         * Color in the format 0-1f, rgba
         */
        public QuadBuilder2d color(Vector4f color) {
            this.colors.add(color);
            return this;
        }

        /**
         * Color in the format 0-1f, rgb
         */
        public QuadBuilder2d color(Vector3f color) {
            return color(new Vector4f(color.x, color.y, color.z, 1f));
        }

        /**
         * Color in the format 0-1f, rgb
         */
        public QuadBuilder2d color(float r, float g, float b) {
            return color(r, g, b, 1f);
        }

        /**
         * Color in the format 0-1f, rgba
         */
        public QuadBuilder2d color(float r, float g, float b, float a) {
            return color(new Vector4f(r, g, b, a));
        }

        public void build(GuiGraphics graphics, RenderType renderType) {
            var consumer = graphics.bufferSource().getBuffer(renderType);
            for (int i = 0; i < verticies.size(); i++) {
                var vertex = verticies.get(i);
                Vector4f color;
                if (colors.isEmpty()) {
                    color = new Vector4f(1, 1, 1, 1);
                } else if (colors.size() == 1 || colors.size() != verticies.size()) {
                    color = colors.get(0);
                } else {
                    color = colors.get(i);
                }
                consumer.addVertex(vertex.x, vertex.y, 0).setColor(FastColor.ARGB32.colorFromFloat(color.w, color.x, color.y, color.z));
            }
            graphics.flush();
        }

        public void build(GuiGraphics graphics) {
            build(graphics, RenderType.gui());
        }
    }
}
