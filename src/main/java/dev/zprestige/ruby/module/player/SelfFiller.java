package dev.zprestige.ruby.module.player;

import dev.zprestige.ruby.module.Category;
import dev.zprestige.ruby.module.Module;
import dev.zprestige.ruby.module.ModuleInfo;
import dev.zprestige.ruby.setting.impl.BooleanSetting;
import dev.zprestige.ruby.setting.impl.DoubleSetting;
import dev.zprestige.ruby.setting.impl.ModeSetting;
import dev.zprestige.ruby.util.BlockUtil;
import dev.zprestige.ruby.util.InventoryUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

@ModuleInfo(name = "SelfFiller", category = Category.Player, description = "cringe as said in skitttyyhack vidoe")
public class SelfFiller extends Module {
    public DoubleSetting force = createSetting("Force", 1.5, -5.0, 10.0);
    public BooleanSetting rotate = createSetting("Rotate", false);
    public ModeSetting block = createSetting("Block", "Obsidian", Arrays.asList("Obsidian", "EnderChests", "Fallback", "WitherSkulls", "Anvil"));
    public int slot = -1;
    public BlockPos startPos;

    @Override
    public void onEnable() {
        mc.player.motionX = 0;
        mc.player.motionZ = 0;
        startPos = BlockUtil.getPlayerPos();
    }

    @Override
    public void onTick() {
        switch (block.getValue()) {
            case "Obsidian":
                slot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
                break;
            case "EnderChests":
                slot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
                break;
            case "Fallback":
                int slot2 = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN));
                if (slot2 != -1)
                    slot = slot2;
                else slot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST));
                break;
            case "WitherSkulls":
                slot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.SKULL));
                break;
            case "Anvil":
                slot = InventoryUtil.getItemFromHotbar(Item.getItemFromBlock(Blocks.ANVIL));
                break;
        }
        if (slot == -1) {
            disableModule("No blocks found, disabling SelfFiller.");
            return;
        }
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, true));
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, true));
        BlockUtil.placeBlockWithSwitch(startPos, EnumHand.MAIN_HAND, rotate.getValue(), true, slot);
        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + force.getValue(), mc.player.posZ, false));
        disableModule();
    }
}