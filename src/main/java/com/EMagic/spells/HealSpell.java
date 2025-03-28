package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

public class HealSpell extends BasicSpell {
    
    private static final int HEAL_AMOUNT = 10;
    private static final float HEAL_RADIUS = 5.0f;
    
    public HealSpell() {
        super(
            "heal",
            "Heals yourself or another player",
            Element.LIGHT,
            40, // Mana cost
            50, // Required mastery level
            10000, // Cooldown time
            Sound.RANDOM_ORB // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        healPlayer(player);
        createHealEffect(player);
        int healedAllies = 0;
        for (Player nearbyPlayer : player.getLevel().getPlayers().values()) {
            if (nearbyPlayer.getId() == player.getId()) {
                continue;
            }

            if (nearbyPlayer.distance(player) <= HEAL_RADIUS) {
                healPlayer(nearbyPlayer);
                createHealEffect(nearbyPlayer);
                healedAllies++;
                nearbyPlayer.sendMessage(TextFormat.GREEN + player.getName() + " has healed you!");
            }
        }

        if (healedAllies > 0) {
            player.sendMessage(TextFormat.GREEN + "You've healed yourself and " + healedAllies + " nearby allies!");
        } else {
            player.sendMessage(TextFormat.GREEN + "You've healed yourself!");
        }
        
        return SpellCastResult.SUCCESS;
    }
    
    private void healPlayer(Player player) {
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float newHealth = Math.min(currentHealth + HEAL_AMOUNT, maxHealth);
        player.setHealth(newHealth);
    }
    
    private void createHealEffect(Player player) {
        Vector3 position = player.add(0, 1.0, 0);
        for (int ring = 0; ring < 3; ring++) {
            double ringRadius = 0.5 + (ring * 0.3);
            
            for (int i = 0; i < 16; i++) {
                double angle = i * (Math.PI / 8);
                
                double x = position.x + Math.cos(angle) * ringRadius;
                double y = position.y + (ring * 0.2);
                double z = position.z + Math.sin(angle) * ringRadius;
                
                Vector3 particlePos = new Vector3(x, y, z);
                player.getLevel().addParticle(new GenericParticle(
                    particlePos,
                    Particle.TYPE_HEART
                ));
            }
        }
    }
} 
