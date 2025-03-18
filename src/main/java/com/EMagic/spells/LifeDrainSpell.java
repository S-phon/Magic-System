package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

public class LifeDrainSpell extends BasicSpell {
    
    private static final int DAMAGE = 8;
    private static final int HEAL_AMOUNT = 4;
    private static final float MAX_RANGE = 10.0f;
    private static final double DRAIN_DURATION = 3.0; // seconds
    private static final int DRAIN_TICKS = (int)(DRAIN_DURATION * 20); // 20 ticks per second
    
    public LifeDrainSpell() {
        super(
            "life_drain",
            "Drains health from the target",
            Element.DARK,
            50, // Mana cost
            50, // Required mastery level
            10000, // Cooldown time in milliseconds (10 seconds)
            Sound.MOB_WITHER_SHOOT // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        // Get the entity the player is looking at
        Entity target = getTargetEntity(player);
        
        if (target == null) {
            player.sendMessage(TextFormat.RED + "No target in range!");
            return SpellCastResult.FAILED;
        }
        
        // Start the life drain effect
        startLifeDrain(player, target);
        
        player.sendMessage(TextFormat.DARK_PURPLE + "You begin to drain life from your target!");
        
        return SpellCastResult.SUCCESS;
    }
    
    private Entity getTargetEntity(Player player) {
        Vector3 position = player.getPosition().add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector();
        
        Entity closestEntity = null;
        double closestDistance = MAX_RANGE;
        
        for (Entity entity : player.getLevel().getEntities()) {
            // Skip the player
            if (entity.getId() == player.getId()) {
                continue;
            }
            
            // Calculate if entity is in front of the player
            Vector3 entityPos = entity.add(0, entity.getHeight() / 2, 0);
            Vector3 toEntity = entityPos.subtract(position);
            double distance = toEntity.length();
            
            // Check if entity is within range
            if (distance > MAX_RANGE) {
                continue;
            }
            
            // Check if entity is in the player's line of sight
            double dot = direction.dot(toEntity.normalize());
            if (dot < 0.8) { // Angle of approximately 35 degrees or less
                continue;
            }
            
            // Check if no blocks are in between
            boolean blocked = false;
            for (double i = 1.0; i < distance; i += 1.0) {
                Vector3 checkPos = position.add(direction.multiply(i));
                if (!player.getLevel().getBlock(checkPos).isTransparent()) {
                    blocked = true;
                    break;
                }
            }
            
            if (blocked) {
                continue;
            }
            
            // Found a potential target
            if (distance < closestDistance) {
                closestEntity = entity;
                closestDistance = distance;
            }
        }
        
        return closestEntity;
    }
    
    private void startLifeDrain(Player player, Entity target) {
        // Create drain tracker
        DrainTracker tracker = new DrainTracker(player, target);
        
        // Schedule the drain effect
        int taskId = player.getServer().getScheduler().scheduleDelayedRepeatingTask(
            player.getServer().getPluginManager().getPlugin("ElementalMagicSystem"),
            tracker,
            1, // Initial delay (1 tick)
            5  // Repeat every 5 ticks (4 times per second)
        ).getTaskId();
        
        // Set the task ID so it can be cancelled later
        tracker.setTaskId(taskId);
    }
    
    private class DrainTracker implements Runnable {
        private Player caster;
        private Entity target;
        private int taskId;
        private int ticksRemaining;
        private boolean stopped = false;
        
        public DrainTracker(Player caster, Entity target) {
            this.caster = caster;
            this.target = target;
            this.ticksRemaining = DRAIN_TICKS;
        }
        
        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }
        
        @Override
        public void run() {
            // Check if the drain should continue
            if (stopped || ticksRemaining <= 0 || 
                !caster.isAlive() || !target.isAlive() ||
                caster.getLevel() != target.getLevel() ||
                caster.distance(target) > MAX_RANGE) {
                
                // Stop the effect
                caster.getServer().getScheduler().cancelTask(taskId);
                return;
            }
            
            // Drain life from target
            double damagePerTick = (double) DAMAGE / (DRAIN_TICKS / 5);
            double healPerTick = (double) HEAL_AMOUNT / (DRAIN_TICKS / 5);
            
            // Apply damage to target
            target.attack(new EntityDamageEvent(
                target,
                DamageCause.MAGIC,
                (float) damagePerTick
            ));
            
            // Heal the caster
            caster.setHealth(Math.min(
                caster.getHealth() + (float) healPerTick,
                caster.getMaxHealth()
            ));
            
            // Create visual effect
            createDrainParticles(caster, target);
            
            // Decrease ticks
            ticksRemaining -= 5;
        }
        
        private void createDrainParticles(Player caster, Entity target) {
            // Get positions
            Vector3 targetPos = target.add(0, target.getHeight() / 2, 0);
            Vector3 casterPos = caster.add(0, caster.getEyeHeight() - 0.5, 0);
            Vector3 direction = casterPos.subtract(targetPos).normalize();
            
            // Draw a trail of particles from target to caster
            double distance = caster.distance(target);
            int particleCount = (int) (distance * 2);
            
            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                double x = targetPos.x + (casterPos.x - targetPos.x) * progress;
                double y = targetPos.y + (casterPos.y - targetPos.y) * progress;
                double z = targetPos.z + (casterPos.z - targetPos.z) * progress;
                
                // Add some waviness to the particle path
                double offset = Math.sin(progress * Math.PI * 2) * 0.25;
                Vector3 perpendicular = new Vector3(-direction.z, 0, direction.x).normalize().multiply(offset);
                
                Vector3 particlePos = new Vector3(x, y, z).add(perpendicular);
                
                // Dark red/crimson color for the drain effect (r=128, g=0, b=32)
                caster.getLevel().addParticle(new DustParticle(
                    particlePos,
                    128, 0, 32
                ));
            }
            
            // Add small explosion effect at target
            for (int i = 0; i < 3; i++) {
                Vector3 offset = new Vector3(
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
                );
                
                caster.getLevel().addParticle(new DustParticle(
                    targetPos.add(offset),
                    128, 0, 32
                ));
            }
            
            // Add healing effect at caster
            for (int i = 0; i < 2; i++) {
                Vector3 offset = new Vector3(
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5,
                    (Math.random() - 0.5) * 0.5
                );
                
                caster.getLevel().addParticle(new DustParticle(
                    casterPos.add(offset),
                    64, 0, 16
                ));
            }
        }
    }
} 