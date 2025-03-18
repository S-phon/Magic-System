package com.EMagic.player;

import cn.nukkit.Player;

import com.EMagic.ElementalMagicSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MagicPlayerManager {
    
    private ElementalMagicSystem plugin;
    private Map<UUID, MagicPlayer> magicPlayers;
    
    public MagicPlayerManager(ElementalMagicSystem plugin) {
        this.plugin = plugin;
        this.magicPlayers = new HashMap<>();
    }
    
    public MagicPlayer getMagicPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!magicPlayers.containsKey(uuid)) {
            MagicPlayer magicPlayer = new MagicPlayer(plugin, player);
            magicPlayer.loadData();
            magicPlayers.put(uuid, magicPlayer);
        }
        
        return magicPlayers.get(uuid);
    }
    
    public MagicPlayer getMagicPlayer(UUID uuid) {
        return magicPlayers.get(uuid);
    }
    
    public boolean hasMagicPlayer(UUID uuid) {
        return magicPlayers.containsKey(uuid);
    }
    
    public void addMagicPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!magicPlayers.containsKey(uuid)) {
            MagicPlayer magicPlayer = new MagicPlayer(plugin, player);
            magicPlayer.loadData();
            magicPlayers.put(uuid, magicPlayer);
        }
    }
    
    public void removeMagicPlayer(UUID uuid) {
        if (magicPlayers.containsKey(uuid)) {
            MagicPlayer magicPlayer = magicPlayers.get(uuid);
            magicPlayer.saveData();
            magicPlayers.remove(uuid);
        }
    }
    
    public void savePlayerData(UUID uuid) {
        if (magicPlayers.containsKey(uuid)) {
            magicPlayers.get(uuid).saveData();
        }
    }
    
    public void saveAllPlayerData() {
        for (MagicPlayer magicPlayer : magicPlayers.values()) {
            magicPlayer.saveData();
        }
    }
} 