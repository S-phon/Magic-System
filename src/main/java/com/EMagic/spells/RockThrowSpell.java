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
    private static final Map<Entity, RockProjectileData> ROCK_PROJECTILES = new HashMap<>();
    
    public RockThrowSpell() {
        super(
            "rock_throw",
            "Throws a rock that deals damage",
            Element.EARTH,
            15, // Mana cost
            25, // Required mastery level
            3000, // Cooldown time
            Sound.RANDOM_EXPLODE // Cast sound
        );
    }
    
    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        int masteryLevel = magicPlayer.getElementMasteryLevel(Element.EARTH);
        float damage = DAMAGE;
        if (masteryLevel >= 75) {
            damage = DAMAGE * 1.5f;
            player.sendMessage(TextFormat.DARK_GREEN + "Your mastery of earth creates a deadly projectile!");
        } else if (masteryLevel >= 50) {
            damage = DAMAGE * 1.25f;
            player.sendMessage(TextFormat.GREEN + "Your growing mastery enhances your rock's power!");
        }

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

        EntitySnowball rockProjectile = new EntitySnowball(player.getChunk(), nbt, player);
        rockProjectile.setMotion(player.getDirectionVector().multiply(PROJECTILE_SPEED));
        ROCK_PROJECTILES.put(rockProjectile, new RockProjectileData(damage, player));
        rockProjectile.spawnToAll();
        createRockParticles(player);
        magicPlayer.getPlugin().getServer().getPluginManager().registerEvents(new cn.nukkit.event.Listener() {
            @cn.nukkit.event.EventHandler
            public void onProjectileHit(cn.nukkit.event.entity.ProjectileHitEvent event) {
                if (event.getEntity() == rockProjectile) {
                    createImpactParticles(rockProjectile);
                    RockProjectileData projectileData = ROCK_PROJECTILES.get(rockProjectile);
                    if (projectileData == null) return;
                    ROCK_PROJECTILES.remove(rockProjectile);

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
        
        player.sendMessage(TextFormat.DARK_GREEN + "You cast " + TextFormat.GREEN + "Rock Throw" + TextFormat.DARK_GREEN + "!");
        return SpellCastResult.SUCCESS;
    }

    private void createRockParticles(Player player) {
        Vector3 pos = player.add(0, player.getEyeHeight(), 0);
        Vector3 direction = player.getDirectionVector();

        int r = 139;
        int g = 69;
        int b = 19;
        
        for (int i = 0; i < 15; i++) {
            double distance = 0.5 + (i * 0.1);
            Vector3 particlePos = pos.add(direction.multiply(distance));
            particlePos = particlePos.add(
                Math.random() * 0.2 - 0.1,
                Math.random() * 0.2 - 0.1,
                Math.random() * 0.2 - 0.1
            );
            
            player.getLevel().addParticle(new DustParticle(particlePos, r, g, b));
        }
    }
    
    private void createImpactParticles(Entity entity) {
        Vector3 pos = entity.getPosition();
        
        int r = 139;
        int g = 69;
        int b = 19;
        
        for (int i = 0; i < 20; i++) {
            double offsetX = Math.random() * 1.0 - 0.5;
            double offsetY = Math.random() * 1.0 - 0.5;
            double offsetZ = Math.random() * 1.0 - 0.5;
            
            Vector3 particlePos = pos.add(offsetX, offsetY, offsetZ);
            entity.getLevel().addParticle(new DustParticle(particlePos, r, g, b));
        }

        entity.getLevel().addSound(pos, Sound.DIG_STONE);
    }

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
