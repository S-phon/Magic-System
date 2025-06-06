package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PurifySpell extends BasicSpell {
    
    private static final float RADIUS = 10.0f;
    private static final int UNDEAD_DAMAGE = 10;
    private static final int[] NEGATIVE_EFFECTS = {
            Effect.POISON,
            Effect.WEAKNESS,
            Effect.SLOWNESS,
            Effect.NAUSEA,
            Effect.BLINDNESS,
            Effect.HUNGER,
            Effect.MINING_FATIGUE,
            Effect.WITHER
    };
    
    public PurifySpell() {
        super(
            "purify",
            "Removes negative effects and damages undead entities",
            Element.LIGHT,
            75, // Mana cost
            75, // Required mastery level
            20000, // Cooldown time
            Sound.RANDOM_EXPLODE // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        createPurifyWave(player);
        int purifiedPlayers = removeNegativeEffects(player);
        int damagedEntities = damageUndeadEntities(player);
        StringBuilder resultMessage = new StringBuilder(TextFormat.YELLOW + "You cast Purify! ");
        
        if (purifiedPlayers > 0) {
            resultMessage.append("Cleansed ").append(purifiedPlayers).append(" players. ");
        }
        
        if (damagedEntities > 0) {
            resultMessage.append("Damaged ").append(damagedEntities).append(" undead creatures.");
        }
        
        player.sendMessage(resultMessage.toString());
        
        return SpellCastResult.SUCCESS;
    }
    
    private int removeNegativeEffects(Player caster) {
        int purifiedPlayers = 0;
        boolean casterPurified = purifyPlayer(caster);
        if (casterPurified) {
            purifiedPlayers++;
        }

        for (Player player : caster.getLevel().getPlayers().values()) {
            if (player.getId() == caster.getId()) {
                continue;
            }

            if (player.distance(caster) <= RADIUS) {
                boolean playerPurified = purifyPlayer(player);
                
                if (playerPurified) {
                    purifiedPlayers++;
                    player.sendMessage(TextFormat.GREEN + caster.getName() + " has cleansed you of negative effects!");
                    createPurifyEffect(player);
                }
            }
        }
        
        return purifiedPlayers;
    }
    
    private boolean purifyPlayer(Player player) {
        Collection<Effect> activeEffects = player.getEffects().values();
        List<Effect> effectsToRemove = new ArrayList<>();
        for (Effect effect : activeEffects) {
            for (int negativeEffectId : NEGATIVE_EFFECTS) {
                if (effect.getId() == negativeEffectId) {
                    effectsToRemove.add(effect);
                    break;
                }
            }
        }

        for (Effect effect : effectsToRemove) {
            player.removeEffect(effect.getId());
        }

        if (!effectsToRemove.isEmpty()) {
            createPurifyEffect(player);
            return true;
        }
        
        return false;
    }
    
    private int damageUndeadEntities(Player caster) {
        int damagedEntities = 0;
        
        for (Entity entity : caster.getLevel().getEntities()) {
            if (entity instanceof Player || !(entity instanceof EntityMob)) {
                continue;
            }
            
            if (entity.distance(caster) <= RADIUS) {
                if (isUndead(entity)) {
                    entity.attack(new EntityDamageEvent(
                            entity, 
                            DamageCause.MAGIC, 
                            UNDEAD_DAMAGE
                    ));
                    createUndeadDamageEffect(entity);
                    damagedEntities++;
                }
            }
        }
        
        return damagedEntities;
    }
    
    private boolean isUndead(Entity entity) {
        String entityType = entity.getClass().getSimpleName().toLowerCase();
        return entityType.contains("zombie") || 
               entityType.contains("skeleton") || 
               entityType.contains("wither") || 
               entityType.contains("phantom") || 
               entityType.contains("drowned") || 
               entityType.contains("husk") || 
               entityType.contains("stray");
    }
    
    private void createPurifyWave(Player player) {
        Vector3 center = player.add(0, 0.5, 0);
        for (int ring = 0; ring < 5; ring++) {
            double radius = ring * 2;
            int particles = (int) (16 * (1 + ring * 0.5));
            for (int i = 0; i < particles; i++) {
                double angle = 2 * Math.PI * i / particles;
                double x = center.x + Math.cos(angle) * radius;
                double y = center.y;
                double z = center.z + Math.sin(angle) * radius;
                Vector3 particlePos = new Vector3(x, y, z);
                player.getLevel().addParticle(new GenericParticle(
                    particlePos,
                    Particle.TYPE_REDSTONE
                ));
            }
        }
    }
    
    private void createPurifyEffect(Player player) {
        Vector3 position = player.add(0, 1.0, 0);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 12; j++) {
                double angle = j * (Math.PI / 6) + (i * Math.PI / 18);
                double height = j * 0.1;
                double radius = 0.8 - (j * 0.05);
                
                double x = position.x + Math.cos(angle) * radius;
                double y = position.y + height;
                double z = position.z + Math.sin(angle) * radius;
                
                Vector3 particlePos = new Vector3(x, y, z);
                player.getLevel().addParticle(new GenericParticle(
                    particlePos,
                    Particle.TYPE_VILLAGER_HAPPY
                ));
            }
        }
    }
    
    private void createUndeadDamageEffect(Entity entity) {
        Vector3 position = entity.add(0, entity.getHeight() / 2, 0);
        for (int i = 0; i < 15; i++) {
            double xOffset = (Math.random() - 0.5) * 1.5;
            double yOffset = (Math.random() - 0.5) * 1.5;
            double zOffset = (Math.random() - 0.5) * 1.5;
            
            Vector3 particlePos = position.add(xOffset, yOffset, zOffset);
            entity.getLevel().addParticle(new GenericParticle(
                particlePos,
                Particle.TYPE_ENCHANTMENT_TABLE
            ));
        }
    }
} 
