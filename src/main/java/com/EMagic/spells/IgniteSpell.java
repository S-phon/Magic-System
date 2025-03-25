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
    private static final int FIRE_TICKS = 100;
    
    public IgniteSpell() {
        super(
            "ignite",
            "Sets target block or entity on fire",
            Element.FIRE,
            10, // Mana cost
            1,  // Required mastery level
            1000, // Cooldown time
            Sound.FIRE_IGNITE // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Entity targetEntity = getTargetEntity(player, MAX_TARGET_DISTANCE);
        
        if (targetEntity != null) {
            targetEntity.setOnFire(FIRE_TICKS / 20);
            Vector3 direction = targetEntity.subtract(player).normalize();
            createElementalParticleTrail(player, direction);
            player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Ignite" + TextFormat.RED + " on the target!");
            return SpellCastResult.SUCCESS;
        }

        Block targetBlock = player.getTargetBlock(MAX_TARGET_DISTANCE);
        
        if (targetBlock == null) {
            player.sendMessage(TextFormat.RED + "No target in range!");
            return SpellCastResult.FAILED;
        }

        Vector3 playerPos = player.getPosition();
        Vector3 targetPos = targetBlock.add(0, 1, 0); 
        Vector3 direction = targetPos.subtract(playerPos).normalize();
        createElementalParticleTrail(player, direction);
        Block aboveBlock = player.level.getBlock(targetPos);
        if (aboveBlock.getId() != 0) {
            player.sendMessage(TextFormat.RED + "Can't set fire there!");
            return SpellCastResult.FAILED;
        }

        player.level.setBlock(targetPos, Block.get(51)); // 51 fire block ID
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.FIRE);
        if (masteryLevel >= 50) {
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
        Entity[] entities = player.getLevel().getEntities();
        
        for (Entity entity : entities) {
            if (entity == player) {
                continue;
            }

            if (entity.distance(player) > maxDistance) {
                continue;
            }

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
        Vector3 end = start.add(direction.multiply(maxDistance));
        if (!boundingBox.isVectorInside(start) && !isVectorInYZ(start, end, boundingBox)) {
            return null;
        }
        
        Vector3 center = new Vector3(
            (boundingBox.getMinX() + boundingBox.getMaxX()) / 2,
            (boundingBox.getMinY() + boundingBox.getMaxY()) / 2,
            (boundingBox.getMinZ() + boundingBox.getMaxZ()) / 2
        );
        
        double distance = start.distance(center);
        return start.add(direction.multiply(distance));
    }
    
    private boolean isVectorInYZ(Vector3 start, Vector3 end, AxisAlignedBB bb) {
        double distance = start.distance(end);
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
