package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.HugeExplodeSeedParticle;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.level.Explosion;
import cn.nukkit.level.particle.ExplodeParticle;
import cn.nukkit.level.particle.SmokeParticle;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.Random;

public class MeteorSpell extends BasicSpell {
    
    private static final int MAX_TARGET_DISTANCE = 50;
    private static final int METEOR_HEIGHT = 20;
    private Random random = new Random();
    
    public MeteorSpell() {
        super(
            "meteor",
            "Summons a meteor from the sky",
            Element.FIRE,
            150, // Mana cost
            75, // Required mastery level
            15000, // Cooldown time ( 15sec )
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
        int explosionPower = 5;
        
        if (masteryLevel >= 150) {
            explosionPower = 15;
            player.sendMessage(TextFormat.GOLD + "Your immense mastery of fire amplifies the meteor's power!");
        } else if (masteryLevel >= 100) {
            explosionPower = 10;
            player.sendMessage(TextFormat.YELLOW + "Your advanced fire mastery increases the meteor's destruction!");
        }
        
        summonMeteor(player, targetPos, explosionPower);
        
        player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Meteor" + TextFormat.RED + "!");
        
        return SpellCastResult.SUCCESS;
    }

    private void summonMeteor(Player caster, Vector3 targetPos, int explosionPower) {
        if (targetPos == null || caster.getLevel() == null) return;
        Level level = caster.getLevel();
        Vector3 meteorStartPos = new Vector3(
            targetPos.getX() + (random.nextDouble() * 10) - 5,
            targetPos.getY() + METEOR_HEIGHT,
            targetPos.getZ() + (random.nextDouble() * 10) - 5
        );

        animateMeteorDescent(caster, meteorStartPos, targetPos, explosionPower);
    }

    private void animateMeteorDescent(Player caster, Vector3 startPos, Vector3 targetPos, int explosionPower) {
        Level level = caster.getLevel();
        Vector3 direction = targetPos.subtract(startPos).normalize();
        ElementalMagicSystem plugin = ElementalMagicSystem.getInstance();
        Vector3 meteorPos = startPos.clone();
        
        plugin.getServer().getScheduler().scheduleDelayedRepeatingTask(plugin, new cn.nukkit.scheduler.Task() {
            @Override
            public void onRun(int currentTick) {
                for (int i = 0; i < 10; i++) {
                    Vector3 particlePos = meteorPos.clone();
                    particlePos.x += (random.nextDouble() * 2) - 1;
                    particlePos.y += (random.nextDouble() * 2) - 1;
                    particlePos.z += (random.nextDouble() * 2) - 1;
                    
                    level.addParticle(new LavaParticle(particlePos));

                    Vector3 trailPos = particlePos.clone();
                    trailPos.y -= 2 + random.nextDouble() * 3;
                    level.addParticle(new SmokeParticle(trailPos));
                }

                meteorPos.x += direction.x * 1.2;
                meteorPos.y += direction.y * 1.2;
                meteorPos.z += direction.z * 1.2;
                
                level.addSound(meteorPos, Sound.RANDOM_FIZZ);

                if (meteorPos.distance(targetPos) < 2) {
                    createMeteorImpact(caster, targetPos, explosionPower);
                    this.getHandler().cancel();
                }
            }
        }, 5, 2);
    }
    
    private void createMeteorImpact(Player caster, Vector3 position, int explosionPower) {
        Level level = caster.getLevel();

        level.addParticle(new HugeExplodeSeedParticle(position));
        level.addSound(position, Sound.RANDOM_EXPLODE);
 
        Position explosionPos = new Position(position.x, position.y, position.z, level);
        Explosion explosion = new Explosion(explosionPos, explosionPower, null);
        explosion.explodeA();
        explosion.explodeB();

        int fireRadius = explosionPower + 2;
        placeFireInRadius(position, level, fireRadius, 100);
        for (int i = 0; i < 40; i++) {
            Vector3 particlePos = position.clone();
            particlePos.x += (random.nextDouble() * fireRadius * 2) - fireRadius;
            particlePos.y += (random.nextDouble() * 5);
            particlePos.z += (random.nextDouble() * fireRadius * 2) - fireRadius;
            
            level.addParticle(new ExplodeParticle(particlePos));
            
            if (random.nextBoolean()) {
                level.addParticle(new LavaParticle(particlePos));
            }
        }
    }
    
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
                            level.setBlock(abovePos, Block.get(51)); 
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
} 
