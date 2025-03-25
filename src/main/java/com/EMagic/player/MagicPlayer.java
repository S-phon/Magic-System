package com.EMagic.player;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MagicPlayer {
    
    private ElementalMagicSystem plugin;
    private UUID playerUUID;
    private String playerName;
    private Map<String, Integer> elementMasteryLevels;
    private Set<String> unlockedElements;
    private int mana;
    private int maxMana;
    private int manaRegenRate;
    private String activeElement;
    private Map<String, Long> spellCooldowns;
    private int elementalAffinity;
    private Map<String, Long> spellEffectEndTimes;
    private Map<String, Object> spellEffectData;
    private int manaLevel = 1;
    private int manaCrystals = 0;
    private String boundSpellElement = null;
    private String boundSpellName = null;
    
    public MagicPlayer(ElementalMagicSystem plugin, Player player) {
        this.plugin = plugin;
        this.playerUUID = player.getUniqueId();
        this.playerName = player.getName();
        this.elementMasteryLevels = new HashMap<>();
        this.unlockedElements = new HashSet<>();
        this.mana = 100;
        this.maxMana = 100;
        this.manaRegenRate = 5;
        this.activeElement = null;
        this.spellCooldowns = new HashMap<>();
        this.spellEffectEndTimes = new HashMap<>();
        this.spellEffectData = new HashMap<>();

        this.elementalAffinity = (int) (Math.random() * 6);
    }
    
    public MagicPlayer(ElementalMagicSystem plugin, UUID playerUUID, String playerName) {
        this.plugin = plugin;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.elementMasteryLevels = new HashMap<>();
        this.unlockedElements = new HashSet<>();
        this.mana = 100;
        this.maxMana = 100;
        this.manaRegenRate = 5;
        this.activeElement = null;
        this.spellCooldowns = new HashMap<>();
        this.spellEffectEndTimes = new HashMap<>();
        this.spellEffectData = new HashMap<>();
        
        this.elementalAffinity = (int) (Math.random() * 6);
    }
    
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public int getMana() {
        return mana;
    }
    
    public void setMana(int mana) {
        this.mana = Math.min(maxMana, Math.max(0, mana));
    }
    
    public void reduceMana(int amount) {
        this.mana = Math.max(0, mana - amount);
    }
    
    public void regenerateMana() {
        this.mana = Math.min(maxMana, mana + manaRegenRate);
    }
    
    /**
     * Adds mana to the player up to their maximum
     * @param amount Amount of mana to add
     */
    public void addMana(int amount) {
        this.mana = Math.min(maxMana, mana + amount);
    }
    
    public int getMaxMana() {
        return maxMana;
    }
    
    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        if (this.mana > maxMana) {
            this.mana = maxMana;
        }
    }
    
    public void increaseMaxMana(int amount) {
        this.maxMana += amount;
    }
    
    public int getManaRegenRate() {
        return manaRegenRate;
    }
    
    public void setManaRegenRate(int manaRegenRate) {
        this.manaRegenRate = manaRegenRate;
    }
    
    public void increaseManaRegenRate(int amount) {
        this.manaRegenRate += amount;
    }
    
    public String getActiveElement() {
        return activeElement;
    }
    
    public void setActiveElement(String activeElement) {
        if (hasUnlockedElement(activeElement)) {
            this.activeElement = activeElement;
        }
    }
    
    public boolean hasUnlockedElement(String elementName) {
        return unlockedElements.contains(elementName);
    }
    
    public void unlockElement(String elementName) {
        if (!unlockedElements.contains(elementName)) {
            unlockedElements.add(elementName);
            elementMasteryLevels.put(elementName, 0);
        }
    }
    
    public int getElementMasteryLevel(String elementName) {
        return elementMasteryLevels.getOrDefault(elementName, 0);
    }
    
    public void increaseElementMastery(String elementName, int amount) {
        if (hasUnlockedElement(elementName)) {
            int currentMastery = getElementMasteryLevel(elementName);
            int bonus = getAffinityBonus(elementName);
            elementMasteryLevels.put(elementName, Math.min(1000, currentMastery + amount + bonus));
        }
    }
    
    public int getAffinityBonus(String elementName) {
        if (elementName.equals(Element.FIRE) && elementalAffinity == 0) return 1;
        if (elementName.equals(Element.WATER) && elementalAffinity == 1) return 1;
        if (elementName.equals(Element.EARTH) && elementalAffinity == 2) return 1;
        if (elementName.equals(Element.AIR) && elementalAffinity == 3) return 1;
        if (elementName.equals(Element.LIGHT) && elementalAffinity == 4) return 1;
        if (elementName.equals(Element.DARK) && elementalAffinity == 5) return 1;
        return 0;
    }
    
    public boolean hasCooldown(String spellName) {
        if (!spellCooldowns.containsKey(spellName)) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownEnd = spellCooldowns.get(spellName);
        
        return currentTime < cooldownEnd;
    }
    
    public long getRemainingCooldown(String spellName) {
        if (!spellCooldowns.containsKey(spellName)) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownEnd = spellCooldowns.get(spellName);
        
        return Math.max(0, cooldownEnd - currentTime);
    }
    
    public void setCooldown(String spellName, long durationMs) {
        long currentTime = System.currentTimeMillis();
        spellCooldowns.put(spellName, currentTime + durationMs);
    }
    
    public Set<String> getUnlockedElements() {
        return new HashSet<>(unlockedElements);
    }
    
    public Map<String, Integer> getElementMasteryLevels() {
        return new HashMap<>(elementMasteryLevels);
    }
    
    /**
     * Sets the end time for a spell effect
     * @param effectName The name of the effect
     * @param endTime The time when the effect should end (in milliseconds)
     */
    public void setEffectEndTime(String effectName, long endTime) {
        spellEffectEndTimes.put(effectName, endTime);
    }
    
    /**
     * Checks if a spell effect is active
     * @param effectName The name of the effect
     * @return True if the effect is active, false otherwise
     */
    public boolean hasActiveEffect(String effectName) {
        if (!spellEffectEndTimes.containsKey(effectName)) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long endTime = spellEffectEndTimes.get(effectName);
        
        return currentTime < endTime;
    }
    
    /**
     * Gets the remaining time of a spell effect
     * @param effectName The name of the effect
     * @return The remaining time in milliseconds, or 0 if the effect is not active
     */
    public long getEffectRemainingTime(String effectName) {
        if (!spellEffectEndTimes.containsKey(effectName)) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long endTime = spellEffectEndTimes.get(effectName);
        
        return Math.max(0, endTime - currentTime);
    }
    
    /**
     * Removes a spell effect
     * @param effectName The name of the effect to remove
     */
    public void removeEffect(String effectName) {
        spellEffectEndTimes.remove(effectName);
    }
    
    /**
     * Stores additional data for a spell effect
     * @param key The data key
     * @param value The data value
     */
    public void setEffectData(String key, Object value) {
        spellEffectData.put(key, value);
    }
    
    /**
     * Gets additional data for a spell effect
     * @param key The data key
     * @return The data value, or null if not found
     */
    public Object getEffectData(String key) {
        return spellEffectData.get(key);
    }
    
    /**
     * Removes additional data for a spell effect
     * @param key The data key
     */
    public void removeEffectData(String key) {
        spellEffectData.remove(key);
    }
    
    /**
     * Gets the element of the bound spell
     * @return The element name, or null if no spell is bound
     */
    public String getBoundSpellElement() {
        return boundSpellElement;
    }
    
    /**
     * Gets the name of the bound spell
     * @return The spell name, or null if no spell is bound
     */
    public String getBoundSpellName() {
        return boundSpellName;
    }
    
    /**
     * Checks if a spell is bound to the player's stick
     * @return True if a spell is bound, false otherwise
     */
    public boolean hasSpellBound() {
        return boundSpellElement != null && boundSpellName != null;
    }
    
    /**
     * Binds a spell to the player's stick
     * @param elementName The element of the spell
     * @param spellName The name of the spell
     */
    public void setBoundSpell(String elementName, String spellName) {
        this.boundSpellElement = elementName;
        this.boundSpellName = spellName;
    }
    
    public void saveData() {
        Config config = plugin.getPlayerDataConfig();
        String uuid = playerUUID.toString();
        
        config.set(uuid + ".name", playerName);
        config.set(uuid + ".mana", mana);
        config.set(uuid + ".maxMana", maxMana);
        config.set(uuid + ".manaRegenRate", manaRegenRate);
        config.set(uuid + ".activeElement", activeElement);
        config.set(uuid + ".affinity", elementalAffinity);
        
        config.set(uuid + ".unlockedElements", unlockedElements.toArray(new String[0]));
        
        for (Map.Entry<String, Integer> entry : elementMasteryLevels.entrySet()) {
            config.set(uuid + ".mastery." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Long> entry : spellEffectEndTimes.entrySet()) {
            if (entry.getValue() > System.currentTimeMillis()) {
                config.set(uuid + ".effects." + entry.getKey(), entry.getValue());
            }
        }
        
        config.set(uuid + ".boundSpellElement", boundSpellElement);
        config.set(uuid + ".boundSpellName", boundSpellName);
        
        config.save();
    }
    
    public void loadData() {
        Config config = plugin.getPlayerDataConfig();
        String uuid = playerUUID.toString();
        
        if (!config.exists(uuid)) {
            unlockElement(Element.FIRE);
            return;
        }
        
        this.mana = config.getInt(uuid + ".mana", 100);
        this.maxMana = config.getInt(uuid + ".maxMana", 100);
        this.manaRegenRate = config.getInt(uuid + ".manaRegenRate", 5);
        this.activeElement = config.getString(uuid + ".activeElement", null);
        this.elementalAffinity = config.getInt(uuid + ".affinity", 0);
        
        if (config.exists(uuid + ".unlockedElements")) {
            Object elementsObj = config.get(uuid + ".unlockedElements");
            if (elementsObj instanceof List) {
                List<?> elementsList = (List<?>) elementsObj;
                for (Object element : elementsList) {
                    if (element instanceof String) {
                        unlockedElements.add((String) element);
                    }
                }
            } else if (elementsObj instanceof String[]) {
                String[] elements = (String[]) elementsObj;
                Collections.addAll(unlockedElements, elements);
            }
        }
        
        for (String element : unlockedElements) {
            int level = config.getInt(uuid + ".mastery." + element, 0);
            elementMasteryLevels.put(element, level);
        }

        long currentTime = System.currentTimeMillis();
        if (config.getSection(uuid + ".effects") != null) {
            for (String effectName : config.getSection(uuid + ".effects").getKeys(false)) {
                long endTime = config.getLong(uuid + ".effects." + effectName);
                if (endTime > currentTime) {
                    spellEffectEndTimes.put(effectName, endTime);
                }
            }
        }
        
        // Load bound spell (added in v1.2)
        if (config.exists(uuid + ".boundSpellElement")) {
            boundSpellElement = config.getString(uuid + ".boundSpellElement");
        }
        
        if (config.exists(uuid + ".boundSpellName")) {
            boundSpellName = config.getString(uuid + ".boundSpellName");
        }
    }
    
    public ElementalMagicSystem getPlugin() {
        return plugin;
    }
    
    /**
     * Gets the player's current mana level
     * @return The mana level
     */
    public int getManaLevel() {
        return manaLevel;
    }
    
    /**
     * Sets the player's mana level
     * @param level The new mana level
     */
    public void setManaLevel(int level) {
        this.manaLevel = Math.max(1, level);
        this.maxMana = 100 + (manaLevel - 1) * 25;
    }
    
    /**
     * Gets the player's current mana crystals
     * @return The number of mana crystals
     */
    public int getManaCrystals() {
        return manaCrystals;
    }
    
    /**
     * Adds mana crystals to the player
     * @param amount The amount to add
     */
    public void addManaCrystals(int amount) {
        this.manaCrystals += amount;
        checkManaLevelUp();
    }
    
    /**
     * Gets the number of mana crystals required for the next level
     * @return The required crystals
     */
    public int getRequiredManaCrystals() {
        return manaLevel * 10;
    }
    
    /**
     * Checks if the player has enough mana crystals to level up
     * and performs the level up if possible
     */
    private void checkManaLevelUp() {
        int required = getRequiredManaCrystals();
        if (manaCrystals >= required) {
            manaCrystals -= required;
            manaLevel++;
            maxMana = 100 + (manaLevel - 1) * 25;
            manaRegenRate = 5 + (manaLevel - 1) / 2;
        }
    }
} 
