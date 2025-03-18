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

import java.util.ArrayList;
import java.util.List;

public class ShadowBoltSpell extends BasicSpell {
    
    private static final int DAMAGE = 6;
    private static final int MAX_DISTANCE = 20;
    private static final double BOLT_SPEED = 0.8; 
    private static final int MAX_LIFETIME = 40; 
    
    public ShadowBoltSpell() {
        super(
            "shadow_bolt",
            "Launches a bolt of shadow energy",
            Element.DARK,
            25, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time 
            Sound.MOB_ENDERMEN_PORTAL // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        Vector3 direction = player.getDirectionVector();
        Location startLocation = player.getLocation().add(0, 1.5, 0);
        launchShadowBolt(player, startLocation, direction);
        player.sendMessage(TextFormat.DARK_PURPLE + "You launch a bolt of shadow energy!");
        
        return SpellCastResult.SUCCESS;
    }
    
    private void launchShadowBolt(Player caster, Location startLocation, Vector3 direction) {
        Vector3 position = new Vector3(startLocation.x, startLocation.y, startLocation.z);
        Vector3 normalizedDir = direction.normalize();
        BoltTracker tracker = new BoltTracker(caster, position, normalizedDir);
        int taskId = caster.getServer().getScheduler().scheduleDelayedRepeatingTask(
            caster.getServer().getPluginManager().getPlugin("ElementalMagicSystem"),
            tracker,
            1, // Initial delay 
            1  // Repeat every tick
        ).getTaskId();

        tracker.setTaskId(taskId);
    }
    
    private class BoltTracker implements Runnable {
        private Player caster;
        private Vector3 position;
        private Vector3 direction;
        private int lifetime = 0;
        private boolean hitTarget = false;
        private int taskId;
        
        public BoltTracker(Player caster, Vector3 startPosition, Vector3 direction) {
            this.caster = caster;
            this.position = startPosition;
            this.direction = direction;
        }
        
        public void setTaskId(int taskId) {
            this.taskId = taskId;
        }
        
        @Override
        public void run() {
            if (lifetime >= MAX_LIFETIME || hitTarget) {
                caster.getServer().getScheduler().cancelTask(taskId);
                return;
            }

            createShadowBoltParticles(position);
            position = position.add(direction.multiply(BOLT_SPEED));
            lifetime++;

            if (!caster.getLevel().getBlock(position).isTransparent()) {
                hitTarget = true;
                createImpactEffect(position);
                return;
            }

            checkEntityCollision();
        }
        
        private void checkEntityCollision() {
            for (Entity entity : caster.getLevel().getEntities()) {
                if (entity.getId() == caster.getId()) {
                    continue;
                }

                if (entity.distance(position) <= 1.5) {
                    entity.attack(new EntityDamageEvent(
                        entity,
                        DamageCause.MAGIC,
                        DAMAGE
                    ));

                    createImpactEffect(entity.add(0, entity.getHeight() / 2, 0));
                    hitTarget = true;
                    break;
                }
            }
        }
        
        private void createShadowBoltParticles(Vector3 pos) {
            for (int i = 0; i < 5; i++) {
                Vector3 offset = new Vector3(
                    (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.3,
                    (Math.random() - 0.5) * 0.3
                );
                
                Vector3 particlePos = pos.add(offset);
                caster.getLevel().addParticle(new DustParticle(
                    particlePos,
                    64, 0, 128
                ));
            }
        }
        
        private void createImpactEffect(Vector3 pos) {
            for (int i = 0; i < 25; i++) {
                double radius = 1.5;
                double angle1 = Math.random() * Math.PI * 2;
                double angle2 = Math.random() * Math.PI * 2;
                double x = pos.x + Math.sin(angle1) * Math.cos(angle2) * radius * Math.random();
                double y = pos.y + Math.sin(angle1) * Math.sin(angle2) * radius * Math.random();
                double z = pos.z + Math.cos(angle1) * radius * Math.random();
                
                Vector3 particlePos = new Vector3(x, y, z);
                caster.getLevel().addParticle(new DustParticle(
                    particlePos,
                    32, 0, 64
                ));
            }

            caster.getLevel().addSound(pos, Sound.RANDOM_EXPLODE);
        }
    }
} 
