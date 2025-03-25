package com.EMagic;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import com.EMagic.commands.MagicCommand;
import com.EMagic.elements.ElementManager;
import com.EMagic.listeners.FallDamageListener;
import com.EMagic.player.MagicPlayer;
import com.EMagic.player.MagicPlayerManager;
import com.EMagic.events.MagicEventListener;
import com.EMagic.spells.SpellManager;
import com.EMagic.forms.MagicForms;
import com.EMagic.forms.FormListener;

import java.util.HashMap;
import java.io.File;

public class ElementalMagicSystem extends PluginBase {
    
    private static ElementalMagicSystem instance;
    private ElementManager elementManager;
    private MagicPlayerManager playerManager;
    private SpellManager spellManager;
    private Config spellConfig;
    private Config playerDataConfig;
    private MagicCommand magicCommand;
    private MagicForms formManager;
    
    @Override
    public void onLoad() {
        instance = this;
        this.getLogger().info(TextFormat.WHITE + "ElementalMagic System loading...");
    }
    
    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File spellFile = new File(getDataFolder(), "spells.yml");
        if (!spellFile.exists()) {
            saveResource("spells.yml", false);
        }
        spellConfig = new Config(spellFile, Config.YAML);
        File playerDataFile = new File(getDataFolder(), "playerdata.yml");
        playerDataConfig = new Config(playerDataFile, Config.YAML);
        elementManager = new ElementManager(this);
        playerManager = new MagicPlayerManager(this);
        spellManager = new SpellManager(this);
        formManager = new MagicForms(this);
        
        getServer().getPluginManager().registerEvents(new MagicEventListener(this), this);
        getServer().getPluginManager().registerEvents(new com.EMagic.events.DamageListener(this), this);
        getServer().getPluginManager().registerEvents(new FallDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new FormListener(this), this);
        
        magicCommand = new MagicCommand(this);
        
        this.getLogger().info(TextFormat.GREEN + "ElementalMagic System enabled!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("magic")) {
            return magicCommand.execute(sender, label, args);
        }
        return false;
    }
    
    @Override
    public void onDisable() {
        playerManager.saveAllPlayerData();
        playerDataConfig.save();
        this.getLogger().info(TextFormat.RED + "ElementalMagic System disabled!");
    }
    
    public static ElementalMagicSystem getInstance() {
        return instance;
    }
    
    public ElementManager getElementManager() {
        return elementManager;
    }
    
    public MagicPlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public SpellManager getSpellManager() {
        return spellManager;
    }
    
    public Config getSpellConfig() {
        return spellConfig;
    }
    
    public Config getPlayerDataConfig() {
        return playerDataConfig;
    }
    
    public MagicForms getFormManager() {
        return formManager;
    }
} 