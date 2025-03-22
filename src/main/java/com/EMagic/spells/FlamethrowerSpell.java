package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.level.Level;
import cn.nukkit.block.Block;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FlamethrowerSpell extends BasicSpell {
    
    private static final int MAX_RANGE = 10;
    private static final int DAMAGE_PER_TICK = 1;
    private static final int CONTINUOUS_MANA_COST = 30;
    private static final int MAX_DURATION_SECONDS = 10;
    
    private Random random = new Random();
    private Map<String, TaskHandler> activeFlamethrowers = new HashMap<>();
    private ElementalMagicSystem plugin;
    
    public FlamethrowerSpell() {
        super(
            "flamethrower",
            "Creates a continuous stream of fire in front of you",
            Element.FIRE,
            30, // Initial mana cost
            85, // Required mastery level
            5000, // Cooldown time (5sec)
            Sound.MOB_BLAZE_BREATHE // Cast sound
        );
        
        this.plugin = ElementalMagicSystem.getInstance();
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        if (activeFlamethrowers.containsKey(player.getName())) {
            stopFlamethrower(player);
            return SpellCastResult.SUCCESS;
        }
        
        player.sendMessage(TextFormat.RED + "You cast " + TextFormat.GOLD + "Flamethrower" + TextFormat.RED + "!");
        
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.FIRE);
        int range = MAX_RANGE;
        int duration = MAX_DURATION_SECONDS;
        
        if (masteryLevel >= 150) {
            range += 3;
            duration += 5;
            player.sendMessage(TextFormat.GOLD + "Your mastery of fire extends your flamethrower's range and duration!");
        } else if (masteryLevel >= 120) {
            range += 2;
            duration += 3;
            player.sendMessage(TextFormat.YELLOW + "Your growing mastery of fire increases your flamethrower's reach!");
        }
        
        startFlamethrower(player, magicPlayer, range, duration);
        return SpellCastResult.SUCCESS;
    }
    
    private void startFlamethrower(Player player, MagicPlayer magicPlayer, int range, int duration) {
        TaskHandler task = plugin.getServer().getScheduler().scheduleRepeatingTask(() -> {
            if (!player.isOnline() || player.isClosed()) {
                stopFlamethrower(player);
                return;
            }

            if (magicPlayer.getMana() < CONTINUOUS_MANA_COST) {
                player.sendMessage(TextFormat.RED + "You've run out of mana to sustain your flamethrower!");
                stopFlamethrower(player);
                return;
            }

            magicPlayer.reduceMana(CONTINUOUS_MANA_COST);
            Vector3 direction = player.getDirectionVector();
            Position playerEyePos = new Position(
                player.x,
                player.y + player.getEyeHeight(),
                player.z,
                player.level
            );
          
            createFlameParticles(player, direction, range);
            damageEntitiesInPath(player, playerEyePos, direction, range);
            player.getLevel().addSound(player, Sound.MOB_BLAZE_BREATHE);
            
        }, 20); 

        activeFlamethrowers.put(player.getName(), task);
        plugin.getServer().getScheduler().scheduleDelayedTask(() -> {
            if (activeFlamethrowers.containsKey(player.getName())) {
                player.sendMessage(TextFormat.YELLOW + "Your flamethrower spell has ended.");
                stopFlamethrower(player);
            }
        }, duration * 20);
    }

    private void stopFlamethrower(Player player) {
        TaskHandler task = activeFlamethrowers.remove(player.getName());
        if (task != null) {
            task.cancel();
        }
    }

    private void createFlameParticles(Player player, Vector3 direction, int range) {
        Level level = player.getLevel();
        Position playerEyePos = new Position(
            player.x,
            player.y + player.getEyeHeight(),
            player.z,
            player.level
        );
        
        for (double i = 0.5; i <= range; i += 0.5) {
            double spread = i * 0.15;
            for (int j = 0; j < 5; j++) {
                double offsetX = (random.nextDouble() - 0.5) * spread;
                double offsetY = (random.nextDouble() - 0.5) * spread;
                double offsetZ = (random.nextDouble() - 0.5) * spread;
                double x = playerEyePos.x + direction.x * i + offsetX;
                double y = playerEyePos.y + direction.y * i + offsetY;
                double z = playerEyePos.z + direction.z * i + offsetZ;

                if (random.nextBoolean()) {
                    level.addParticle(new FlameParticle(new Vector3(x, y, z)));
                } else {
                    level.addParticle(new LavaParticle(new Vector3(x, y, z)));
                }
              
                if (random.nextDouble() < 0.03) {
                    Vector3 blockPos = new Vector3(x, y, z);
                    Block block = level.getBlock(blockPos);
                    if (block.getId() == 0) {
                        Block blockBelow = level.getBlock(blockPos.add(0, -1, 0));
                        if (blockBelow.isSolid()) {
                            level.setBlock(blockPos, Block.get(51));
                        }
                    }
                }
            }
        }
    }

    private void damageEntitiesInPath(Player caster, Position startPos, Vector3 direction, int range) {
        Level level = caster.getLevel();
        double width = 2.0; 
        
        for (Entity entity : level.getEntities()) {
            if (entity.equals(caster)) continue;
            if (entity.distance(caster) > range) continue;

            Vector3 entityDelta = entity.subtract(startPos);
            double dot = entityDelta.dot(direction);

            if (dot <= 0) continue;

            Vector3 projected = direction.multiply(dot);
            double distanceFromLine = entityDelta.subtract(projected).length();
            double maxDistAtThisRange = width * (dot / range);
            if (distanceFromLine > maxDistAtThisRange) continue;
            entity.setOnFire(3); 
            entity.attack(new EntityDamageEvent(
                entity,
                DamageCause.FIRE,
                DAMAGE_PER_TICK
            ));
        }
    }
} 
