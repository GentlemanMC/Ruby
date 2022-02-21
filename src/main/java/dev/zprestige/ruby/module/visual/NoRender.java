package dev.zprestige.ruby.module.visual;

import dev.zprestige.ruby.events.PacketEvent;
import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.BooleanSetting;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@ModuleInfo(name = "NoRender" , category = Category.Visual, description = "doesnt rener selected stuff")
public class NoRender extends Module {
    public static NoRender Instance;
    public BooleanSetting hurtCam = createSetting("Hurt Cam", false);
    public BooleanSetting fire = createSetting("Fire", false);
    public BooleanSetting explosions = createSetting("Explosions", false);
    public BooleanSetting insideBlocks = createSetting("Inside Blocks", false);
    public BooleanSetting armor = createSetting("Armor", false);

    public NoRender() {
        Instance = this;
    }

    @SubscribeEvent
    public void onRenderBlockOverlay(RenderBlockOverlayEvent event) {
        if (nullCheck() || !isEnabled())
            return;

        if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.FIRE))
            event.setCanceled(fire.getValue());

        if (event.getOverlayType().equals(RenderBlockOverlayEvent.OverlayType.BLOCK))
            event.setCanceled(insideBlocks.getValue());
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (nullCheck() || !isEnabled())
            return;

        if (event.getPacket() instanceof SPacketExplosion)
            event.setCanceled(explosions.getValue());
    }

}