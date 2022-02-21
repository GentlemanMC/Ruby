package dev.zprestige.ruby.module.visual;

import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.BooleanSetting;
import dev.zprestige.ruby.setting.impl.ColorSetting;
import dev.zprestige.ruby.setting.impl.FloatSetting;
import dev.zprestige.ruby.setting.impl.IntegerSetting;
import dev.zprestige.ruby.util.RenderUtil;
import dev.zprestige.ruby.util.Timer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.lwjgl.opengl.GL11.glEnable;

@ModuleInfo(name = "EntityTrails", category = Category.Visual, description = "crumble")
public class EntityTrails extends Module {

    public BooleanSetting self = createSetting("Self", false);
    public FloatSetting lineWidth = createSetting("LineWidth", 2.0f, 0.1f, 5.0f, (Predicate<Float>) v -> self.getValue());
    public BooleanSetting fade = createSetting("Fade", false, v -> self.getValue());
    public IntegerSetting removeDelay = createSetting("RemoveDelay", 1000, 0, 2000, (Predicate<Integer>) v -> self.getValue());
    public ColorSetting startColor = createSetting("StartColor", new Color(-1), v -> self.getValue());
    public ColorSetting endColor = createSetting("EndColor", new Color(-1), v -> self.getValue());

    public BooleanSetting entities = createSetting("Entities", false);
    public BooleanSetting pearls = createSetting("Pearls", false, v -> entities.getValue());
    public BooleanSetting exp = createSetting("Exp", false, v -> entities.getValue());
    public ColorSetting pearlColor = createSetting("EntityColor", new Color(-1), v -> entities.getValue());
    public FloatSetting pearlLineWidth = createSetting("PearlLineWidth", 3.0f, 0.0f, 10.0f, (Predicate<Float>) v -> entities.getValue());

    public HashMap<UUID, List<Vec3d>> pearlPos = new HashMap<>();
    public HashMap<UUID, Double> removeWait = new HashMap<>();
    public Map<UUID, ItemTrail> trails = new HashMap<>();

    @Override
    public void onThreadReset() {
        trails.clear();
    }

    @Override
    public void onTick() {
        if (entities.getValue()) {
            UUID pearlPos = null;
            for (UUID uuid : removeWait.keySet())
                if (removeWait.get(uuid) <= 0) {
                    this.pearlPos.remove(uuid);
                    pearlPos = uuid;
                } else
                    removeWait.replace(uuid, removeWait.get(uuid) - 0.05);
            if (pearlPos != null)
                removeWait.remove(pearlPos);
            for (Entity entity : mc.world.getLoadedEntityList()) {
                if ((entity instanceof EntityEnderPearl && pearls.getValue()) || (entity instanceof EntityExpBottle && exp.getValue())) {
                    if (!this.pearlPos.containsKey(entity.getUniqueID())) {
                        this.pearlPos.put(entity.getUniqueID(), new ArrayList<>(Collections.singletonList(entity.getPositionVector())));
                        this.removeWait.put(entity.getUniqueID(), 0.1);
                    } else {
                        this.removeWait.replace(entity.getUniqueID(), 0.1);
                        List<Vec3d> v = this.pearlPos.get(entity.getUniqueID());
                        v.add(entity.getPositionVector());
                    }
                }
            }
        }
        if (self.getValue()) {
            if (trails.containsKey(mc.player.getUniqueID())) {
                ItemTrail playerTrail = trails.get(mc.player.getUniqueID());
                playerTrail.timer.setTime(0);
                List<Position> toRemove = playerTrail.positions.stream().filter(position -> System.currentTimeMillis() - position.time > removeDelay.getValue().longValue()).collect(Collectors.toList());
                playerTrail.positions.removeAll(toRemove);
                playerTrail.positions.add(new Position(mc.player.getPositionVector()));
            } else
                trails.put(mc.player.getUniqueID(), new ItemTrail(mc.player));
        }
    }


    @Override
    public void onGlobalRenderTick() {
        if (self.getValue()) {
            trails.forEach((key, value) -> {
                if (value.entity.isDead || mc.world.getEntityByID(value.entity.getEntityId()) == null) {
                    if (value.timer.isPaused())
                        value.timer.setTime(0);

                    value.timer.setPaused(false);
                }
                if (!value.timer.isPassed())
                    drawTrail(value);

            });
        }
        if (pearlPos.isEmpty() || !entities.getValue())
            return;
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(pearlLineWidth.getValue());
        pearlPos.keySet().stream().filter(uuid -> pearlPos.get(uuid).size() > 2).forEach(uuid -> {
            GL11.glBegin(1);
            IntStream.range(1, pearlPos.get(uuid).size()).forEach(i -> {
                Color color = pearlColor.getValue();
                GL11.glColor3d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
                List<Vec3d> pos = pearlPos.get(uuid);
                GL11.glVertex3d(pos.get(i).x - mc.getRenderManager().viewerPosX, pos.get(i).y - mc.getRenderManager().viewerPosY, pos.get(i).z - mc.getRenderManager().viewerPosZ);
                GL11.glVertex3d(pos.get(i - 1).x - mc.getRenderManager().viewerPosX, pos.get(i - 1).y - mc.getRenderManager().viewerPosY, pos.get(i - 1).z - mc.getRenderManager().viewerPosZ);
            });
            GL11.glEnd();
        });
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }

    void drawTrail(ItemTrail trail) {
        Color fadeColor = endColor.getValue();
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GL11.glDisable(3553);
        glEnable(2848);
        GL11.glBlendFunc(770, 771);
        GL11.glLineWidth(lineWidth.getValue());
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        (RenderUtil.builder = RenderUtil.tessellator.getBuffer()).begin(3, DefaultVertexFormats.POSITION_COLOR);
        buildBuffer(RenderUtil.builder, trail, startColor.getValue(), fade.getValue() ? fadeColor : startColor.getValue());
        RenderUtil.tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.popMatrix();
        glEnable(3553);
        GL11.glPolygonMode(1032, 6914);
    }

    void buildBuffer(BufferBuilder builder, ItemTrail trail, Color start, Color end) {
        for (Position p : trail.positions) {
            Vec3d pos = RenderUtil.updateToCamera(p.pos);
            double value = normalize(trail.positions.indexOf(p), trail.positions.size());
            RenderUtil.addBuilderVertex(builder, pos.x, pos.y, pos.z, RenderUtil.interpolateColor((float) value, start, end));
        }
    }

    double normalize(double value, double max) {
        return (value - 0.0) / (max - 0.0);
    }

    static class ItemTrail {
        public Entity entity;
        public List<Position> positions;
        public Timer timer;

        ItemTrail(Entity entity) {
            this.entity = entity;
            positions = new ArrayList<>();
            (timer = new dev.zprestige.ruby.util.Timer()).setDelay(1000);
            timer.setPaused(true);
        }
    }

    static class Position {
        public Vec3d pos;
        public long time;

        public Position(Vec3d pos) {
            this.pos = pos;
            time = System.currentTimeMillis();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Position position = (Position) o;
            return time == position.time && Objects.equals(pos, position.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, time);
        }
    }
}