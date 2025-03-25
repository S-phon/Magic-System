package com.EMagic.forms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.element.ElementDropdown;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementLabel;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.element.ElementStepSlider;
import cn.nukkit.form.element.ElementToggle;
import cn.nukkit.form.window.FormWindow;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;
import com.EMagic.spells.Spell;
import com.EMagic.spells.SpellCastResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles all form-based interactions for the magic system
 */
public class MagicForms {
    
    private final ElementalMagicSystem plugin;
    
    // Form IDs for handling responses
    public static final int FORM_MAIN_MENU = 1000;
    public static final int FORM_ELEMENT_SELECT = 1001;
    public static final int FORM_ELEMENT_LEARN = 1002;
    public static final int FORM_SPELL_CAST = 1003;
    public static final int FORM_ELEMENT_COMBINE = 1004;
    public static final int FORM_ELEMENTS_LIST = 1005;
    public static final int FORM_SPELLS_LIST = 1006;
    public static final int FORM_PLAYER_INFO = 1007;
    public static final int FORM_SPELL_BINDING = 1008;
    
    public MagicForms(ElementalMagicSystem plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Shows the main magic menu to the player
     * @param player The player to show the menu to
     */
    public void showMainMenu(Player player) {
        FormWindowSimple form = new FormWindowSimple(
            "§b✧ Elemental Magic System ✧",
            "Welcome to the Elemental Magic System! Select an option below:"
        );
        
        form.addButton(new ElementButton("Select Active Element", 
            new ElementButtonImageData("path", "textures/items/blaze_rod")));
        form.addButton(new ElementButton("Learn New Element", 
            new ElementButtonImageData("path", "textures/items/book_enchanted")));
        form.addButton(new ElementButton("Cast Spell", 
            new ElementButtonImageData("path", "textures/items/fireball")));
        form.addButton(new ElementButton("Combine Elements", 
            new ElementButtonImageData("path", "textures/items/brewing_stand")));
        form.addButton(new ElementButton("View Elements", 
            new ElementButtonImageData("path", "textures/items/book_normal")));
        form.addButton(new ElementButton("View Spells", 
            new ElementButtonImageData("path", "textures/items/book_writable")));
        form.addButton(new ElementButton("My Magic Info", 
            new ElementButtonImageData("path", "textures/items/name_tag")));
        
        player.showFormWindow(form, FORM_MAIN_MENU);
    }
    
    /**
     * Shows the element selection form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showElementSelect(Player player, MagicPlayer magicPlayer) {
        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        
        if (unlockedElements.isEmpty()) {
            FormWindowModal errorForm = new FormWindowModal(
                "No Elements",
                "You haven't unlocked any elements yet. Learn an element first!",
                "Learn Element",
                "Back to Menu"
            );
            
            player.showFormWindow(errorForm, FORM_ELEMENT_SELECT);
            return;
        }
        
        FormWindowSimple form = new FormWindowSimple(
            "Select Active Element",
            "Choose which element you want to activate:"
        );
        
        String currentElement = magicPlayer.getActiveElement();
        
        for (String elementName : unlockedElements) {
            Element element = plugin.getElementManager().getElement(elementName);
            String displayName = element.getDisplayName();
            if (elementName.equals(currentElement)) {
                displayName = "★ " + displayName + " ★";
            }

            form.addButton(new ElementButton(displayName));
        }
        
        player.showFormWindow(form, FORM_ELEMENT_SELECT);
    }
    
    /**
     * Shows the learn element form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showLearnElement(Player player, MagicPlayer magicPlayer) {
        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        List<Element> basicElements = plugin.getElementManager().getElementsByTier(1);
        
        FormWindowSimple form = new FormWindowSimple(
            "Learn Element",
            "Select a basic element to learn:"
        );
        
        boolean hasAvailableElements = false;
        
        for (Element element : basicElements) {
            if (unlockedElements.contains(element.getName())) {
                continue;
            }
            
            hasAvailableElements = true;
            form.addButton(new ElementButton(element.getDisplayName()));
        }
        
        if (!hasAvailableElements) {
            FormWindowModal learnedAllForm = new FormWindowModal(
                "All Elements Learned",
                "You have already learned all the basic elements! Try combining them to discover advanced elements.",
                "Combine Elements",
                "Back to Menu"
            );
            
            player.showFormWindow(learnedAllForm, FORM_ELEMENT_LEARN);
            return;
        }
        
        player.showFormWindow(form, FORM_ELEMENT_LEARN);
    }
    
    /**
     * Shows the spell casting form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showSpellCast(Player player, MagicPlayer magicPlayer) {
        String activeElement = magicPlayer.getActiveElement();
        
        if (activeElement == null || activeElement.isEmpty()) {
            FormWindowModal errorForm = new FormWindowModal(
                "No Active Element",
                "You need to select an active element before casting spells!",
                "Select Element",
                "Back to Menu"
            );
            
            player.showFormWindow(errorForm, FORM_SPELL_CAST);
            return;
        }
        
        Element element = plugin.getElementManager().getElement(activeElement);
        List<Spell> elementSpells = plugin.getSpellManager().getSpellsForElement(activeElement);
        
        if (elementSpells.isEmpty()) {
            FormWindowModal noSpellsForm = new FormWindowModal(
                "No Spells",
                "There are no spells available for " + element.getDisplayName() + "!",
                "Select Different Element",
                "Back to Menu"
            );
            
            player.showFormWindow(noSpellsForm, FORM_SPELL_CAST);
            return;
        }
        
        FormWindowSimple form = new FormWindowSimple(
            "Cast Spell - " + element.getDisplayName(),
            "Select a spell to cast. Your mana: " + magicPlayer.getMana() + "/" + magicPlayer.getMaxMana()
        );
        
        for (Spell spell : elementSpells) {
            // Show spell info in the button
            String buttonText = spell.getName() + " (Mana: " + spell.getManaCost() + 
                                ", Mastery: " + spell.getRequiredMasteryLevel() + ")";
            
            int masteryLevel = magicPlayer.getElementMasteryLevel(activeElement);
            
            // Gray out spells that require higher mastery
            if (masteryLevel < spell.getRequiredMasteryLevel()) {
                buttonText = "§8" + buttonText + " §8[Locked]";
            }
            
            form.addButton(new ElementButton(buttonText));
        }
        
        player.showFormWindow(form, FORM_SPELL_CAST);
    }
    
    /**
     * Shows the element combination form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showElementCombine(Player player, MagicPlayer magicPlayer) {
        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        
        if (unlockedElements.size() < 2) {
            FormWindowModal errorForm = new FormWindowModal(
                "Cannot Combine",
                "You need at least two elements to attempt a combination!",
                "Learn More Elements",
                "Back to Menu"
            );
            
            player.showFormWindow(errorForm, FORM_ELEMENT_COMBINE);
            return;
        }
        
        FormWindowCustom form = new FormWindowCustom("Combine Elements");
        
        // Convert set to list for easier access
        List<String> elementNames = new ArrayList<>(unlockedElements);
        List<String> elementDisplayNames = new ArrayList<>();
        
        for (String element : elementNames) {
            elementDisplayNames.add(plugin.getElementManager().getElement(element).getDisplayName());
        }
        
        form.addElement(new ElementLabel("Select two elements to combine. Both elements must have at least 750 mastery."));
        form.addElement(new ElementDropdown("First Element", elementDisplayNames));
        form.addElement(new ElementDropdown("Second Element", elementDisplayNames));
        
        player.showFormWindow(form, FORM_ELEMENT_COMBINE);
    }
    
    /**
     * Shows the elements list form to the player
     * @param player The player to show the form to
     */
    public void showElementsList(Player player) {
        FormWindowSimple form = new FormWindowSimple(
            "Elements List",
            "Here are all the elements in the magic system:"
        );
        
        // Group elements by tier
        for (int tier = 1; tier <= 3; tier++) {
            List<Element> tierElements = plugin.getElementManager().getElementsByTier(tier);
            String tierName = getTierName(tier);
            
            for (Element element : tierElements) {
                String buttonText = tierName + ": " + element.getDisplayName();
                form.addButton(new ElementButton(buttonText));
            }
        }
        
        player.showFormWindow(form, FORM_ELEMENTS_LIST);
    }
    
    /**
     * Shows the spells list form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showSpellsList(Player player, MagicPlayer magicPlayer) {
        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        
        FormWindowSimple form = new FormWindowSimple(
            "Spells List",
            "Select an element to see its spells:"
        );
        
        // Add an "All Spells" option
        form.addButton(new ElementButton("All Spells"));
        
        // Add buttons for each element the player has unlocked
        for (String elementName : unlockedElements) {
            Element element = plugin.getElementManager().getElement(elementName);
            form.addButton(new ElementButton(element.getDisplayName() + " Spells"));
        }
        
        player.showFormWindow(form, FORM_SPELLS_LIST);
    }
    
    /**
     * Shows the player info form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showPlayerInfo(Player player, MagicPlayer magicPlayer) {
        FormWindowCustom form = new FormWindowCustom("Magic Profile: " + player.getName());
        
        // Add active element
        String activeElement = magicPlayer.getActiveElement();
        String activeElementDisplay = activeElement != null && !activeElement.isEmpty() ? 
                plugin.getElementManager().getElement(activeElement).getDisplayName() : "None";
        
        form.addElement(new ElementLabel("§lActive Element: §r" + activeElementDisplay));
        
        // Add mana info
        form.addElement(new ElementLabel("§lMana: §r" + magicPlayer.getMana() + "/" + magicPlayer.getMaxMana()));
        form.addElement(new ElementLabel("§lMana Level: §r" + magicPlayer.getManaLevel()));
        form.addElement(new ElementLabel("§lMana Crystals: §r" + magicPlayer.getManaCrystals() + "/" + 
                                        magicPlayer.getRequiredManaCrystals()));
        form.addElement(new ElementLabel("§lMana Regen Rate: §r" + magicPlayer.getManaRegenRate() + " per tick"));
        
        form.addElement(new ElementLabel("\n§l§6Unlocked Elements and Mastery Levels:"));
        
        // List all unlocked elements and their mastery
        Map<String, Integer> masteryLevels = magicPlayer.getElementMasteryLevels();
        
        for (String elementName : magicPlayer.getUnlockedElements()) {
            Element element = plugin.getElementManager().getElement(elementName);
            int mastery = masteryLevels.getOrDefault(elementName, 0);
            form.addElement(new ElementLabel(element.getDisplayName() + ": " + mastery + "/1000"));
        }
        
        player.showFormWindow(form, FORM_PLAYER_INFO);
    }
    
    /**
     * Show a spell details form for a specific spell
     * @param player The player to show the form to
     * @param spell The spell to show details for
     */
    public void showSpellDetails(Player player, Spell spell) {
        FormWindowModal form = new FormWindowModal(
            spell.getName(),
            "§lElement: §r" + plugin.getElementManager().getElement(spell.getElement()).getDisplayName() + "\n" +
            "§lDescription: §r" + spell.getDescription() + "\n" +
            "§lMana Cost: §r" + spell.getManaCost() + "\n" +
            "§lRequired Mastery: §r" + spell.getRequiredMasteryLevel() + "\n" +
            "§lCooldown: §r" + (spell.getCooldownTime() / 1000) + " seconds",
            "Cast Spell",
            "Back"
        );
        
        player.showFormWindow(form);
    }
    
    /**
     * Show element details for a specific element
     * @param player The player to show the form to
     * @param element The element to show details for
     */
    public void showElementDetails(Player player, Element element) {
        StringBuilder content = new StringBuilder();
        content.append("§lTier: §r").append(getTierName(element.getTier())).append("\n");
        content.append("§lDescription: §r").append(element.getDescription()).append("\n");
        
        if (element.getTier() > 1) {
            content.append("\n§lCreated by combining: §r");
            List<String> parents = element.getParentElements();
            for (int i = 0; i < parents.size(); i++) {
                String parentName = parents.get(i);
                Element parent = plugin.getElementManager().getElement(parentName);
                content.append(parent.getDisplayName());
                
                if (i < parents.size() - 1) {
                    content.append(" + ");
                }
            }
        }
        
        FormWindowModal form = new FormWindowModal(
            element.getDisplayName(),
            content.toString(),
            "OK",
            "Back"
        );
        
        player.showFormWindow(form);
    }
    
    /**
     * Shows a list of spells for a specific element
     * @param player The player to show the form to
     * @param elementName The element name or "all" for all spells
     */
    public void showElementSpells(Player player, String elementName) {
        List<Spell> spells;
        String title;
        
        if (elementName.equalsIgnoreCase("all")) {
            spells = plugin.getSpellManager().getAllSpells();
            title = "All Spells";
        } else {
            spells = plugin.getSpellManager().getSpellsForElement(elementName);
            Element element = plugin.getElementManager().getElement(elementName);
            title = element.getDisplayName() + " Spells";
        }
        
        FormWindowSimple form = new FormWindowSimple(
            title,
            "Select a spell to view its details:"
        );
        
        if (spells.isEmpty()) {
            form.setContent("No spells available for this element.");
        } else {
            for (Spell spell : spells) {
                form.addButton(new ElementButton(spell.getName()));
            }
        }
        
        player.showFormWindow(form);
    }
    
    /**
     * Shows the spell binding form to the player
     * @param player The player to show the form to
     * @param magicPlayer The player's magic data
     */
    public void showSpellBindingForm(Player player, MagicPlayer magicPlayer) {
        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        
        if (unlockedElements.isEmpty()) {
            FormWindowModal errorForm = new FormWindowModal(
                "No Elements",
                "You haven't unlocked any elements yet. Learn an element first!",
                "Learn Element",
                "Back to Menu"
            );
            
            player.showFormWindow(errorForm, FORM_SPELL_BINDING);
            return;
        }
        
        FormWindowSimple form = new FormWindowSimple(
            "Bind Spell to Stick",
            "Choose a spell to bind to your stick. When you right-click with the stick, this spell will be cast instead of your basic spell."
        );
        
        // Show all spells from all elements that the player has enough mastery for
        for (String elementName : unlockedElements) {
            Element element = plugin.getElementManager().getElement(elementName);
            List<Spell> spells = plugin.getSpellManager().getSpellsForElement(elementName);
            
            int masteryLevel = magicPlayer.getElementMasteryLevel(elementName);
            
            for (Spell spell : spells) {
                // Only show spells the player has enough mastery for
                if (masteryLevel >= spell.getRequiredMasteryLevel()) {
                    form.addButton(new ElementButton(
                        element.getDisplayName() + ": " + TextFormat.WHITE + spell.getName() + "\n" +
                        TextFormat.BLUE + "Mana: " + spell.getManaCost() + " | Mastery: " + spell.getRequiredMasteryLevel()
                    ));
                }
            }
        }
        
        // Add an "Unbind" option
        form.addButton(new ElementButton(TextFormat.RED + "Unbind Spell"));
        
        player.showFormWindow(form, FORM_SPELL_BINDING);
    }
    
    /**
     * Gets a readable name for an element tier
     * @param tier The tier number
     * @return A readable tier name
     */
    private String getTierName(int tier) {
        switch(tier) {
            case 1:
                return "Basic";
            case 2:
                return "Advanced";
            case 3:
                return "Master";
            default:
                return "Unknown";
        }
    }
} 