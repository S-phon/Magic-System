package com.EMagic.elements;

import cn.nukkit.level.particle.DustParticle;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ElementManager {

    private ElementalMagicSystem plugin;
    private Map<String, Element> elements;
    private Map<String, List<SpellEffect>> elementEffects;
    
    // Define particle types for each element
    private static final int PARTICLE_FIRE = 2; // Flame particle
    private static final int PARTICLE_WATER = 8; // Water drip particle
    private static final int PARTICLE_EARTH = 23; // Block break particle
    private static final int PARTICLE_AIR = 30; // White smoke particle
    private static final int PARTICLE_LIGHT = 14; // Redstone particle
    private static final int PARTICLE_DARK = 16; // Portal particle
    
    public ElementManager(ElementalMagicSystem plugin) {
        this.plugin = plugin;
        this.elements = new HashMap<>();
        this.elementEffects = new HashMap<>();
        initializeElements();
        initializeElementEffects();
    }
    
    private void initializeElements() {
        // Basic Elements
        registerElement(new Element(Element.FIRE, "Fire", TextFormat.RED, PARTICLE_FIRE, 1));
        registerElement(new Element(Element.WATER, "Water", TextFormat.BLUE, PARTICLE_WATER, 1));
        registerElement(new Element(Element.EARTH, "Earth", TextFormat.GREEN, PARTICLE_EARTH, 1));
        registerElement(new Element(Element.AIR, "Air", TextFormat.WHITE, PARTICLE_AIR, 1));
        registerElement(new Element(Element.LIGHT, "Light", TextFormat.YELLOW, PARTICLE_LIGHT, 1));
        registerElement(new Element(Element.DARK, "Dark", TextFormat.DARK_PURPLE, PARTICLE_DARK, 1));
        
        // Advanced Elements
        registerElement(new Element(Element.LIGHTNING, "Lightning", TextFormat.AQUA, PARTICLE_AIR, 2, 
                Element.AIR, Element.FIRE));
        registerElement(new Element(Element.ICE, "Ice", TextFormat.AQUA, PARTICLE_WATER, 2, 
                Element.WATER, Element.AIR));
        registerElement(new Element(Element.LAVA, "Lava", TextFormat.GOLD, PARTICLE_FIRE, 2, 
                Element.FIRE, Element.EARTH));
        registerElement(new Element(Element.NATURE, "Nature", TextFormat.DARK_GREEN, PARTICLE_EARTH, 2, 
                Element.EARTH, Element.WATER));
        registerElement(new Element(Element.ARCANE, "Arcane", TextFormat.LIGHT_PURPLE, PARTICLE_LIGHT, 2, 
                Element.LIGHT, Element.DARK));
        
        // Divine Elements 
        registerElement(new Element(Element.COSMIC, "Cosmic", TextFormat.GOLD, PARTICLE_LIGHT, 3, 
                Element.LIGHT, Element.AIR, Element.FIRE));
        registerElement(new Element(Element.VOID, "Void", TextFormat.BLACK, PARTICLE_DARK, 3, 
                Element.DARK, Element.WATER, Element.EARTH));
    }
    
    private void initializeElementEffects() {
        // Fire element effects
        List<SpellEffect> fireEffects = new ArrayList<>();
        fireEffects.add(new SpellEffect("ignite", "Sets target on fire", 1));
        fireEffects.add(new SpellEffect("fireball", "Launches a fireball", 25));
        fireEffects.add(new SpellEffect("heatwave", "Creates a wave of heat damaging all entities in range", 50));
        fireEffects.add(new SpellEffect("meteorstorm", "Summons a storm of meteors", 75));
        elementEffects.put(Element.FIRE, fireEffects);
        
        // Water element effects
        List<SpellEffect> waterEffects = new ArrayList<>();
        waterEffects.add(new SpellEffect("waterbreath", "Allows breathing underwater", 1));
        waterEffects.add(new SpellEffect("waterbolt", "Launches a bolt of water", 25));
        waterEffects.add(new SpellEffect("tsunami", "Creates a wave that pushes entities", 50));
        waterEffects.add(new SpellEffect("maelstrom", "Creates a whirlpool that pulls entities", 75));
        elementEffects.put(Element.WATER, waterEffects);
        
        // Earth element effects
        List<SpellEffect> earthEffects = new ArrayList<>();
        earthEffects.add(new SpellEffect("stoneshield", "Creates a shield of stone", 1));
        earthEffects.add(new SpellEffect("rockshard", "Launches sharp rock shards", 25));
        earthEffects.add(new SpellEffect("earthquake", "Creates an earthquake that damages entities", 50));
        earthEffects.add(new SpellEffect("terraform", "Reshapes the terrain in a large area", 75));
        elementEffects.put(Element.EARTH, earthEffects);
        
        // Air element effects
        List<SpellEffect> airEffects = new ArrayList<>();
        airEffects.add(new SpellEffect("featherfall", "Reduces fall damage", 1));
        airEffects.add(new SpellEffect("gust", "Creates a gust of wind that pushes entities", 25));
        airEffects.add(new SpellEffect("cyclone", "Creates a cyclone that lifts entities", 50));
        airEffects.add(new SpellEffect("tornado", "Creates a tornado that damages and throws entities", 75));
        elementEffects.put(Element.AIR, airEffects);
        
        // Light element effects
        List<SpellEffect> lightEffects = new ArrayList<>();
        lightEffects.add(new SpellEffect("illuminate", "Creates light in the area", 1));
        lightEffects.add(new SpellEffect("flashbang", "Blinds nearby entities", 25));
        lightEffects.add(new SpellEffect("heal", "Heals the caster or target", 50));
        lightEffects.add(new SpellEffect("purify", "Removes negative effects and damages undead", 75));
        elementEffects.put(Element.LIGHT, lightEffects);
        
        // Dark element effects
        List<SpellEffect> darkEffects = new ArrayList<>();
        darkEffects.add(new SpellEffect("nightvision", "Allows seeing in the dark", 1));
        darkEffects.add(new SpellEffect("shadowbolt", "Launches a bolt of shadow energy", 25));
        darkEffects.add(new SpellEffect("lifedrain", "Drains health from the target", 50));
        darkEffects.add(new SpellEffect("corruption", "Corrupts the area, damaging entities over time", 75));
        elementEffects.put(Element.DARK, darkEffects);
        
        // Advanced elements
        List<SpellEffect> lightningEffects = new ArrayList<>();
        lightningEffects.add(new SpellEffect("shock", "Shocks the target, dealing damage", 1));
        lightningEffects.add(new SpellEffect("thunderbolt", "Summons a bolt of lightning", 50));
        elementEffects.put(Element.LIGHTNING, lightningEffects);
        
        List<SpellEffect> iceEffects = new ArrayList<>();
        iceEffects.add(new SpellEffect("freeze", "Freezes water or slows entities", 1));
        iceEffects.add(new SpellEffect("blizzard", "Creates a damaging ice storm in an area", 50));
        elementEffects.put(Element.ICE, iceEffects);
    }
    
    public void registerElement(Element element) {
        elements.put(element.getName(), element);
    }
    
    public Element getElement(String name) {
        return elements.get(name);
    }
    
    public boolean hasElement(String name) {
        return elements.containsKey(name);
    }
    
    public List<Element> getAllElements() {
        return new ArrayList<>(elements.values());
    }
    
    public List<Element> getElementsByTier(int tier) {
        List<Element> result = new ArrayList<>();
        for (Element element : elements.values()) {
            if (element.getTier() == tier) {
                result.add(element);
            }
        }
        return result;
    }
    
    public List<SpellEffect> getElementEffects(String elementName) {
        return elementEffects.getOrDefault(elementName, new ArrayList<>());
    }
    
    public SpellEffect getElementEffect(String elementName, String effectName) {
        List<SpellEffect> effects = getElementEffects(elementName);
        for (SpellEffect effect : effects) {
            if (effect.getName().equalsIgnoreCase(effectName)) {
                return effect;
            }
        }
        return null;
    }
    
    public Element combineElements(Element... elements) {
        List<String> elementNames = new ArrayList<>();
        for (Element element : elements) {
            elementNames.add(element.getName());
        }
        
        // Air + Fire = Lightning
        if (containsAll(elementNames, Element.AIR, Element.FIRE) && elementNames.size() == 2) {
            return getElement(Element.LIGHTNING);
        }
        // Water + Air = Ice
        if (containsAll(elementNames, Element.WATER, Element.AIR) && elementNames.size() == 2) {
            return getElement(Element.ICE);
        }
        // Fire + Earth = Lava
        if (containsAll(elementNames, Element.FIRE, Element.EARTH) && elementNames.size() == 2) {
            return getElement(Element.LAVA);
        }
        // Earth + Water = Nature
        if (containsAll(elementNames, Element.EARTH, Element.WATER) && elementNames.size() == 2) {
            return getElement(Element.NATURE);
        }
        // Light + Dark = Arcane
        if (containsAll(elementNames, Element.LIGHT, Element.DARK) && elementNames.size() == 2) {
            return getElement(Element.ARCANE);
        }
        // Light + Air + Fire = Cosmic
        if (containsAll(elementNames, Element.LIGHT, Element.AIR, Element.FIRE) && elementNames.size() == 3) {
            return getElement(Element.COSMIC);
        }
        // Dark + Water + Earth = Void
        if (containsAll(elementNames, Element.DARK, Element.WATER, Element.EARTH) && elementNames.size() == 3) {
            return getElement(Element.VOID);
        }
        
        return null;
    }
    
    private boolean containsAll(List<String> list, String... elements) {
        for (String element : elements) {
            if (!list.contains(element)) {
                return false;
            }
        }
        return true;
    }
    
    public class SpellEffect {
        private String name;
        private String description;
        private int requiredMasteryLevel;
        
        public SpellEffect(String name, String description, int requiredMasteryLevel) {
            this.name = name;
            this.description = description;
            this.requiredMasteryLevel = requiredMasteryLevel;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getRequiredMasteryLevel() {
            return requiredMasteryLevel;
        }
    }
} 