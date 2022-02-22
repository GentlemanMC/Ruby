package dev.zprestige.ruby.module.visual;

import dev.zprestige.ruby.eventbus.annotation.RegisterListener;
import dev.zprestige.ruby.events.Render3DEvent;
import dev.zprestige.ruby.events.RenderItemInFirstPersonEvent;
import dev.zprestige.ruby.mixins.render.IEntityRenderer;
import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.BooleanSetting;
import dev.zprestige.ruby.setting.impl.ColorSetting;
import dev.zprestige.ruby.setting.impl.FloatSetting;
import dev.zprestige.ruby.setting.impl.ParentSetting;
import dev.zprestige.ruby.util.shader.ItemShader;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.awt.*;

@ModuleInfo(name = "Shaders", category = Category.Visual, description = "Chams the items in the items chams")
public class Shaders extends Module {
    public ParentSetting targets = createSetting("Targets");
    public BooleanSetting players = createSetting("Players", false).setParent(targets);
    public BooleanSetting crystals = createSetting("Crystals", false).setParent(targets);
    public BooleanSetting experienceBottles = createSetting("Experience Bottles", false).setParent(targets);
    public BooleanSetting items = createSetting("Items", false).setParent(targets);

    public ParentSetting shader = createSetting("Shader");
    public ColorSetting color = createSetting("Color", new Color(-1)).setParent(shader);
    public FloatSetting radius = createSetting("Radius", 2.0f, 0.1f, 10.0f).setParent(shader);
    public FloatSetting opacity = createSetting("Opacity", 255.0f, 0.0f, 255.0f).setParent(shader);
    public boolean forceRender = false;

    @RegisterListener
    public void renderItemInFirstPerson(RenderItemInFirstPersonEvent event) {
        if (nullCheck() || !isEnabled() || !event.isPre || forceRender || !items.getValue())
            return;
        event.setCancelled(true);
    }

    @Override
    public void onGlobalRenderTick(Render3DEvent event) {
        if (nullCheck())
            return;
        if (((Display.isActive() || Display.isVisible()) && mc.gameSettings.thirdPersonView == 0) && !(mc.currentScreen instanceof GuiDownloadTerrain)) {
            GlStateManager.pushMatrix();
            GlStateManager.pushAttrib();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableAlpha();
            ItemShader shader = ItemShader.Instance;
            shader.mix = opacity.getValue() / 255.0f;
            shader.alpha = color.getValue().getAlpha() / 255.0f;
            shader.startDraw(mc.getRenderPartialTicks());
            forceRender = true;
            mc.world.loadedEntityList.stream().filter(entity -> entity != null && ((entity != mc.player || entity != mc.getRenderViewEntity()) && mc.getRenderManager().getEntityRenderObject(entity) != null) && (entity instanceof EntityPlayer && players.getValue() && !((EntityPlayer) entity).isSpectator() || entity instanceof EntityEnderCrystal && crystals.getValue() || entity instanceof EntityExpBottle && experienceBottles.getValue())).forEach(entity -> {
                Vec3d vector = getInterpolatedRenderPos(entity, event.partialTicks);
                if (entity instanceof EntityPlayer)
                    ((EntityPlayer) entity).hurtTime = 0;
                Render<Entity> render = mc.getRenderManager().getEntityRenderObject(entity);
                if (render != null) {
                    try {
                        render.doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, event.partialTicks);
                    } catch (Exception ignored){
                    }
                }
            });
            if (items.getValue())
                ((IEntityRenderer) mc.entityRenderer).invokeRenderHand(mc.getRenderPartialTicks(), 2);
            forceRender = false;
            shader.stopDraw(color.getValue(), radius.getValue(), 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.disableDepth();
            GlStateManager.popAttrib();
            GlStateManager.popMatrix();
        }
    }

    public Vec3d getInterpolatedRenderPos(Entity entity, float ticks) {
        return interpolateEntity(entity, ticks).subtract(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY, mc.getRenderManager().renderPosZ);
    }

    public Vec3d interpolateEntity(Entity entity, float time) {
        return new Vec3d(entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * time, entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * time, entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * time);
    }
}
