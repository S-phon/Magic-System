package com.EMagic.elements;

import cn.nukkit.Player;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import java.util.Arrays;
import java.util.List;

public class Element {
    
    // Base elements
    public static final String FIRE = "fire";
    public static final String WATER = "water";
    public static final String EARTH = "earth";
    public static final String AIR = "air";
    public static final String LIGHT = "light";
    public static final String DARK = "dark";
    
    // Advanced elements (combinations)
    public static final String LIGHTNING = "lightning"; // AIR + FIRE
    public static final String ICE = "ice"; // WATER + AIR
    public static final String LAVA = "lava"; // FIRE + EARTH
    public static final String NATURE = "nature"; // EARTH + WATER
    public static final String ARCANE = "arcane"; // LIGHT + DARK
    
    // Divine elements (rare combinations)
    public static final String COSMIC = "cosmic"; // LIGHT + AIR + FIRE
    public static final String VOID = "void"; // DARK + WATER + EARTH
    
    private String name;
    private String displayName;
    private TextFormat color;
    private List<String> parentElements;
    private int particleType;
    private int masteryLevel; // 1-100
    private int tier; // 1-Basic, 2-Advanced, 3-Divine
    
    public Element(String name, String displayName, TextFormat color, int particleType, int tier, String... parentElements) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.particleType = particleType;
        this.parentElements = Arrays.asList(parentElements);
        this.masteryLevel = 0;
        this.tier = tier;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return color + displayName;
    }
    
    public TextFormat getColor() {
        return color;
    }
    
    public List<String> getParentElements() {
        return parentElements;
    }
    
    public int getParticleType() {
        return particleType;
    }
    
    public int getMasteryLevel() {
        return masteryLevel;
    }
    
    public void setMasteryLevel(int masteryLevel) {
        this.masteryLevel = Math.min(1000, Math.max(0, masteryLevel));
    }
    
    public void increaseMastery(int amount) {
        this.masteryLevel = Math.min(1000, masteryLevel + amount);
    }
    
    public int getTier() {
        return tier;
    }
    
    public void spawnParticles(Player player, Vector3 position, int count) {
        for (int i = 0; i < count; i++) {
            double offsetX = Math.random() * 2 - 1;
            double offsetY = Math.random() * 2 - 1;
            double offsetZ = Math.random() * 2 - 1;
            
            Vector3 pos = new Vector3(
                position.x + offsetX,
                position.y + offsetY,
                position.z + offsetZ
            );
            
            Particle particle = new GenericParticle(pos, particleType);
            player.getLevel().addParticle(particle);
        }
    }
    
    public boolean isCombinationOf(Element... elements) {
        for (Element element : elements) {
            if (!parentElements.contains(element.getName())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Element)) return false;
        
        Element element = (Element) obj;
        return name.equals(element.name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
} 
