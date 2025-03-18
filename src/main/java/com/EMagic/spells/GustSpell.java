package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class GustSpell extends BasicSpell {
    
    private static final int RANGE = 10;
    private static final int MIN_EFFECTIVE_RANGE = 8;
    private static final int BASE_KNOCKBACK_STRENGTH = 4;
    private static final int BOOSTED_KNOCKBACK_STRENGTH = 6;
    
    public GustSpell() {
        super(
            "gust",
            "Creates a gust of wind that pushes entities",
            Element.AIR,
            20, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time in milliseconds
            Sound.BUBBLE_POP // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        // Get player's direction
        Vector3 direction = player.getDirectionVector();
        Location sourceLocation = player.getLocation();
        
        // Get entities in range
        Entity[] entitiesArray = player.getLevel().getEntities();
        List<Entity> entities = new ArrayList<>(Arrays.asList(entitiesArray));
        int affectedEntities = 0;
        
        // Create the cone of effect in front of player
        Vector3 playerPos = player.getPosition();
        
        for (Entity entity : entities) {
            // Skip the caster
            if (entity.getId() == player.getId()) {
                continue;
            }
            
            // Check if entity is within range
            double distance = entity.distance(player);
            if (distance <= RANGE) {
                // Check if entity is in front of the player (in a 120-degree cone)
                Vector3 toEntity = entity.subtract(playerPos).normalize();
                double dotProduct = direction.dot(toEntity);
                
                // If dot product > 0.5, entity is within a ~60 degree cone in front of player
                if (dotProduct > 0.5) {
                    // Calculate knockback direction (away from player)
                    Vector3 knockbackDir = entity.subtract(player).normalize();
                    
                    // Apply stronger knockback for entities at 8-10 block range
                    int knockbackStrength = BASE_KNOCKBACK_STRENGTH;
                    if (distance >= MIN_EFFECTIVE_RANGE) {
                        knockbackStrength = BOOSTED_KNOCKBACK_STRENGTH;
                    }
                    
                    // Apply knockback force - boost upward component to make it more visible
                    knockbackDir.y += 0.3;
                    knockbackDir = knockbackDir.normalize();
                    
                    // Apply the knockback with a stronger effect
                    entity.setMotion(knockbackDir.multiply(knockbackStrength));
                    
                    // Add visual effect
                    for (int i = 0; i < 5; i++) {
                        player.getLevel().addParticle(new GenericParticle(
                            entity.add(0, 1, 0),
                            Particle.TYPE_VILLAGER_HAPPY
                        ));
                    }
                    
                    affectedEntities++;
                }
            }
        }
        
        // Create particle effect in a cone in front of the player
        createWindEffect(player, direction);
        
        // Give feedback to the player
        if (affectedEntities > 0) {
            player.sendMessage(TextFormat.GREEN + "You cast a powerful gust of wind, pushing " + 
                    affectedEntities + " entities away!");
        } else {
            player.sendMessage(TextFormat.YELLOW + "You cast a gust of wind, but nothing was affected.");
        }
        
        return SpellCastResult.SUCCESS;
    }
    
    private void createWindEffect(Player player, Vector3 direction) {
        Vector3 position = player.add(0, 1.5, 0);
        Vector3 directionNorm = direction.normalize();
        
        for (int i = 0; i < 30; i++) {
            double distance = i * 0.5;
            Vector3 particlePos = position.add(directionNorm.multiply(distance));
            
            // Add some randomness to create a cone effect
            particlePos = particlePos.add(
                (Math.random() - 0.5) * distance * 0.5,
                (Math.random() - 0.5) * distance * 0.5,
                (Math.random() - 0.5) * distance * 0.5
            );
            
            player.getLevel().addParticle(new GenericParticle(
                particlePos,
                Particle.TYPE_WHITE_SMOKE
            ));
        }
    }
} 