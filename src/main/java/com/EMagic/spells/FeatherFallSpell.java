package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

public class FeatherFallSpell extends BasicSpell {
    
    private static final String EFFECT_NAME = "feather_fall";
    private static final int DURATION = 300; // Duration in seconds
    
    public FeatherFallSpell() {
        super(
            "feather_fall",
            "Reduces fall damage",
            Element.AIR,
            15, // Mana cost
            1,  // Required mastery level
            30000, // Cooldown time 
            Sound.RANDOM_LEVELUP // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        long endTime = System.currentTimeMillis() + (DURATION * 1000);
        magicPlayer.setEffectEndTime(EFFECT_NAME, endTime);
        magicPlayer.setEffectData(EFFECT_NAME + ".reduction", 0.8f); 
        createFeatherEffect(player);
        player.sendMessage(TextFormat.AQUA + "You feel light as a feather! Fall damage reduced for " + DURATION + " seconds.");
        
        return SpellCastResult.SUCCESS;
    }
    
    private void createFeatherEffect(Player player) {
        Vector3 position = player.add(0, 0.5, 0);

        for (int i = 0; i < 2; i++) { 
            double height = i * 0.5;
            for (int j = 0; j < 16; j++) { 
                double angle = j * (Math.PI / 8);
                double radius = 0.8;
                
                double x = position.x + Math.cos(angle) * radius;
                double y = position.y + height;
                double z = position.z + Math.sin(angle) * radius;
                
                Vector3 particlePos = new Vector3(x, y, z);
                player.getLevel().addParticle(new GenericParticle(
                    particlePos,
                    Particle.TYPE_DUST
                ));
            }
        }
    }
} 
