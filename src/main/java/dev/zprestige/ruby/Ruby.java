package dev.zprestige.ruby;

import dev.zprestige.ruby.eventbus.EventBus;
import dev.zprestige.ruby.events.listener.EventListener;
import dev.zprestige.ruby.manager.*;
import dev.zprestige.ruby.module.visual.Nametags;
import dev.zprestige.ruby.ui.font.RubyFont;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "ruby", name = "Ruby", version = "0.1")
public class Ruby {
    public static Minecraft mc;
    public static EventBus eventBus;
    public static ThreadManager threadManager;
    public static HoleManager holeManager;
    public static ModuleManager moduleManager;
    public static EventListener eventListener;
    public static FriendManager friendManager;
    public static EnemyManager enemyManager;
    public static TickManager tickManager;
    public static ChatManager chatManager;
    public static TotemPopManager totemPopManager;
    public static CommandManager commandManager;
    public static ConfigManager configManager;
    public static RubyFont rubyFont = new RubyFont("Font", 17.0f);

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        mc = Minecraft.getMinecraft();
        eventBus = new EventBus();
        threadManager = new ThreadManager();
        holeManager = new HoleManager();
        moduleManager = new ModuleManager();
        eventListener = new EventListener();
        friendManager = new FriendManager();
        enemyManager = new EnemyManager();
        tickManager = new TickManager();
        chatManager = new ChatManager();
        totemPopManager = new TotemPopManager();
        commandManager = new CommandManager();
        configManager = new ConfigManager().loadFromActiveConfig().readAndSetSocials();
    }
}

