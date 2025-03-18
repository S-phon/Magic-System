package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.block.Block;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.List;

public class IgniteSpell extends BasicSpell {
    
    private static final int MAX_TARGET_DISTANCE = 20;
    private static final int FIRE_TICKS = 100; // 5 seconds of fire
    
    public IgniteSpell() {
        super(
            "ignite",
            "Sets target block or entity on fire",
            Element.FIRE,
            10, // Mana cost
            1,  // Required mastery level
            1000, // Cooldown time (1 second)
            Sound.FIRE_IGNITE // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        // Find entity the player is looking at
        Entity targetEntity = getTargetEntity(player, MAX_TARGET_DISTANCE);
        
        if (targetEntity != null) {
            // We found an entity to ignite
            targetEntity.setOnFire(FIRE_TICKS / 20); // Convert ticks to seconds
            
            // Create particle trail to the entity
            Vector3 direction = targetEntity.subtract(player).normalize();
            createElementalParticleTrail(player, direction);
            
            // Success message
            player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Ignite" + TextFormat.RED + " on the target!");
            return SpellCastResult.SUCCESS;
        }
        
        // If no entity found, try to ignite a block
        Block targetBlock = player.getTargetBlock(MAX_TARGET_DISTANCE);
        
        if (targetBlock == null) {
            player.sendMessage(TextFormat.RED + "No target in range!");
            return SpellCastResult.FAILED;
        }
        
        // Create a direction vector to the target block
        Vector3 playerPos = player.getPosition();
        Vector3 targetPos = targetBlock.add(0, 1, 0); // Set fire on top of the block
        Vector3 direction = targetPos.subtract(playerPos).normalize();
        
        // Create particle trail
        createElementalParticleTrail(player, direction);
        
        // Check if we can place fire on this block
        Block aboveBlock = player.level.getBlock(targetPos);
        if (aboveBlock.getId() != 0) {
            player.sendMessage(TextFormat.RED + "Can't set fire there!");
            return SpellCastResult.FAILED;
        }
        
        // Place fire on top of the block
        player.level.setBlock(targetPos, Block.get(51)); // 51 is fire block ID
        
        // Increase mastery effect based on player level
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.FIRE);
        if (masteryLevel >= 50) {
            // For advanced users, set fire to additional blocks
            for (int i = 0; i < 4; i++) {
                Vector3 randomPos = targetPos.add(
                    Math.random() * 2 - 1,
                    0,
                    Math.random() * 2 - 1
                );
                
                Block randomBlock = player.level.getBlock(randomPos);
                if (randomBlock.getId() == 0) {
                    player.level.setBlock(randomPos, Block.get(51));
                }
            }
            player.sendMessage(TextFormat.GOLD + "Your mastery creates additional flames!");
        }
        
        // Success message
        player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Ignite" + TextFormat.RED + "!");
        return SpellCastResult.SUCCESS;
    }
    
    /**
     * Gets the entity that the player is looking at
     * @param player The player
     * @param maxDistance The maximum distance to check
     * @return The target entity, or null if none found
     */
    private Entity getTargetEntity(Player player, double maxDistance) {
        Vector3 pos = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector();
        
        Entity nearestEntity = null;
        double nearestDistance = maxDistance;
        
        // Get all entities in the level
        Entity[] entities = player.getLevel().getEntities();
        
        for (Entity entity : entities) {
            // Skip the player itself
            if (entity == player) {
                continue;
            }
            
            // Skip entities that are too far away
            if (entity.distance(player) > maxDistance) {
                continue;
            }
            
            // Calculate the distance from the entity to the ray
            AxisAlignedBB boundingBox = entity.getBoundingBox().expand(0.5, 0.5, 0.5);
            Vector3 intersection = calculateIntercept(pos, direction, boundingBox, maxDistance);
            
            if (intersection != null) {
                double distance = pos.distance(intersection);
                if (distance < nearestDistance) {
                    nearestEntity = entity;
                    nearestDistance = distance;
                }
            }
        }
        
        return nearestEntity;
    }
    
    /**
     * Calculates the intersection point of a ray with a bounding box
     * @param start The start position of the ray
     * @param direction The direction of the ray
     * @param boundingBox The bounding box to check intersection with
     * @param maxDistance The maximum distance to check
     * @return The intersection point, or null if none found
     */
    private Vector3 calculateIntercept(Vector3 start, Vector3 direction, AxisAlignedBB boundingBox, double maxDistance) {
        // Calculate potential intersections with each face of the bounding box
        Vector3 end = start.add(direction.multiply(maxDistance));
        
        // Check if the ray intersects with the bounding box
        // Using simpler approach by checking if entity's bounding box contains the line segment
        if (!boundingBox.isVectorInside(start) && !isVectorInYZ(start, end, boundingBox)) {
            return null;
        }
        
        // The ray intersects, so calculate the exact intersection point
        // For simplicity, we'll just return a point on the entity
        // Calculate the center of the bounding box
        Vector3 center = new Vector3(
            (boundingBox.getMinX() + boundingBox.getMaxX()) / 2,
            (boundingBox.getMinY() + boundingBox.getMaxY()) / 2,
            (boundingBox.getMinZ() + boundingBox.getMaxZ()) / 2
        );
        
        double distance = start.distance(center);
        return start.add(direction.multiply(distance));
    }
    
    /**
     * Checks if a vector is inside the bounding box on YZ plane
     * Simplified approach for ray-box intersection
     */
    private boolean isVectorInYZ(Vector3 start, Vector3 end, AxisAlignedBB bb) {
        // Simple check to see if any point along the ray is within the entity bounding box
        // This is a simplified approach - not a true ray cast
        double distance = start.distance(end);
        
        // Check a few points along the ray
        for (double i = 0; i <= 1.0; i += 0.1) {
            Vector3 point = start.add(end.subtract(start).multiply(i));
            if (point.x >= bb.getMinX() && point.x <= bb.getMaxX() &&
                point.y >= bb.getMinY() && point.y <= bb.getMaxY() &&
                point.z >= bb.getMinZ() && point.z <= bb.getMaxZ()) {
                return true;
            }
        }
        
        return false;
    }
} 