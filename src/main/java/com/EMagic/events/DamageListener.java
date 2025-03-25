package com.EMagic.events;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.player.MagicPlayer;

public class DamageListener implements Listener {
    
    private ElementalMagicSystem plugin;
    
    public DamageListener(ElementalMagicSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled()) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        // Check for active Stone Shield
        if (magicPlayer.hasActiveEffect("stone_shield")) {
            Object reductionObj = magicPlayer.getEffectData("stone_shield_reduction");
            if (reductionObj instanceof Double) {
                double reduction = (Double) reductionObj;
                double originalDamage = event.getDamage();
                double reducedDamage = originalDamage * (1 - reduction);
                
                // Apply damage reduction
                event.setDamage((float)reducedDamage);
                
                // Visual feedback only on significant damage
                if (originalDamage >= 1.0) {
                    // Calculate damage prevented
                    double prevented = originalDamage - reducedDamage;
                    int preventedDisplay = (int) Math.ceil(prevented);
                    
                    // Show stone particles on hit to indicate shield activation
                    for (int i = 0; i < 10; i++) {
                        double offsetX = Math.random() * 1 - 0.5;
                        double offsetY = Math.random() * 1;
                        double offsetZ = Math.random() * 1 - 0.5;
                        player.getLevel().addParticle(new cn.nukkit.level.particle.DustParticle(
                            player.add(offsetX, 1 + offsetY, offsetZ), 139, 69, 19));
                    }
                    
                    // Notify player
                    if (prevented >= 1.0) {
                        player.sendTip(TextFormat.GREEN + "Stone Shield absorbed " + 
                                      TextFormat.GOLD + preventedDisplay + 
                                      TextFormat.GREEN + " damage!");
                    }
                }
            }
        }
    }
} 