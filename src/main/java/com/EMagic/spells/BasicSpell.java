package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.FlameParticle;
import cn.nukkit.level.particle.LavaParticle;
import cn.nukkit.level.particle.WaterParticle;
import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.level.particle.EnchantParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

public abstract class BasicSpell implements Spell {
    
    protected String name;
    protected String description;
    protected String element;
    protected int manaCost;
    protected int requiredMasteryLevel;
    protected long cooldownTime;
    protected Sound castSound;
    
    public BasicSpell(String name, String description, String element, int manaCost, int requiredMasteryLevel, long cooldownTime, Sound castSound) {
        this.name = name;
        this.description = description;
        this.element = element;
        this.manaCost = manaCost;
        this.requiredMasteryLevel = requiredMasteryLevel;
        this.cooldownTime = cooldownTime;
        this.castSound = castSound;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getElement() {
        return element;
    }
    
    @Override
    public int getManaCost() {
        return manaCost;
    }
    
    @Override
    public int getRequiredMasteryLevel() {
        return requiredMasteryLevel;
    }
    
    @Override
    public long getCooldownTime() {
        return cooldownTime;
    }
    
    @Override
    public Sound getCastSound() {
        return castSound;
    }
    
    @Override
    public SpellCastResult cast(Player player, MagicPlayer magicPlayer) {
        int masteryLevel = magicPlayer.getElementMasteryLevel(element);
        if (masteryLevel < requiredMasteryLevel) {
            player.sendMessage(TextFormat.RED + "You need more mastery to cast this spell!");
            return SpellCastResult.INSUFFICIENT_MASTERY;
        }

        if (magicPlayer.hasCooldown(name)) {
            long remaining = magicPlayer.getRemainingCooldown(name) / 1000;
            player.sendMessage(TextFormat.RED + "This spell is on cooldown for " + remaining + " seconds!");
            return SpellCastResult.COOLDOWN;
        }

        if (magicPlayer.getMana() < manaCost) {
            player.sendMessage(TextFormat.RED + "You don't have enough mana to cast this spell!");
            return SpellCastResult.INSUFFICIENT_MANA;
        }

        magicPlayer.setCooldown(name, cooldownTime);
        magicPlayer.reduceMana(manaCost);

        player.getLevel().addSound(player, castSound);

        return executeSpell(player, magicPlayer);
    }
    
    /**
     * Creates a trail of particles in the direction of the spell based on the element type
     * 
     * @param player The player casting the spell
     * @param direction The direction vector of the spell
     */
    protected void createElementalParticleTrail(Player player, Vector3 direction) {
        for (double i = 1; i <= 15; i += 0.5) {
            double x = player.x + direction.x * i;
            double y = player.y + 1 + direction.y * i; 
            double z = player.z + direction.z * i;

            Particle particle = null;
            
            switch (element) {
                case Element.FIRE:
                    particle = i % 1 == 0 ? 
                        new LavaParticle(new Vector3(x, y, z)) : 
                        new FlameParticle(new Vector3(x, y, z));
                    break;
                case Element.WATER:
                    particle = i % 1 == 0 ? 
                        new WaterParticle(new Vector3(x, y, z)) : 
                        new BubbleParticle(new Vector3(x, y, z));
                    break;
                case Element.EARTH:
                    particle = new DustParticle(new Vector3(x, y, z), 139, 69, 19); // Brown color
                    break;
                case Element.AIR:
                    particle = new DustParticle(new Vector3(x, y, z), 255, 255, 255); // White color
                    break;
                case Element.LIGHT:
                    particle = new EnchantParticle(new Vector3(x, y, z));
                    break;
                case Element.DARK:
                    particle = new DustParticle(new Vector3(x, y, z), 75, 0, 130); // Dark purple color
                    break;
                default:
                    particle = new FlameParticle(new Vector3(x, y, z));
                    break;
            }

            if (particle != null) {
                player.getLevel().addParticle(particle);
            }
        }
    }
    
    /**
     * Executes the spell's specific effects
     * @param player The player casting the spell
     * @param magicPlayer The magic player data
     * @return The result of the spell cast
     */
    protected abstract SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer);
} 