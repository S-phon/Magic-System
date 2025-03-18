package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.Random;

public class FireballSpell extends BasicSpell {
    
    private static final int MAX_TARGET_DISTANCE = 25;
    private Random random = new Random();
    
    public FireballSpell() {
        super(
            "fireball",
            "Creates fire where you're looking",
            Element.FIRE,
            25, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time 
            Sound.MOB_GHAST_FIREBALL // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Block targetBlock = player.getTargetBlock(MAX_TARGET_DISTANCE);
        
        if (targetBlock == null) {
            player.sendMessage(TextFormat.RED + "No target in range!");
            return SpellCastResult.FAILED;
        }
 
        Vector3 playerPos = player.getPosition();
        Vector3 targetPos = new Vector3(targetBlock.getX(), targetBlock.getY(), targetBlock.getZ());
        Vector3 direction = targetPos.subtract(playerPos).normalize();

        createElementalParticleTrail(player, direction);

        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.FIRE);
        int maxFireCount = 50; 
        
        if (masteryLevel >= 75) {
            maxFireCount = 100;
            player.sendMessage(TextFormat.GOLD + "Your mastery of fire allows you to create a massive blaze!");
        } else if (masteryLevel >= 50) {
            maxFireCount = 75;
            player.sendMessage(TextFormat.YELLOW + "Your growing mastery of fire creates a larger area of flames!");
        }
        
        createFireEffect(player, targetPos, maxFireCount);
        
        player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Fireball" + TextFormat.RED + "!");
        
        return SpellCastResult.SUCCESS;
    }
    
    /**
     * Creates fire effect at the target location
     * @param caster The player who cast the spell
     * @param position The position to create fire
     * @param maxFireCount Maximum number of fire blocks to create
     */
    private void createFireEffect(Player caster, Vector3 position, int maxFireCount) {
        if (position == null || caster.getLevel() == null) return;
        
        Level level = caster.getLevel();
        
        level.addParticle(new HugeExplodeSeedParticle(position));
        level.addSound(position, Sound.RANDOM_EXPLODE);
        
        int radius = random.nextInt(2) + 1; // 1 to 2

        int count = placeFireInRadius(position, level, radius, maxFireCount);

        caster.sendMessage("§c§lYour spell created a fire with radius " + radius + " (" + count + " blocks)!");
    }
    
    /**
     * Places fire blocks in a radius around the given location
     * @param center The center location
     * @param level The level to place blocks in
     * @param radius The radius to place fire blocks
     * @param maxFireCount Maximum number of fire blocks to create
     * @return Number of fire blocks placed
     */
    private int placeFireInRadius(Vector3 center, Level level, int radius, int maxFireCount) {
        if (level == null) return 0;
        
        int count = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                double dist = Math.sqrt(x * x + z * z);
                
                if (dist <= radius && (dist <= 1 || random.nextDouble() > 0.3)) {
                    for (int y = -2; y <= 2; y++) {
                        Vector3 checkPos = new Vector3(center.x + x, center.y + y, center.z + z);
                        Vector3 abovePos = new Vector3(checkPos.x, checkPos.y + 1, checkPos.z);
                        Block block = level.getBlock(checkPos);
                        Block blockAbove = level.getBlock(abovePos);

                        if (block.isSolid() && blockAbove.getId() == 0) {
                            level.setBlock(blockAbove, Block.get(51)); 
                            count++;

                            break;
                        }
                    }
                    
                    if (count >= maxFireCount) {
                        return count;
                    }
                }
            }
        }
        
        return count;
    }
    
    private Vector3 rotateVector(Vector3 vec, double degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        
        return new Vector3(
                vec.x * cos - vec.z * sin,
                vec.y,
                vec.x * sin + vec.z * cos
        );
    }
} 