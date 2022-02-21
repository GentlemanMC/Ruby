package dev.zprestige.ruby.module.visual;

import dev.zprestige.ruby.Ruby;
import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.ColorSetting;
import dev.zprestige.ruby.setting.impl.FloatSetting;
import dev.zprestige.ruby.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.awt.*;

@ModuleInfo(name = "Interactions", category = Category.Visual, description = "Shows when stuff gets interactificationizationified")
public class Interactions extends Module {
    public FloatSetting range = createSetting("Range", 100.0f, 0.1f, 300.0f);
    public ColorSetting color = createSetting("Color", new Color(-1));
    public ColorSetting outlineColor = createSetting("Outline Color", new Color(-1));

    @Override
    public void onGlobalRenderTick() {
        mc.renderGlobal.damagedBlocks.forEach(((integer, destroyBlockProgress) -> renderDestroyProgress(destroyBlockProgress)));
    }

    private void renderDestroyProgress(DestroyBlockProgress destroyBlockProgress) {
        if (destroyBlockProgress != null) {
            BlockPos pos = destroyBlockProgress.getPosition();
            if ((mc.playerController.getIsHittingBlock() && mc.playerController.currentBlock.equals(pos)) || (mc.player.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > range.getValue()))
                return;
            float progress = Math.min(1F, (float) destroyBlockProgress.getPartialBlockDamage() / 8F);
            renderProgress(pos, progress);
        }
    }

    private void renderProgress(BlockPos pos, float progress) {
        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, -1500000.0f);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        float scale = 0.016666668f * 0.95f;
        GlStateManager.translate(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ);
        GlStateManager.glNormal3f(0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(-mc.player.rotationYaw, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.player.rotationPitch, mc.gameSettings.thirdPersonView == 2 ? -1.0f : 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-scale, -scale, scale);
        int distance = (int) mc.player.getDistance(x, y, z);
        scale = (distance / 2.0f) / (2.0f + (2.0f - 1));
        if (scale < 1.0f)
            scale = 1;
        GlStateManager.scale(scale, scale, scale);
        String string = progress * 100 + "%";
        GlStateManager.translate(-(Ruby.rubyFont.getStringWidth(string) / 2.0), 0, 0);
        RenderUtil.drawUnfilledCircle(Ruby.rubyFont.getStringWidth(string) / 2.0f, 0, 23.0f, new Color(outlineColor.getValue().getRed() / 255.0f, outlineColor.getValue().getGreen() / 255.0f, outlineColor.getValue().getBlue() / 255.0f, 1.0f).getRGB(), 5.0f, progress * 360);
        RenderUtil.drawCircle(Ruby.rubyFont.getStringWidth(string) / 2.0f, 0, 22.0f, new Color(color.getValue().getRed() / 255.0f, color.getValue().getGreen() / 255.0f, color.getValue().getBlue() / 255.0f, 1.0f).getRGB());
        Ruby.rubyFont.drawString(string, 0, 6.0f, new Color(255, 255, 255).getRGB());
        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/icons/pickaxe.png"));
        Gui.drawScaledCustomSizeModalRect((int) (Ruby.rubyFont.getStringWidth(string) / 2.0f) - 10, -17, 0, 0, 12, 12, 22, 22, 12, 12);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1.0f, 1500000.0f);
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
