package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntitySnowball;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.HashMap;
import java.util.Map;

public class RockThrowSpell extends BasicSpell {
    
    private static final int DAMAGE = 7;
    private static final float PROJECTILE_SPEED = 1.2f;
    
    // Map to track rock projectiles and their damage values
    private static final Map<Entity, RockProjectileData> ROCK_PROJECTILES = new HashMap<>();
    
    public RockThrowSpell() {
        super(
            "rock_throw",
            "Throws a rock that deals damage",
            Element.EARTH,
            15, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time (3 seconds)
            Sound.RANDOM_EXPLODE // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.EARTH);
        float damage = DAMAGE;
        
        // Scale damage based on mastery level
        if (masteryLevel >= 75) {
            damage = DAMAGE * 1.5f;
            player.sendMessage(TextFormat.DARK_GREEN + "Your mastery of earth creates a deadly projectile!");
        } else if (masteryLevel >= 50) {
            damage = DAMAGE * 1.25f;
            player.sendMessage(TextFormat.GREEN + "Your growing mastery enhances your rock's power!");
        }
        
        // Create a "rock" (using snowball as base)
        CompoundTag nbt = new CompoundTag()
            .putList(new ListTag<DoubleTag>("Pos")
                .add(new DoubleTag("", player.x))
                .add(new DoubleTag("", player.y + player.getEyeHeight()))
                .add(new DoubleTag("", player.z)))
            .putList(new ListTag<DoubleTag>("Motion")
                .add(new DoubleTag("", -Math.sin(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI) * PROJECTILE_SPEED))
                .add(new DoubleTag("", -Math.sin(player.pitch / 180 * Math.PI) * PROJECTILE_SPEED))
                .add(new DoubleTag("", Math.cos(player.yaw / 180 * Math.PI) * Math.cos(player.pitch / 180 * Math.PI) * PROJECTILE_SPEED)))
            .putList(new ListTag<FloatTag>("Rotation")
                .add(new FloatTag("", (float) player.yaw))
                .add(new FloatTag("", (float) player.pitch)));
        
        // Create rock projectile
        EntitySnowball rockProjectile = new EntitySnowball(player.getChunk(), nbt, player);
        rockProjectile.setMotion(player.getDirectionVector().multiply(PROJECTILE_SPEED));
        
        // Store projectile data in our map
        ROCK_PROJECTILES.put(rockProjectile, new RockProjectileData(damage, player));
        
        // Spawn the projectile
        rockProjectile.spawnToAll();
        
        // Create rock particles along the path
        createRockParticles(player);
        
        // Register collision handler
        magicPlayer.getPlugin().getServer().getPluginManager().registerEvents(new cn.nukkit.event.Listener() {
            @cn.nukkit.event.EventHandler
            public void onProjectileHit(cn.nukkit.event.entity.ProjectileHitEvent event) {
                if (event.getEntity() == rockProjectile) {
                    // Create impact particles
                    createImpactParticles(rockProjectile);
                    
                    // Get projectile data from our map
                    RockProjectileData projectileData = ROCK_PROJECTILES.get(rockProjectile);
                    if (projectileData == null) return;
                    
                    // Remove from map after use
                    ROCK_PROJECTILES.remove(rockProjectile);
                    
                    // Damage entity if hit
                    if (event.getMovingObjectPosition().entityHit != null) {
                        Entity hitEntity = event.getMovingObjectPosition().entityHit;
                        hitEntity.attack(new EntityDamageByEntityEvent(
                            player, hitEntity, DamageCause.PROJECTILE, projectileData.getDamage(), 0.5f));
                            
                        player.sendMessage(TextFormat.GREEN + "Your rock hit " + 
                                          TextFormat.WHITE + hitEntity.getName() + 
                                          TextFormat.GREEN + " for " + 
                                          TextFormat.GOLD + projectileData.getDamage() + 
                                          TextFormat.GREEN + " damage!");
                    }
                }
            }
        }, magicPlayer.getPlugin());
        
        // Success message
        player.sendMessage(TextFormat.DARK_GREEN + "You cast " + TextFormat.GREEN + "Rock Throw" + TextFormat.DARK_GREEN + "!");
        
        return SpellCastResult.SUCCESS;
    }
    
    /**
     * Creates rock particles at the projectile start
     */
    private void createRockParticles(Player player) {
        // Create dust particles in front of the player
        Vector3 pos = player.add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector();
        
        // Brown rock-colored particles
        int r = 139;
        int g = 69;
        int b = 19;
        
        for (int i = 0; i < 15; i++) {
            double distance = 0.5 + (i * 0.1);
            Vector3 particlePos = pos.add(direction.multiply(distance));
            
            // Add some randomness to the particles
            particlePos = particlePos.add(
                Math.random() * 0.2 - 0.1,
                Math.random() * 0.2 - 0.1,
                Math.random() * 0.2 - 0.1
            );
            
            player.getLevel().addParticle(new DustParticle(particlePos, r, g, b));
        }
    }
    
    /**
     * Creates rock particles at impact point
     */
    private void createImpactParticles(Entity entity) {
        Vector3 pos = entity.getPosition();
        
        // Brown rock-colored particles for impact
        int r = 139;
        int g = 69;
        int b = 19;
        
        for (int i = 0; i < 20; i++) {
            // Create particles in a sphere around impact point
            double offsetX = Math.random() * 1.0 - 0.5;
            double offsetY = Math.random() * 1.0 - 0.5;
            double offsetZ = Math.random() * 1.0 - 0.5;
            
            Vector3 particlePos = pos.add(offsetX, offsetY, offsetZ);
            entity.getLevel().addParticle(new DustParticle(particlePos, r, g, b));
        }
        
        // Play stone break sound at impact
        entity.getLevel().addSound(pos, Sound.DIG_STONE);
    }
    
    /**
     * Class to store rock projectile data
     */
    private static class RockProjectileData {
        private final float damage;
        private final Player caster;
        
        public RockProjectileData(float damage, Player caster) {
            this.damage = damage;
            this.caster = caster;
        }
        
        public float getDamage() {
            return damage;
        }
        
        public Player getCaster() {
            return caster;
        }
    }
} 