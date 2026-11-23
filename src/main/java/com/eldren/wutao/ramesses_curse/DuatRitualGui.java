package com.eldren.wutao.ramesses_curse;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/** Curved, buffered GUI geometry; no world framebuffer or post-processing dependencies. */
public final class DuatRitualGui {
    private DuatRitualGui() {}
    private static double time() { return (System.nanoTime() / 1_000_000L % 120000) / 1000.0; }

    private static void vertex(VertexConsumer out, Matrix4f m, double x, double y, int color) {
        out.vertex(m, (float)x, (float)y, 0).color(color).endVertex();
    }

    public static void arc(GuiGraphics g, double x, double y, double rx, double ry,
                           double start, double sweep, double thickness, int color) {
        if (sweep <= 0 || rx <= 0 || ry <= 0) return;
        VertexConsumer out = g.bufferSource().getBuffer(DuatUiRenderTypes.ui());
        Matrix4f m = g.pose().last().pose();
        int steps = Math.max(2, (int)Math.ceil(sweep * Math.max(rx, ry) / 3));
        for (int i = 0; i < steps; i++) {
            // GUI projection flips Y: emit counter-clockwise screen-space faces.
            double a = start + sweep * (i + 1) / steps, b = start + sweep * i / steps;
            vertex(out, m, x + rx * Math.cos(a), y + ry * Math.sin(a), color);
            vertex(out, m, x + rx * Math.cos(b), y + ry * Math.sin(b), color);
            vertex(out, m, x + Math.max(0, rx-thickness) * Math.cos(b), y + Math.max(0, ry-thickness) * Math.sin(b), color);
            vertex(out, m, x + Math.max(0, rx-thickness) * Math.cos(a), y + Math.max(0, ry-thickness) * Math.sin(a), color);
        }
    }

