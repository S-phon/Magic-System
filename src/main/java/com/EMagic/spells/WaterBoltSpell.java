package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.level.particle.WaterParticle;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.ArrayList;
import java.util.List;

public class WaterBoltSpell extends BasicSpell {
    
    private static final int MAX_DISTANCE = 20;
    private static final int DAMAGE = 3;
    private static final int KNOCKBACK = 3;
    
    public WaterBoltSpell() {
        super(
            "water_bolt",
            "Shoots a bolt of water that pushes entities",
            Element.WATER,
            20, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time (3 seconds)
            Sound.BUCKET_FILL_WATER // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Vector3 startPos = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector();
        
        // Create water particles along the bolt's path
        createWaterBoltParticles(player, direction);
        
        // Check for entities in the bolt's path
        List<Entity> hitEntities = rayTraceEntities(player, startPos, direction, MAX_DISTANCE);
        
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.WATER);
        float damageAmount = DAMAGE;
        float knockbackStrength = KNOCKBACK;
        
        // Scale effects based on mastery level
        if (masteryLevel >= 75) {
            damageAmount *= 1.5;
            knockbackStrength *= 1.5;
            player.sendMessage(TextFormat.AQUA + "Your mastery of water increases the bolt's power!");
        } else if (masteryLevel >= 50) {
            damageAmount *= 1.25;
            knockbackStrength *= 1.25;
            player.sendMessage(TextFormat.BLUE + "Your growing mastery strengthens your water bolt!");
        }
        
        // Apply effects to hit entities
        for (Entity entity : hitEntities) {
            // Damage entity
            entity.attack(new EntityDamageByEntityEvent(
                player, entity, DamageCause.MAGIC, damageAmount
            ));
            
            // Apply knockback
            Vector3 knockbackDirection = direction.multiply(knockbackStrength);
            entity.setMotion(knockbackDirection);
            
            // Water particle effect at hit location
            for (int i = 0; i < 10; i++) {
                double offsetX = Math.random() * 1 - 0.5;
                double offsetY = Math.random() * 1 - 0.5;
                double offsetZ = Math.random() * 1 - 0.5;
                
                player.getLevel().addParticle(new WaterParticle(
                    entity.add(offsetX, offsetY + 1, offsetZ)
                ));
            }
        }
        
        if (hitEntities.isEmpty()) {
            // Create water splash particles at the end of the bolt's path if no entity was hit
            Vector3 endPos = startPos.add(direction.multiply(MAX_DISTANCE));
            
            for (int i = 0; i < 20; i++) {
                double offsetX = Math.random() * 2 - 1;
                double offsetY = Math.random() * 2 - 1;
                double offsetZ = Math.random() * 2 - 1;
                
                player.getLevel().addParticle(new WaterParticle(
                    endPos.add(offsetX, offsetY, offsetZ)
                ));
            }
        }
        
        player.sendMessage(TextFormat.BLUE + "You cast " + TextFormat.AQUA + "Water Bolt" + TextFormat.BLUE + "!");
        return SpellCastResult.SUCCESS;
    }
    
    /**
     * Creates water particles along the bolt's path
     */
    private void createWaterBoltParticles(Player player, Vector3 direction) {
        for (double i = 1; i <= MAX_DISTANCE; i += 0.5) {
            Vector3 pos = player.add(0, player.getEyeHeight(), 0).add(direction.multiply(i));
            
            // Alternate between water and bubble particles
            if (i % 1 == 0) {
                player.getLevel().addParticle(new WaterParticle(pos));
            } else {
                player.getLevel().addParticle(new BubbleParticle(pos));
            }
        }
    }
    
    /**
     * Performs a ray trace to find entities in the path of the bolt
     */
    private List<Entity> rayTraceEntities(Player player, Vector3 start, Vector3 direction, double maxDistance) {
        List<Entity> hitEntities = new ArrayList<>();
        Vector3 end = start.add(direction.multiply(maxDistance));
        
        // Get all entities in the level
        Entity[] entities = player.getLevel().getEntities();
        
        for (Entity entity : entities) {
            // Skip the player casting the spell
            if (entity == player) {
                continue;
            }
            
            // Skip entities that are too far away
            if (entity.distance(player) > maxDistance) {
                continue;
            }
            
            // Get entity bounding box
            AxisAlignedBB boundingBox = entity.getBoundingBox().expand(0.5, 0.5, 0.5);
            
            // Check if ray intersects with entity
            if (entityRayIntersection(start, end, boundingBox)) {
                hitEntities.add(entity);
            }
        }
        
        return hitEntities;
    }
    
    /**
     * Checks if a ray intersects with an entity's bounding box
     */
    private boolean entityRayIntersection(Vector3 start, Vector3 end, AxisAlignedBB boundingBox) {
        if (boundingBox.isVectorInside(start)) {
            return true;
        }
        
        // Sample points along the ray
        for (double t = 0; t <= 1.0; t += 0.1) {
            Vector3 point = start.add(end.subtract(start).multiply(t));
            if (point.x >= boundingBox.getMinX() && point.x <= boundingBox.getMaxX() &&
                point.y >= boundingBox.getMinY() && point.y <= boundingBox.getMaxY() &&
                point.z >= boundingBox.getMinZ() && point.z <= boundingBox.getMaxZ()) {
                return true;
            }
        }
        
        return false;
    }
} 