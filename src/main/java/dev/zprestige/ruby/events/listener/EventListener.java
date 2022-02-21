package dev.zprestige.ruby.events.listener;

import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import dev.zprestige.ruby.Ruby;
import dev.zprestige.ruby.events.*;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.visual.Nametags;
import dev.zprestige.ruby.module.visual.Waypoints;
import dev.zprestige.ruby.util.MessageUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;

public class EventListener {
    public EventListener() {
        Ruby.RubyEventBus.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event) {
        if (Ruby.mc.player != null && Ruby.mc.world != null && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(Ruby.mc.player)) {
            Ruby.moduleManager.getOrderedModuleList().stream().filter(Module::isEnabled).forEach(Module::onTick);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlayTextEvent(RenderGameOverlayEvent.Text event) {
        if (Ruby.mc.player != null && Ruby.mc.world != null)
            Ruby.moduleManager.getOrderedModuleList().stream().filter(Module::isEnabled).forEach(Module::onOverlayTick);
    }

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity().equals(Ruby.mc.player)) {
            Ruby.moduleManager.getOrderedModuleList().stream().filter(Module::isEnabled).forEach(Module::onThreadReset);
            Ruby.holeManager.onThreadReset();
        }
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Ruby.moduleManager.getOrderedModuleList().stream().filter(Module::isEnabled).forEach(Module::onThreadReset);
        Ruby.holeManager.onThreadReset();
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Ruby.RubyEventBus.post(new SelfLogoutEvent());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderWorldLastEvent(RenderWorldLastEvent event) {
        Ruby.mc.profiler.startSection("ruby");
        if (Ruby.mc.player != null && Ruby.mc.world != null) {
            Render3DEvent render3DEvent = new Render3DEvent(event.getPartialTicks());
            Ruby.holeManager.onRenderWorldLastEvent();
            if (Nametags.Instance.isEnabled())
                Nametags.Instance.onGlobalRenderTick(render3DEvent);
            Ruby.moduleManager.getOrderedModuleList().stream().filter(Module::isEnabled).forEach(Module::onGlobalRenderTick);
            Ruby.moduleManager.getOrderedModuleList().stream().filter(module -> module.isEnabled() && !(module instanceof Nametags)).forEach(module -> module.onGlobalRenderTick(render3DEvent));
        }
        Ruby.mc.profiler.endSection();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Ruby.mc.player != null && Ruby.mc.world != null && Keyboard.getEventKey() != 226) {
            Ruby.moduleManager.getOrderedModuleList().stream().filter(module -> Keyboard.getEventKeyState() && module.getKeybind().equals(Keyboard.getEventKey())).forEach(module -> {
                if (module.isEnabled())
                    module.disableModule();
                else
                    module.enableModule();
            });
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.PacketReceiveEvent event) {
        if (Ruby.mc.world == null || Ruby.mc.player == null)
            return;
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT)
            Ruby.RubyEventBus.post(new ChorusEvent(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ()));
        if (event.getPacket() instanceof SPacketPlayerListItem) {
            for (SPacketPlayerListItem.AddPlayerData data : ((SPacketPlayerListItem) event.getPacket()).getEntries()) {
                if (data != null && (!Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null)) {
                    EntityPlayer entity = Ruby.mc.world.getPlayerEntityByUUID(data.getProfile().getId());
                    if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.ADD_PLAYER)) {
                        Ruby.RubyEventBus.post(new LogoutEvent.LoginEvent(entity));
                    } else if (((SPacketPlayerListItem) event.getPacket()).getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER)) {

                        if (entity != null)
                            Ruby.RubyEventBus.post(new LogoutEvent(entity, entity.getPosition(), System.currentTimeMillis(), entity.getEntityId()));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.PacketSendEvent event) {
        if (Ruby.mc.world == null || Ruby.mc.player == null || !(event.getPacket() instanceof CPacketChatMessage))
            return;
        CPacketChatMessage packet = (CPacketChatMessage) event.getPacket();
        if (packet.getMessage().startsWith("@"))
            event.setCanceled(true);
        else
            return;
        if (packet.getMessage().toLowerCase().contains("ruby load")) {
            String folder = packet.getMessage().split(" ")[2];
            try {
                Ruby.configInitializer.load(folder);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully loaded " + ChatFormatting.GRAY + folder + ChatFormatting.WHITE + ".");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby save")) {
            String folder = packet.getMessage().split(" ")[2];
            try {
                Ruby.configInitializer.save(folder);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully saved " + ChatFormatting.GRAY + folder + ChatFormatting.WHITE + ".");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby friend add")) {
            String name = packet.getMessage().split(" ")[3];
            try {
                Ruby.friendInitializer.addFriend(name);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully added " + ChatFormatting.AQUA + name + ChatFormatting.WHITE + " as a friend.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby friend del")) {
            String name = packet.getMessage().split(" ")[3];
            try {
                Ruby.friendInitializer.removeFriend(name);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully removed " + ChatFormatting.AQUA + name + ChatFormatting.WHITE + " as a friend.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby enemy add")) {
            String name = packet.getMessage().split(" ")[3];
            try {
                Ruby.enemyInitializer.addEnemy(name);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully added " + ChatFormatting.RED + name + ChatFormatting.WHITE + " as an enemy.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby enemy del")) {
            String name = packet.getMessage().split(" ")[3];
            try {
                Ruby.enemyInitializer.removeEnemy(name);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully removed " + ChatFormatting.RED + name + ChatFormatting.WHITE + " as an enemy.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }

        if (packet.getMessage().toLowerCase().contains("ruby waypoint add")) {
            String message = packet.getMessage();
            String name = message.split(" ")[3];
            String x = message.split(" ")[4];
            String y = message.split(" ")[5];
            String z = message.split(" ")[6];
            try {
                Waypoints.Instance.waypointHashMap.put(name, new Waypoints.Waypoint(name, new BlockPos(Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z))));
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully added " + ChatFormatting.RED + name + ChatFormatting.WHITE + " to waypoints.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby waypoint del")) {
            String message = packet.getMessage();
            String name = message.split(" ")[3];
            try {
                Waypoints.Instance.waypointHashMap.remove(name);
                MessageUtil.sendMessage(ChatFormatting.WHITE + "Successfully added " + ChatFormatting.RED + name + ChatFormatting.WHITE + " to waypoints.");
            } catch (Exception ignored) {
                MessageUtil.sendMessage("Input not found or something went wrong, command failed.");
            }
            return;
        }
        if (packet.getMessage().toLowerCase().contains("ruby help")) {
            MessageUtil.sendMessage(ChatFormatting.WHITE + "Ruby Help:");
            MessageUtil.sendMessage(ChatFormatting.RED + "====================");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Prefix: " + ChatFormatting.GRAY + " @");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Load Config: " + ChatFormatting.GRAY + " @Ruby Load" + ChatFormatting.RED + " <Folder Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Save Config: " + ChatFormatting.GRAY + "@Ruby Save" + ChatFormatting.RED + " <Folder Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Add Friend: " + ChatFormatting.GRAY + "@Ruby Friend Add" + ChatFormatting.RED + " <Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Delete Friend: " + ChatFormatting.GRAY + "@Ruby Friend Del" + ChatFormatting.RED + " <Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Add Enemy: " + ChatFormatting.GRAY + "@Ruby Enemy Add" + ChatFormatting.RED + " <Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Delete Enemy: " + ChatFormatting.GRAY + "@Ruby Enemy Del" + ChatFormatting.RED + " <Name>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Add Waypoint: " + ChatFormatting.GRAY + "@Ruby Waypoint Add" + ChatFormatting.RED + " <Name> <x> <y> <z>");
            MessageUtil.sendMessage("\u2022 " + ChatFormatting.WHITE + "Delete Waypoint: " + ChatFormatting.GRAY + "@Ruby Waypoint Del" + ChatFormatting.RED + " <Name>");
            MessageUtil.sendMessage(ChatFormatting.RED + "====================");
            return;
        }
        MessageUtil.sendMessage("Command not found, use '@Ruby Help' for all commands.");
    }
}