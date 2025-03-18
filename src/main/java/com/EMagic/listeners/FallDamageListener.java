package com.EMagic.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.player.MagicPlayer;

public class FallDamageListener implements Listener {

    private ElementalMagicSystem plugin;
    
    public FallDamageListener(ElementalMagicSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        // Check if it's a fall damage event
        if (event.getCause() != DamageCause.FALL) {
            return;
        }
        
        // Check if the entity is a player
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        // Check if player has the Feather Fall effect
        if (magicPlayer.hasActiveEffect("feather_fall")) {
            // Get the damage reduction amount
            Object reductionObj = magicPlayer.getEffectData("feather_fall.reduction");
            if (reductionObj != null && reductionObj instanceof Float) {
                float reduction = (Float) reductionObj;
                
                // Reduce the damage
                float originalDamage = event.getDamage();
                float newDamage = originalDamage * (1 - reduction);
                
                // Set the new damage
                event.setDamage(newDamage);
                
                // Create visual effect on landing
                createLandingEffect(player);
            }
        }
    }
    
    private void createLandingEffect(Player player) {
        Vector3 position = player.add(0, 0.1, 0);
        
        for (int i = 0; i < 20; i++) {
            double angle = i * (Math.PI / 10);
            double radius = 1.2;
            
            double x = position.x + Math.cos(angle) * radius;
            double y = position.y;
            double z = position.z + Math.sin(angle) * radius;
            
            Vector3 particlePos = new Vector3(x, y, z);
            player.getLevel().addParticle(new GenericParticle(
                particlePos,
                Particle.TYPE_DUST
            ));
        }
    }
} 