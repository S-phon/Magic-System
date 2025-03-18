package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

public class StoneShieldSpell extends BasicSpell {
    
    private static final int DURATION = 60; // 1 minute in seconds
    private static final double DAMAGE_REDUCTION = 0.5; // 50% damage reduction
    
    public StoneShieldSpell() {
        super(
            "stone_shield",
            "Creates a shield of stone around you",
            Element.EARTH,
            30, // Mana cost
            1,  // Required mastery level
            120000, // Cooldown time (30 seconds)
            Sound.DIG_STONE // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.EARTH);
        int duration = DURATION;
        double damageReduction = DAMAGE_REDUCTION;
        
        // Scale duration and damage reduction based on mastery level
        if (masteryLevel >= 75) {
            duration *= 2; // 10 minutes at high mastery
            damageReduction = 0.75; // 75% damage reduction
            player.sendMessage(TextFormat.DARK_GREEN + "Your mastery of earth creates an impenetrable shield!");
        } else if (masteryLevel >= 50) {
            duration *= 1.5; // 7.5 minutes at medium mastery
            damageReduction = 0.6; // 60% damage reduction
            player.sendMessage(TextFormat.GREEN + "Your growing mastery strengthens your stone shield!");
        }
        
        // Store shield effect values in player data
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + (duration * 1000);
        magicPlayer.setEffectEndTime("stone_shield", endTime);
        magicPlayer.setEffectData("stone_shield_reduction", damageReduction);
        
        // Create stone shield particles around the player
        createShieldParticles(player);
        
        // Schedule periodic shield particles
        int taskId = magicPlayer.getPlugin().getServer().getScheduler().scheduleRepeatingTask(() -> {
            if (magicPlayer.hasActiveEffect("stone_shield")) {
                createShieldParticles(player);
            } else {
                // Cancel task when effect ends
                Integer storedTaskId = (Integer) magicPlayer.getEffectData("stone_shield_task");
                if (storedTaskId != null) {
                    magicPlayer.getPlugin().getServer().getScheduler().cancelTask(storedTaskId);
                    magicPlayer.removeEffectData("stone_shield_task");
                }
            }
        }, 100).getTaskId(); // Every 5 seconds (100 ticks)
        
        // Store task ID for cancellation when effect ends
        magicPlayer.setEffectData("stone_shield_task", taskId);
        
        // Success message
        int minutes = duration / 60;
        int seconds = duration % 60;
        int reductionPercent = (int) (damageReduction * 100);
        
        player.sendMessage(TextFormat.DARK_GREEN + "You cast " + TextFormat.GREEN + "Stone Shield" + 
                          TextFormat.DARK_GREEN + "! You are protected for " + 
                          minutes + " minutes and " + seconds + " seconds with " +
                          reductionPercent + "% damage reduction.");
        
        return SpellCastResult.SUCCESS;
    }
    
    /**
     * Creates stone shield particles around the player
     */
    private void createShieldParticles(Player player) {
        // Create a sphere of particles around the player
        double radius = 1.5;
        int particles = 50;
        
        // Stone color particles (brown)
        int r = 139;
        int g = 69;
        int b = 19;
        
        for (int i = 0; i < particles; i++) {
            // Create points on a sphere
            double phi = Math.acos(2 * Math.random() - 1);
            double theta = 2 * Math.PI * Math.random();
            
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);
            
            Vector3 particlePos = player.add(x, y + 1, z); // +1 to center around player torso
            
            player.getLevel().addParticle(new DustParticle(particlePos, r, g, b));
        }
    }
} 