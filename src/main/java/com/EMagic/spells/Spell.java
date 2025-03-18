package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.level.Sound;

import com.EMagic.player.MagicPlayer;

public interface Spell {
    
    /**
     * Gets the name of the spell
     * @return The spell's name
     */
    String getName();
    
    /**
     * Gets the description of the spell
     * @return The spell's description
     */
    String getDescription();
    
    /**
     * Gets the element this spell belongs to
     * @return The element name
     */
    String getElement();
    
    /**
     * Gets the mana cost of this spell
     * @return The mana cost
     */
    int getManaCost();
    
    /**
     * Gets the mastery level required to cast this spell
     * @return The required mastery level
     */
    int getRequiredMasteryLevel();
    
    /**
     * Gets the cooldown time of this spell in milliseconds
     * @return The cooldown time in milliseconds
     */
    long getCooldownTime();
    
    /**
     * Gets the sound played when casting this spell
     * @return The sound
     */
    Sound getCastSound();
    
    /**
     * Casts the spell for the given player
     * @param player The player casting the spell
     * @param magicPlayer The magic player data
     * @return The result of the spell cast
     */
    SpellCastResult cast(Player player, MagicPlayer magicPlayer);
} 