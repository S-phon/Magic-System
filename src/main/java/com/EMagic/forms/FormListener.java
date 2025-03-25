package com.EMagic.forms;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
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
 * Handles form responses for the magic system
 */
public class FormListener implements Listener {
    
    private final ElementalMagicSystem plugin;
    private final MagicForms forms;
    
    public FormListener(ElementalMagicSystem plugin) {
        this.plugin = plugin;
        this.forms = new MagicForms(plugin);
    }
    
    @EventHandler
    public void onFormResponse(PlayerFormRespondedEvent event) {
        Player player = event.getPlayer();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        if (magicPlayer == null) return;
        
        // Check if form was closed
        if (event.wasClosed()) return;
        
        int formId = event.getFormID();
        
        switch (formId) {
            case MagicForms.FORM_MAIN_MENU:
                handleMainMenuResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_ELEMENT_SELECT:
                handleElementSelectResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_ELEMENT_LEARN:
                handleElementLearnResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_SPELL_CAST:
                handleSpellCastResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_ELEMENT_COMBINE:
                handleElementCombineResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_ELEMENTS_LIST:
                handleElementsListResponse(player, event);
                break;
                
            case MagicForms.FORM_SPELLS_LIST:
                handleSpellsListResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_SPELL_BINDING:
                handleSpellBindingResponse(player, magicPlayer, event);
                break;
                
            case MagicForms.FORM_PLAYER_INFO:
                forms.showMainMenu(player);
                break;
        }
    }
    
