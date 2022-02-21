package dev.zprestige.ruby.module.movement;

import dev.zprestige.ruby.events.BlockPushEvent;
import dev.zprestige.ruby.events.EntityPushEvent;
import dev.zprestige.ruby.events.MoveEvent;
import dev.zprestige.ruby.events.PacketEvent;
import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.BooleanSetting;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Velocity" , category = Category.Movement, description = "cancels kabooms")
public class Velocity extends Module {
    public BooleanSetting explosions = createSetting("Explosions", false);
    public BooleanSetting push = createSetting("Push", false);
    public BooleanSetting blocks = createSetting("Blocks", false);
    public BooleanSetting pistons = createSetting("Pistons", false);

    @SubscribeEvent
    public void onPacketReceived(PacketEvent.PacketReceiveEvent event) {
        if (nullCheck() || !isEnabled() || !explosions.getValue())
            return;
        event.setCanceled(event.getPacket() instanceof SPacketEntityVelocity ? ((SPacketEntityVelocity) event.getPacket()).getEntityID() == mc.player.entityId : event.getPacket() instanceof SPacketExplosion);
    }

    @SubscribeEvent
    public void onBlockPush(BlockPushEvent event) {
        if (nullCheck() || !isEnabled() || !blocks.getValue())
            return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onEntityCollision(EntityPushEvent event) {
        if (nullCheck() || !isEnabled() || !push.getValue())
            return;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event){
        if (nullCheck() || !isEnabled() || !pistons.getValue() || !(event.getType().equals(MoverType.PISTON) || event.getType().equals(MoverType.SHULKER_BOX))) {
            return;
        }
        event.setCanceled(true);
    }
}
