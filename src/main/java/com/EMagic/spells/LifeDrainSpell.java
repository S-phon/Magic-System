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
    private static final double DRAIN_DURATION = 3.0; 
    private static final int DRAIN_TICKS = (int)(DRAIN_DURATION * 20); 
    
    public LifeDrainSpell() {
        super(
            "life_drain",
            "Drains health from the target",
            Element.DARK,
            50, // Mana cost
            50, // Required mastery level
            10000, // Cooldown time
            Sound.MOB_WITHER_SHOOT // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Entity target = getTargetEntity(player);
        
        if (target == null) {
            player.sendMessage(TextFormat.RED + "No target in range!");
            return SpellCastResult.FAILED;
        }

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
            if (entity.getId() == player.getId()) {
                continue;
            }

            Vector3 entityPos = entity.add(0, entity.getHeight() / 2, 0);
            Vector3 toEntity = entityPos.subtract(position);
            double distance = toEntity.length();
            if (distance > MAX_RANGE) {
                continue;
            }

            double dot = direction.dot(toEntity.normalize());
            if (dot < 0.8) {
                continue;
            }

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
            
            if (distance < closestDistance) {
                closestEntity = entity;
                closestDistance = distance;
            }
        }
        
        return closestEntity;
    }
    
    private void startLifeDrain(Player player, Entity target) {
        DrainTracker tracker = new DrainTracker(player, target);
        int taskId = player.getServer().getScheduler().scheduleDelayedRepeatingTask(
            player.getServer().getPluginManager().getPlugin("ElementalMagicSystem"),
            tracker,
            1, // Initial delay
            5  // Repeat every 5 ticks 
        ).getTaskId();
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
            if (stopped || ticksRemaining <= 0 || 
                !caster.isAlive() || !target.isAlive() ||
                caster.getLevel() != target.getLevel() ||
                caster.distance(target) > MAX_RANGE) {
                caster.getServer().getScheduler().cancelTask(taskId);
                return;
            }

            double damagePerTick = (double) DAMAGE / (DRAIN_TICKS / 5);
            double healPerTick = (double) HEAL_AMOUNT / (DRAIN_TICKS / 5);
            target.attack(new EntityDamageEvent(
                target,
                DamageCause.MAGIC,
                (float) damagePerTick
            ));

            caster.setHealth(Math.min(
                caster.getHealth() + (float) healPerTick,
                caster.getMaxHealth()
            ));

            createDrainParticles(caster, target);
            ticksRemaining -= 5;
        }
        
        private void createDrainParticles(Player caster, Entity target) {
            Vector3 targetPos = target.add(0, target.getHeight() / 2, 0);
            Vector3 casterPos = caster.add(0, caster.getEyeHeight() - 0.5, 0);
            Vector3 direction = casterPos.subtract(targetPos).normalize();
            double distance = caster.distance(target);
            int particleCount = (int) (distance * 2);
            
            for (int i = 0; i < particleCount; i++) {
                double progress = (double) i / particleCount;
                double x = targetPos.x + (casterPos.x - targetPos.x) * progress;
                double y = targetPos.y + (casterPos.y - targetPos.y) * progress;
                double z = targetPos.z + (casterPos.z - targetPos.z) * progress;
                double offset = Math.sin(progress * Math.PI * 2) * 0.25;
                Vector3 perpendicular = new Vector3(-direction.z, 0, direction.x).normalize().multiply(offset);
                Vector3 particlePos = new Vector3(x, y, z).add(perpendicular);
                caster.getLevel().addParticle(new DustParticle(
                    particlePos,
                    128, 0, 32
                ));
            }
            
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
