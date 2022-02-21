package dev.zprestige.ruby.module;

import dev.zprestige.ruby.Ruby;
import dev.zprestige.ruby.events.ModuleToggleEvent;
import dev.zprestige.ruby.events.Render3DEvent;
import dev.zprestige.ruby.setting.Setting;
import dev.zprestige.ruby.setting.impl.*;
import dev.zprestige.ruby.util.MessageUtil;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Module {
    public boolean open, drawn = true;
    String name = getModuleInfo().name();
    String hudString = "";
    Color hudStringColor = new Color(0x939393);
    Category category = getModuleInfo().category();
    String description = getModuleInfo().description();
    List<Setting<?>> settingList = new ArrayList<>();
    KeySetting keybind = createSetting("Keybind", Keyboard.KEY_NONE);
    BooleanSetting enabled = createSetting("Enabled", false);
    public Minecraft mc = Minecraft.getMinecraft();
    public int scrollY;

    public ModuleInfo getModuleInfo() {
        return getClass().getAnnotation(ModuleInfo.class);
    }

    public void onThreadReset() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onOverlayTick() {
    }


    public void onGlobalRenderTick() {
    }

    public void onGlobalRenderTick(Render3DEvent event) {
    }

    public void setHudString(String hudString) {
        this.hudString = hudString;
    }

    public void enableModule() {
        setEnabled(true);
        onEnable();
        Ruby.RubyEventBus.post(new ModuleToggleEvent.Enable(this));
        Ruby.RubyEventBus.register(this);
    }

    public void disableModule() {
        setEnabled(false);
        onDisable();
        Ruby.RubyEventBus.post(new ModuleToggleEvent.Disable(this));
        Ruby.RubyEventBus.unregister(this);
    }

    public void disableModule(String message) {
        setEnabled(false);
        onDisable();
        Ruby.RubyEventBus.post(new ModuleToggleEvent.Disable(this));
        Ruby.RubyEventBus.unregister(this);
        MessageUtil.sendMessage(message);
    }


    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isEnabled() {
        return enabled.getValue();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setValue(enabled);
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Integer getKeybind() {
        return keybind.getKey();
    }

    public void setKeybind(Integer keybind) {
        this.keybind.setValue(keybind);
    }

    public boolean nullCheck() {
        return mc.world == null || mc.player == null;
    }

    public String getHudString() {
        return hudString;
    }

    public void setHudStringColor(Color hudStringColor) {
        this.hudStringColor = hudStringColor;
    }

    public Color getHudStringColor() {
        return hudStringColor;
    }

    public Float getModuleNameWidth() {
        return Ruby.rubyFont.getStringWidth("[" + name + hudString + "]");
    }

    public BooleanSetting createSetting(String name, boolean value) {
        BooleanSetting setting = new BooleanSetting(name, value);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public BooleanSetting createSetting(String name, boolean value, Predicate<Boolean> shown) {
        BooleanSetting setting = new BooleanSetting(name, value, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ColorSetting createSetting(String name, Color value) {
        ColorSetting setting = new ColorSetting(name, value);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ColorSetting createSetting(String name, Color value, Predicate<Color> shown) {
        ColorSetting setting = new ColorSetting(name, value, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public DoubleSetting createSetting(String name, double value, double minimum, double maximum) {
        DoubleSetting setting = new DoubleSetting(name, value, minimum, maximum);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public DoubleSetting createSetting(String name, double value, double minimum, double maximum, Predicate<Double> shown) {
        DoubleSetting setting = new DoubleSetting(name, value, minimum, maximum, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ModeSetting createSetting(String name, String value, java.util.List<String> modeList) {
        ModeSetting setting = new ModeSetting(name, value, modeList);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ModeSetting createSetting(String name, String value, List<String> modeList, Predicate<String> shown) {
        ModeSetting setting = new ModeSetting(name, value, modeList, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public FloatSetting createSetting(String name, float value, float minimum, float maximum) {
        FloatSetting setting = new FloatSetting(name, value, minimum, maximum);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public FloatSetting createSetting(String name, float value, float minimum, float maximum, Predicate<Float> shown) {
        FloatSetting setting = new FloatSetting(name, value, minimum, maximum, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public IntegerSetting createSetting(String name, int value, int minimum, int maximum) {
        IntegerSetting setting = new IntegerSetting(name, value, minimum, maximum);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public IntegerSetting createSetting(String name, int value, int minimum, int maximum, Predicate<Integer> shown) {
        IntegerSetting setting = new IntegerSetting(name, value, minimum, maximum, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public KeySetting createSetting(String name, int value) {
        KeySetting setting = new KeySetting(name, value);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public KeySetting createSetting(String name, int value, Predicate<Integer> shown) {
        KeySetting setting = new KeySetting(name, value, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public StringSetting createSetting(String name, String value) {
        StringSetting setting = new StringSetting(name, value);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public StringSetting createSetting(String name, String value, Predicate<String> shown) {
        StringSetting setting = new StringSetting(name, value, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ParentSetting createSetting(String name) {
        ParentSetting setting = new ParentSetting(name);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public ParentSetting createSetting(String name, Predicate<Boolean> shown) {
        ParentSetting setting = new ParentSetting(name, shown);
        setting.setModule(this);
        settingList.add(setting);
        return setting;
    }

    public List<Setting<?>> getSettingList() {
        return settingList;
    }
}