    /**
     * Handle response from the main menu form
     */
    private void handleMainMenuResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        switch (buttonId) {
            case 0: // Select Active Element
                forms.showElementSelect(player, magicPlayer);
                break;
                
            case 1: // Learn New Element
                forms.showLearnElement(player, magicPlayer);
                break;
                
            case 2: // Cast Spell
                forms.showSpellCast(player, magicPlayer);
                break;
                
            case 3: // Combine Elements
                forms.showElementCombine(player, magicPlayer);
                break;
                
            case 4: // View Elements
                forms.showElementsList(player);
                break;
                
            case 5: // View Spells
                forms.showSpellsList(player, magicPlayer);
                break;
                
            case 6: // My Magic Info
                forms.showPlayerInfo(player, magicPlayer);
                break;
        }
    }
    
    /**
     * Handle response from the element selection form
     */
    private void handleElementSelectResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof FormWindowModal) {
            FormResponseModal response = (FormResponseModal) event.getResponse();
            if (response.getClickedButtonId() == 0) {
                forms.showLearnElement(player, magicPlayer);
            } else {
                forms.showMainMenu(player);
            }
            return;
        }
        
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        // Convert unlocked elements to a list for indexing
        List<String> elementsList = new ArrayList<>(magicPlayer.getUnlockedElements());
        
        if (buttonId >= 0 && buttonId < elementsList.size()) {
            String selectedElement = elementsList.get(buttonId);
            magicPlayer.setActiveElement(selectedElement);
            
            Element element = plugin.getElementManager().getElement(selectedElement);
            player.sendMessage(TextFormat.GREEN + "You have set your active element to: " + element.getDisplayName());
        }
        
        forms.showMainMenu(player);
    }
    
    /**
     * Handle response from the element learning form
     */
    private void handleElementLearnResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof FormWindowModal) {
            FormResponseModal response = (FormResponseModal) event.getResponse();
            if (response.getClickedButtonId() == 0) { // Combine Elements button
                forms.showElementCombine(player, magicPlayer);
            } else {
                forms.showMainMenu(player);
            }
            return;
        }
        
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        // Get all available basic elements the player hasn't learned yet
        List<Element> availableElements = new ArrayList<>();
        for (Element element : plugin.getElementManager().getElementsByTier(1)) {
            if (!magicPlayer.hasUnlockedElement(element.getName())) {
                availableElements.add(element);
            }
        }
        
        if (buttonId >= 0 && buttonId < availableElements.size()) {
            Element selectedElement = availableElements.get(buttonId);
            magicPlayer.unlockElement(selectedElement.getName());
            
            player.sendMessage(TextFormat.GREEN + "You have learned the " + selectedElement.getDisplayName() + 
                              TextFormat.GREEN + " element!");
        }
        
        forms.showMainMenu(player);
    }
    
    /**
     * Handle response from the spell casting form
     */
    private void handleSpellCastResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof FormWindowModal) {
            FormResponseModal response = (FormResponseModal) event.getResponse();
            if (response.getClickedButtonId() == 0) { 
                forms.showElementSelect(player, magicPlayer);
            } else {
                forms.showMainMenu(player);
            }
            return;
        }
        
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        String activeElement = magicPlayer.getActiveElement();
        List<Spell> elementSpells = plugin.getSpellManager().getSpellsForElement(activeElement);
        
        if (buttonId >= 0 && buttonId < elementSpells.size()) {
            Spell selectedSpell = elementSpells.get(buttonId);
            
            // Check if player has enough mastery
            int masteryLevel = magicPlayer.getElementMasteryLevel(activeElement);
            if (masteryLevel < selectedSpell.getRequiredMasteryLevel()) {
                player.sendMessage(TextFormat.RED + "You need " + selectedSpell.getRequiredMasteryLevel() + " " + 
                                  plugin.getElementManager().getElement(activeElement).getDisplayName() + 
                                  TextFormat.RED + " mastery to cast this spell!");
                forms.showSpellCast(player, magicPlayer);
                return;
            }
            
            // Try to cast the spell
            SpellCastResult result = selectedSpell.cast(player, magicPlayer);
            
            if (result != SpellCastResult.SUCCESS) {
                player.sendMessage(TextFormat.RED + "Failed to cast the spell: " + result.name());
            }
        }
        
        // Reopen the form after casting
        forms.showSpellCast(player, magicPlayer);
    }
    
    /**
     * Handle response from the element combination form
     */
    private void handleElementCombineResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof FormWindowModal) {
            FormResponseModal response = (FormResponseModal) event.getResponse();
            if (response.getClickedButtonId() == 0) { // Learn More Elements button
                forms.showLearnElement(player, magicPlayer);
            } else {
                forms.showMainMenu(player);
            }
            return;
        }
        
        FormResponseCustom response = (FormResponseCustom) event.getResponse();
        
        int firstElementIndex = (int) response.getResponse(1);
        int secondElementIndex = (int) response.getResponse(2);
        
        // Convert unlocked elements to a list for indexing
        List<String> elementsList = new ArrayList<>(magicPlayer.getUnlockedElements());
        
        if (firstElementIndex < 0 || firstElementIndex >= elementsList.size() || 
            secondElementIndex < 0 || secondElementIndex >= elementsList.size()) {
            player.sendMessage(TextFormat.RED + "Invalid element selection!");
            forms.showMainMenu(player);
            return;
        }
        
        String element1 = elementsList.get(firstElementIndex);
        String element2 = elementsList.get(secondElementIndex);
        
        // Skip if same element selected twice
        if (element1.equals(element2)) {
            player.sendMessage(TextFormat.RED + "You cannot combine an element with itself!");
            forms.showElementCombine(player, magicPlayer);
            return;
        }
        
        Element firstElement = plugin.getElementManager().getElement(element1);
        Element secondElement = plugin.getElementManager().getElement(element2);
        
        Element result = plugin.getElementManager().combineElements(firstElement, secondElement);
        
        if (result == null) {
            player.sendMessage(TextFormat.RED + "Those elements cannot be combined!");
            forms.showElementCombine(player, magicPlayer);
            return;
        }
        
        if (magicPlayer.hasUnlockedElement(result.getName())) {
            player.sendMessage(TextFormat.YELLOW + "You already know the " + result.getDisplayName() + TextFormat.YELLOW + " element!");
            forms.showMainMenu(player);
            return;
        }

        int element1Mastery = magicPlayer.getElementMasteryLevel(element1);
        int element2Mastery = magicPlayer.getElementMasteryLevel(element2);
        
        if (element1Mastery < 750 || element2Mastery < 750) {
            player.sendMessage(TextFormat.RED + "You need at least 750 mastery in both elements to combine them!");
            player.sendMessage(TextFormat.YELLOW + "Your mastery: " + firstElement.getDisplayName() +
                    TextFormat.YELLOW + ": " + element1Mastery + "/750, " +
                    secondElement.getDisplayName() + TextFormat.YELLOW + ": " + element2Mastery + "/750");
            forms.showMainMenu(player);
            return;
        }

        magicPlayer.unlockElement(result.getName());
        player.sendMessage(TextFormat.GOLD + "You have successfully combined " + 
                           firstElement.getDisplayName() + TextFormat.GOLD + " and " + 
                           secondElement.getDisplayName() + TextFormat.GOLD + " to create " + 
                           result.getDisplayName() + TextFormat.GOLD + "!");
        
        forms.showMainMenu(player);
    }
    
    /**
     * Handle response from the elements list form
     */
    private void handleElementsListResponse(Player player, PlayerFormRespondedEvent event) {
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        // Collect all elements across tiers into a flat list
        List<Element> allElements = new ArrayList<>();
        for (int tier = 1; tier <= 3; tier++) {
            allElements.addAll(plugin.getElementManager().getElementsByTier(tier));
        }
        
        if (buttonId >= 0 && buttonId < allElements.size()) {
            Element selectedElement = allElements.get(buttonId);
            forms.showElementDetails(player, selectedElement);
        } else {
            forms.showMainMenu(player);
        }
    }
    
    /**
     * Handle response from the spells list form
     */
    private void handleSpellsListResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        // All Spells is first button (index 0)
        if (buttonId == 0) {
            forms.showElementSpells(player, "all");
            return;
        }
        
        // Element spells buttons follow (starting at index 1)
        buttonId--;
        
        // Convert unlocked elements to a list for indexing
        List<String> elementsList = new ArrayList<>(magicPlayer.getUnlockedElements());
        
        if (buttonId >= 0 && buttonId < elementsList.size()) {
            String elementName = elementsList.get(buttonId);
            forms.showElementSpells(player, elementName);
        } else {
            forms.showMainMenu(player);
        }
    }
    
    /**
     * Handle response from the spell binding form
     */
    private void handleSpellBindingResponse(Player player, MagicPlayer magicPlayer, PlayerFormRespondedEvent event) {
        if (event.getWindow() instanceof FormWindowModal) {
            FormResponseModal response = (FormResponseModal) event.getResponse();
            if (response.getClickedButtonId() == 0) { // Learn Element button
                forms.showLearnElement(player, magicPlayer);
            } else {
                forms.showMainMenu(player);
            }
            return;
        }
        
        FormResponseSimple response = (FormResponseSimple) event.getResponse();
        int buttonId = response.getClickedButtonId();
        
        // Get all available spells for the player
        List<Spell> availableSpells = new ArrayList<>();
        for (String elementName : magicPlayer.getUnlockedElements()) {
            int masteryLevel = magicPlayer.getElementMasteryLevel(elementName);
            
            for (Spell spell : plugin.getSpellManager().getSpellsForElement(elementName)) {
                if (masteryLevel >= spell.getRequiredMasteryLevel()) {
                    availableSpells.add(spell);
                }
            }
        }
        
        // Last button is "Unbind Spell"
        if (buttonId == availableSpells.size()) {
            magicPlayer.setBoundSpell(null, null);
            player.sendMessage(TextFormat.YELLOW + "You have unbound any spell from your stick.");
            forms.showMainMenu(player);
            return;
        }
        
        if (buttonId >= 0 && buttonId < availableSpells.size()) {
            Spell selectedSpell = availableSpells.get(buttonId);
            magicPlayer.setBoundSpell(selectedSpell.getElement(), selectedSpell.getName());
            
            player.sendMessage(TextFormat.GREEN + "You have bound " + 
                               TextFormat.GOLD + selectedSpell.getName() + 
                               TextFormat.GREEN + " to your stick!");
        }
        
        forms.showMainMenu(player);
    }
} 