    public static void pill(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0) return;
        double radius = Math.min(h * .5, w * .5);
        VertexConsumer out = g.bufferSource().getBuffer(DuatUiRenderTypes.ui());
        Matrix4f m = g.pose().last().pose();
        for (int i=0; i<64; i++) {
            double a = Math.PI*2*(i+1)/64, b = Math.PI*2*i/64;
            double ax = x+w*.5 + Math.copySign(w*.5-radius, Math.cos(a)) + radius*Math.cos(a);
            double bx = x+w*.5 + Math.copySign(w*.5-radius, Math.cos(b)) + radius*Math.cos(b);
            vertex(out,m,x+w*.5,y+h*.5,color);
            vertex(out,m,ax,y+h*.5+radius*Math.sin(a),color);
            vertex(out,m,bx,y+h*.5+radius*Math.sin(b),color);
            vertex(out,m,x+w*.5,y+h*.5,color);
        }
    }

    public static void panel(GuiGraphics g, int x, int y, int w, int h, boolean violet) {
        if (w <= 2 || h <= 2) return;
        int accent = violet ? 0xFF76539E : 0xFFB8792D;
        int accentDim = violet ? 0xFFBBA7D0 : 0xFFC49A5B;
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x553A2D26);
        g.fill(x, y, x + w, y + h, 0xFFE9DDC6);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, accent);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, accentDim);
        g.fill(x + 1, y + 2, x + 2, y + h - 2, accentDim);
        g.fill(x + w - 2, y + 2, x + w - 1, y + h - 2, 0xFFD1BE9D);
        g.fill(x + 4, y + 5, x + w - 4, y + 6, 0x556E5A48);
    }

    /** Organic, asymmetric shell used by the Duat screens. */
    public static void organicPanel(GuiGraphics g, int x, int y, int w, int h, boolean violet) {
        if (w <= 4 || h <= 4) return;
        int accent = violet ? 0xFF76539E : 0xFFB8792D;
        int shadow = violet ? 0x88765C98 : 0x887E5A2B;
        // Keep the readable panel surface opaque; only the shadow and ornament layers use alpha.
        int inner = violet ? 0xFFF1EAF5 : 0xFFFFF2D7;
        drawVanillaBase(g, x, y, w, h, violet);
        blob(g, x + 3, y + 4, w, h, 0x553A2D26, 0.8);
        blob(g, x, y, w, h, violet ? 0xFFF0E8F8 : 0xFFF4E8D2, 0.0, true);
        blob(g, x + 2, y + 2, w - 4, h - 4, inner, 1.3, true);
        arc(g, x + w * .23, y + 3, w * .20, 5, 3.15, 1.45, 1.1, accent);
        arc(g, x + w * .78, y + h - 3, w * .17, 5, 0.0, 1.35, 1.0, shadow);
        arc(g, x + 5, y + h * .48, 5, h * .22, 4.1, 1.15, 1.0, shadow);
    }

    private static void drawVanillaBase(GuiGraphics g, int x, int y, int w, int h,
                                        boolean violet) {
        int fill = violet ? 0xFFF0E8F8 : 0xFFF4E8D2;
        int border = violet ? 0xFF76539E : 0xFFB8792D;
        g.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x553A2D26);
        g.fill(x, y, x + w, y + h, fill);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, border);
        g.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, border);
        g.fill(x + 1, y + 2, x + 2, y + h - 2, border);
        g.fill(x + w - 2, y + 2, x + w - 1, y + h - 2, border);
    }

    private static void blob(GuiGraphics g, double x, double y, double w, double h, int color, double phase) {
        blob(g, x, y, w, h, color, phase, false);
    }

    private static void blob(GuiGraphics g, double x, double y, double w, double h,
                             int color, double phase, boolean opaque) {
        VertexConsumer out = g.bufferSource().getBuffer(opaque
                ? DuatUiRenderTypes.uiOpaque()
                : DuatUiRenderTypes.ui());
        Matrix4f m = g.pose().last().pose();
        double cx = x + w * .5;
        double cy = y + h * .5;
        double rx = Math.max(1.0, w * .5);
        double ry = Math.max(1.0, h * .5);
        int steps = 64;
        for (int i = 0; i < steps; i++) {
            double a = Math.PI * 2.0 * i / steps;
            double b = Math.PI * 2.0 * (i + 1) / steps;
            double ra = 1.0 + .035 * Math.sin(a * 3.0 + phase) + .018 * Math.cos(a * 7.0 - phase);
            double rb = 1.0 + .035 * Math.sin(b * 3.0 + phase) + .018 * Math.cos(b * 7.0 - phase);
            vertex(out, m, cx, cy, color);
            vertex(out, m, cx + rx * ra * Math.cos(a), cy + ry * ra * Math.sin(a), color);
            vertex(out, m, cx + rx * rb * Math.cos(b), cy + ry * rb * Math.sin(b), color);
        }
    }

    private static void sheet(GuiGraphics g, int x, int y, int w, int h, int color) {
        VertexConsumer out = g.bufferSource().getBuffer(DuatUiRenderTypes.ui());
        Matrix4f m = g.pose().last().pose();
        for (int i=0; i<96; i++) {
            double a=(i+1)*Math.PI*2/96, b=i*Math.PI*2/96;
            vertex(out,m,x+w*.5,y+h*.5,color);
            vertex(out,m,x+w*.5+w*.5*curve(Math.cos(a)),y+h*.5+h*.5*curve(Math.sin(a)),color);
            vertex(out,m,x+w*.5+w*.5*curve(Math.cos(b)),y+h*.5+h*.5*curve(Math.sin(b)),color);
            vertex(out,m,x+w*.5,y+h*.5,color);
        }
    }

    private static double curve(double value) {
        return Math.copySign(Math.pow(Math.abs(value), 1.0/3.0), value);
    }

    public static void divider(GuiGraphics g,int left,int right,int y) {
        arc(g,(left+right)*.5,y-4,(right-left)*.5,6,.12,Math.PI-.24,.7,0x889F8555);
        arc(g,(left+right)*.5,y,2,2,0,Math.PI*2,1,0xFFE7CE8B);
    }

    /** Text is fitted inside an explicit rectangle, never allowed to cross another region. */
    public static void fittedText(GuiGraphics g, Component text, int x, int y, int w, int h, int color) {
        var font = Minecraft.getInstance().font;
        float scale = Math.min(1.0F, Math.min(w / (float)Math.max(1, font.width(text)), h / (float)font.lineHeight));
        g.flush();
        g.pose().pushPose();
        g.pose().translate(x + (w-font.width(text)*scale)/2, y+(h-font.lineHeight*scale)/2, 0);
        g.pose().scale(scale,scale,1);
        g.drawString(font,text,0,0,color,false);
        g.flush();
        g.pose().popPose();
    }

    /** Compact signet: circular progress is separate from the number and caption. */
    public static void status(GuiGraphics g, int x, int y, int width, float ratio,
                              Component caption, Component secondary, int color) {
        ratio=Mth.clamp(ratio,0,1);
        // A light text backing instead of a large framed plaque.
        pill(g,x+18,y+3,width-18,18,0xA8101320);
        double cx=x+12, cy=y+12;
        arc(g,cx,cy,11,11,0,Math.PI*2,11,0xE00B0E19);
        arc(g,cx,cy,11,11,0,Math.PI*2,.6,0xAA74644A);
        arc(g,cx,cy,9,9,-Math.PI/2,Math.PI*2,1.5,0xFF343140);
        arc(g,cx,cy,9,9,-Math.PI/2,Math.PI*2*ratio,1.5,color);
        // Symmetric eye-shaped jewel, confined to the medallion.
        arc(g,cx,cy-2,5,3.5,0,Math.PI,.7,color);
        arc(g,cx,cy+2,5,3.5,Math.PI,Math.PI,.7,color);
        arc(g,cx,cy,1.3,2,0,Math.PI*2,1.3,color);
        fittedText(g,caption,x+27,y+3,width-32,9,color);
        fittedText(g,secondary,x+27,y+13,width-32,8,0xFFC5C0B8);
    }

    public record HudSpace(int width, int height) {
        public int x(int desired, int itemWidth) {
            return Mth.clamp(desired,4,Math.max(4,width-itemWidth-4));
        }
        public int y(int desired) {
            return Mth.clamp(desired,4,Math.max(4,height-28));
        }
    }

    /** Use framebuffer dimensions so Minecraft GUI scale does not change HUD size. */
    public static HudSpace beginHud(GuiGraphics g) {
        var window=Minecraft.getInstance().getWindow();
        double pixelsPerUnit=Mth.clamp(Math.min(window.getWidth()/960.0,window.getHeight()/540.0),1.0,3.0);
        float transform=(float)(pixelsPerUnit/window.getGuiScale());
        g.flush();
        g.pose().pushPose();
        g.pose().scale(transform,transform,1);
        return new HudSpace((int)(window.getWidth()/pixelsPerUnit),(int)(window.getHeight()/pixelsPerUnit));
    }

    public static void endHud(GuiGraphics g) {
        g.flush();
        g.pose().popPose();
    }

    public static Button button(Component text, Button.OnPress press, int x,int y,int w,int h) {
        return new RitualButton(x,y,w,h,text,press);
    }

    private static final class RitualButton extends Button {
        RitualButton(int x,int y,int w,int h,Component text,OnPress press) {
            super(x,y,w,h,text,press,DEFAULT_NARRATION);
        }
        @Override
        protected void renderWidget(GuiGraphics g,int mx,int my,float partial) {
            boolean hover=isHoveredOrFocused();
            pill(g,getX(),getY(),width,height,active ? (hover ? 0xFF8A5FAD : 0xFFB8792D) : 0xFFA9A095);
            pill(g,getX()+1,getY()+1,width-2,height-2,hover&&active ? 0xFFE8D8F0 : 0xFFFFF0D2);
            if(hover&&active) arc(g,getX()+width*.5,getY()+height*.5,width*.48,height*.45,
                    time()*.4,Math.PI*.6,1,0xFFB8792D);
            g.flush();
            renderScrollingString(g,Minecraft.getInstance().font,4,active ? 0xFF3B2A24 : 0xFF6D655E);
        }
    }
}
