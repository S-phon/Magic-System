package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.List;
import java.util.ArrayList;

public class FlashSpell extends BasicSpell {
    
    private static final int RADIUS = 8;
    private static final int EFFECT_DURATION = 100;
    private static final int BLINDNESS_AMPLIFIER = 1;
    
    public FlashSpell() {
        super(
            "flash",
            "Creates a bright flash that blinds entities",
            Element.LIGHT,
            25, // Mana cost
            25, // Required mastery level
            5000, // Cooldown time
            Sound.RANDOM_EXPLODE // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        createFlashEffect(player);
        List<EntityLiving> affectedEntities = new ArrayList<>();
        
        for (Entity entity : player.getLevel().getEntities()) {
            if (entity.getId() == player.getId()) {
                continue;
            }

            if (!(entity instanceof EntityLiving)) {
                continue;
            }

            if (entity.distance(player) <= RADIUS) {
                EntityLiving living = (EntityLiving) entity;
                Effect blindness = Effect.getEffect(Effect.BLINDNESS)
                        .setAmplifier(BLINDNESS_AMPLIFIER)
                        .setDuration(EFFECT_DURATION);
                living.addEffect(blindness);
                affectedEntities.add(living);
                createBlindedEffect(living);
            }
        }

        if (!affectedEntities.isEmpty()) {
            int count = affectedEntities.size();
            player.sendMessage(TextFormat.YELLOW + "You created a bright flash, blinding " + 
                    count + (count == 1 ? " entity!" : " entities!"));
        } else {
            player.sendMessage(TextFormat.YELLOW + "You created a bright flash, but no entities were affected!");
        }
        
        return SpellCastResult.SUCCESS;
    }
    
    private void createFlashEffect(Player player) {
        Vector3 center = player.add(0, 1, 0);
        for (int i = 0; i < 200; i++) {
            double phi = Math.random() * Math.PI * 2;
            double costheta = Math.random() * 2 - 1;
            double theta = Math.acos(costheta);
            double x = Math.sin(theta) * Math.cos(phi);
            double y = Math.sin(theta) * Math.sin(phi);
            double z = Math.cos(theta);
            double distance = Math.random() * RADIUS;
            Vector3 position = center.add(x * distance, y * distance, z * distance);
            
            player.getLevel().addParticle(new GenericParticle(
                position,
                Particle.TYPE_REDSTONE 
            ));
        }

        for (int i = 0; i < 50; i++) {
            double offset = Math.random() * 0.5;
            Vector3 position = center.add(
                (Math.random() - 0.5) * offset,
                (Math.random() - 0.5) * offset,
                (Math.random() - 0.5) * offset
            );
            
            player.getLevel().addParticle(new GenericParticle(
                position,
                Particle.TYPE_END_ROD
            ));
        }

        player.getLevel().addSound(center, Sound.RANDOM_EXPLODE);
        player.getLevel().addSound(center, Sound.RANDOM_FIZZ);
    }
    
    private void createBlindedEffect(EntityLiving entity) {
        Vector3 position = entity.add(0, entity.getHeight() * 0.75, 0);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 8; j++) {
                double angle = j * Math.PI / 4 + (i * Math.PI / 8);
                double radius = 0.4;
                
                double x = position.x + Math.cos(angle) * radius;
                double y = position.y + (j % 2) * 0.1;
                double z = position.z + Math.sin(angle) * radius;
                
                Vector3 particlePos = new Vector3(x, y, z);
                entity.getLevel().addParticle(new GenericParticle(
                    particlePos,
                    Particle.TYPE_SMOKE
                ));
            }
        }
    }
}
