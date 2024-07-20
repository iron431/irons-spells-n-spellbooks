package io.redspace.ironsspellbooks.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
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

    public static QuadBuilder quadBuilder() {
        return new QuadBuilder();
    }

    public static class QuadBuilder {
        List<Vector3f> verticies;
        List<Integer> colors;
        Integer light = null;
        @Nullable Matrix4f matrix;

        public QuadBuilder() {
            this.verticies = new ArrayList<>();
            this.colors = new ArrayList<>();
        }

        public QuadBuilder vertex(float x, float y) {
            this.verticies.add(new Vector3f(x, y, 0));
            return this;
        }

        public QuadBuilder vertex(float x, float y, float z) {
            this.verticies.add(new Vector3f(x, y, 0));
            return this;
        }

        public QuadBuilder matrix(Matrix4f matrix) {
            this.matrix = matrix;
            return this;
        }

        /**
         * Color in the format 0-1f, rgba
         */
        public QuadBuilder color(Vector4f color) {
            this.colors.add(colorf(color.x, color.y, color.z, color.w));
            return this;
        }

        public QuadBuilder color(int color) {
            this.colors.add(color);
            return this;
        }

        /**
         * Color in the format 0-1f, rgb
         */
        public QuadBuilder color(Vector3f color) {
            return color(new Vector4f(color.x, color.y, color.z, 1f));
        }

        /**
         * Color in the format 0-1f, rgb
         */
        public QuadBuilder color(float r, float g, float b) {
            return color(r, g, b, 1f);
        }

        /**
         * Color in the format 0-1f, rgba
         */
        public QuadBuilder color(float r, float g, float b, float a) {
            return color(new Vector4f(r, g, b, a));
        }

        public QuadBuilder light(int light) {
            this.light = light;
            return this;
        }

        public void build(VertexConsumer consumer) {
            for (int i = 0; i < verticies.size(); i++) {
                var vertex = verticies.get(i);
                int color;
                if (colors.isEmpty()) {
                    color = 0xFFFFFF;
                } else if (colors.size() == 1 || colors.size() != verticies.size()) {
                    color = colors.get(0);
                } else {
                    color = colors.get(i);
                }
                if (matrix != null) {
                    vertex = matrix.transformPosition(vertex.x, vertex.y, vertex.z, new Vector3f());
                }
                consumer.addVertex(vertex.x, vertex.y, vertex.z).setColor(color);
                if (light != null) {
                    consumer.setLight(light);
                }
            }
        }

        public void build(GuiGraphics graphics, RenderType renderType) {
            build(graphics.bufferSource().getBuffer(renderType));
            graphics.flush();
        }

        public void build(GuiGraphics graphics) {
            build(graphics, RenderType.gui());
        }

    }
}
