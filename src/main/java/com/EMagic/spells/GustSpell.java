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
            3000, // Cooldown time
            Sound.BUBBLE_POP // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Vector3 direction = player.getDirectionVector();
        Location sourceLocation = player.getLocation();
        Entity[] entitiesArray = player.getLevel().getEntities();
        List<Entity> entities = new ArrayList<>(Arrays.asList(entitiesArray));
        int affectedEntities = 0;
        Vector3 playerPos = player.getPosition();
        
        for (Entity entity : entities) {
            if (entity.getId() == player.getId()) {
                continue;
            }
            
            double distance = entity.distance(player);
            if (distance <= RANGE) {
                Vector3 toEntity = entity.subtract(playerPos).normalize();
                double dotProduct = direction.dot(toEntity);
                if (dotProduct > 0.5) {
                    Vector3 knockbackDir = entity.subtract(player).normalize();
                    int knockbackStrength = BASE_KNOCKBACK_STRENGTH;
                    if (distance >= MIN_EFFECTIVE_RANGE) {
                        knockbackStrength = BOOSTED_KNOCKBACK_STRENGTH;
                    }

                    knockbackDir.y += 0.3;
                    knockbackDir = knockbackDir.normalize();
                    entity.setMotion(knockbackDir.multiply(knockbackStrength));
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
        
        createWindEffect(player, direction);
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
