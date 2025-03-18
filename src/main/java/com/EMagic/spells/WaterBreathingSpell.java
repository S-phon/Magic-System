package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.ShortEntityData;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;
import com.EMagic.ElementalMagicSystem;

public class WaterBreathingSpell extends BasicSpell {
    
    private static final int DURATION = 60; // 1 minute in seconds
    
    public WaterBreathingSpell() {
        super(
            "water_breathing",
            "Allows you to breathe underwater",
            Element.WATER,
            15, // Mana cost
            1,  // Required mastery level
            60000, // Cooldown time (60 seconds)
            Sound.BUBBLE_POP // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.WATER);
        int duration = DURATION;
        
        // Scale duration based on mastery level
        if (masteryLevel >= 75) {
            duration *= 2; // 10 minutes at high mastery
            player.sendMessage(TextFormat.AQUA + "Your mastery of water gives you extended breathing!");
        } else if (masteryLevel >= 50) {
            duration *= 1.5; // 7.5 minutes at medium mastery
            player.sendMessage(TextFormat.BLUE + "Your growing mastery extends your water breathing!");
        }
        
        // Apply water breathing effect using Nukkit's proper potion effect system
        Effect waterBreathing = Effect.getEffect(Effect.WATER_BREATHING);
        waterBreathing.setDuration(duration * 20); // Convert to ticks (20 ticks per second)
        waterBreathing.setAmplifier(0);
        waterBreathing.setVisible(true);
        player.addEffect(waterBreathing);
        
        // Also set the air supply to maximum as a backup
        player.setDataProperty(new ShortEntityData(Entity.DATA_AIR, 400));
        
        // Register a repeating task to maintain air supply
        // This is critical for water breathing to actually work
        ElementalMagicSystem plugin = magicPlayer.getPlugin();
        int taskId = plugin.getServer().getScheduler().scheduleRepeatingTask(() -> {
            // Check if effect is still active
            if (magicPlayer.hasActiveEffect("water_breathing")) {
                // Set air supply to maximum when underwater
                player.setDataProperty(new ShortEntityData(Entity.DATA_AIR, 400));
            }
        }, 20).getTaskId(); // Run every second (20 ticks)
        
        // Store task ID for cancellation when effect ends
        magicPlayer.setEffectData("water_breathing_task", taskId);
        
        // Create bubble particle effect around the player
        for (int i = 0; i < 20; i++) {
            double offsetX = Math.random() * 2 - 1;
            double offsetY = Math.random() * 2;
            double offsetZ = Math.random() * 2 - 1;
            
            player.getLevel().addParticle(new BubbleParticle(
                player.add(offsetX, offsetY, offsetZ)
            ));
        }
        
        // Store the effect end time in player data
        long currentTime = System.currentTimeMillis();
        long endTime = currentTime + (duration * 1000);
        magicPlayer.setEffectEndTime("water_breathing", endTime);
        
        // Success message
        int minutes = duration / 60;
        int seconds = duration % 60;
        player.sendMessage(TextFormat.BLUE + "You cast " + TextFormat.AQUA + "Water Breathing" + 
                          TextFormat.BLUE + "! You can breathe underwater for " + 
                          minutes + " minutes and " + seconds + " seconds.");
        
        return SpellCastResult.SUCCESS;
    }
} 