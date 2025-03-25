package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellManager {
    
    private ElementalMagicSystem plugin;
    private Map<String, List<Spell>> elementSpells;
    
    public SpellManager(ElementalMagicSystem plugin) {
        this.plugin = plugin;
        this.elementSpells = new HashMap<>();
        registerSpells();
    }
    
    private void registerSpells() {
        List<Spell> fireSpells = new ArrayList<>();
        fireSpells.add(new FireballSpell());
        fireSpells.add(new IgniteSpell());
        fireSpells.add(new FlamethrowerSpell());
        fireSpells.add(new MeteorSpell());
        elementSpells.put(Element.FIRE, fireSpells);
        
        // Water spells
        List<Spell> waterSpells = new ArrayList<>();
        waterSpells.add(new WaterBoltSpell());
        waterSpells.add(new WaterBreathingSpell());
        elementSpells.put(Element.WATER, waterSpells);
        
        // Earth spells
        List<Spell> earthSpells = new ArrayList<>();
        earthSpells.add(new StoneShieldSpell());
        earthSpells.add(new RockThrowSpell());
        elementSpells.put(Element.EARTH, earthSpells);
        
        // Air spells
        List<Spell> airSpells = new ArrayList<>();
        airSpells.add(new FeatherFallSpell());
        airSpells.add(new GustSpell());
        elementSpells.put(Element.AIR, airSpells);
        
        // Light spells
        List<Spell> lightSpells = new ArrayList<>();
        lightSpells.add(new FlashSpell());
        lightSpells.add(new IlluminateSpell());
        lightSpells.add(new HealSpell());
        lightSpells.add(new PurifySpell());
        elementSpells.put(Element.LIGHT, lightSpells);
        
        // Dark spells
        List<Spell> darkSpells = new ArrayList<>();
        darkSpells.add(new ShadowBoltSpell());
        darkSpells.add(new LifeDrainSpell());
        elementSpells.put(Element.DARK, darkSpells);
        
        // Advanced element spells coming soon
    }
    
    /**
     * Gets all spells for a specific element
     * @param elementName The element name
     * @return List of spells for that element
     */
    public List<Spell> getSpellsForElement(String elementName) {
        return elementSpells.getOrDefault(elementName, new ArrayList<>());
    }
    
    /**
     * Gets all spells from all elements
     * @return List of all spells
     */
    public List<Spell> getAllSpells() {
        List<Spell> allSpells = new ArrayList<>();
        for (List<Spell> elementSpellList : elementSpells.values()) {
            allSpells.addAll(elementSpellList);
        }
        return allSpells;
    }
    
    /**
     * Gets a specific spell by name and element
     * @param elementName The element name
     * @param spellName The spell name
     * @return The spell, or null if not found
     */
    public Spell getSpell(String elementName, String spellName) {
        List<Spell> spells = getSpellsForElement(elementName);
        for (Spell spell : spells) {
            if (spell.getName().equalsIgnoreCase(spellName)) {
                return spell;
            }
        }
        return null;
    }
    
    /**
     * Casts a specific spell for a player
     * @param player The player casting the spell
     * @param elementName The element of the spell
     * @param spellName The name of the spell
     * @return The result of the spell cast
     */
    public SpellCastResult castSpell(Player player, String elementName, String spellName) {
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        if (!magicPlayer.hasUnlockedElement(elementName)) {
            player.sendMessage(TextFormat.RED + "You haven't unlocked this element yet!");
            return SpellCastResult.FAILED;
        }
        
        Spell spell = getSpell(elementName, spellName);
        if (spell == null) {
            player.sendMessage(TextFormat.RED + "That spell doesn't exist!");
            return SpellCastResult.FAILED;
        }
        
        return spell.cast(player, magicPlayer);
    }
    
    /**
     * Casts a basic spell for the player's active element
     * @param player The player casting the spell
     * @param elementName The element to cast
     * @return The result of the spell cast
     */
    public SpellCastResult castBasicSpell(Player player, String elementName) {
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        if (!magicPlayer.hasUnlockedElement(elementName)) {
            player.sendMessage(TextFormat.RED + "You haven't unlocked this element yet!");
            return SpellCastResult.FAILED;
        }
        
        List<Spell> spells = getSpellsForElement(elementName);
        if (spells.isEmpty()) {
            player.sendMessage(TextFormat.RED + "There are no spells for this element yet!");
            return SpellCastResult.FAILED;
        }

        List<Spell> availableSpells = new ArrayList<>();
        for (Spell spell : spells) {
            if (magicPlayer.getElementMasteryLevel(elementName) >= spell.getRequiredMasteryLevel() &&
                !magicPlayer.hasCooldown(spell.getName()) &&
                magicPlayer.getMana() >= spell.getManaCost()) {
                availableSpells.add(spell);
            }
        }
        
        if (availableSpells.isEmpty()) {
            player.sendMessage(TextFormat.RED + "You can't cast any spells with this element right now!");
            return SpellCastResult.FAILED;
        }

        Spell bestSpell = availableSpells.get(0);
        for (Spell spell : availableSpells) {
            if (spell.getRequiredMasteryLevel() > bestSpell.getRequiredMasteryLevel()) {
                bestSpell = spell;
            }
        }

        if (bestSpell instanceof BasicSpell) {
            Vector3 direction = player.getDirectionVector();
            ((BasicSpell) bestSpell).createElementalParticleTrail(player, direction);
        }
        
        return bestSpell.cast(player, magicPlayer);
    }
} 
