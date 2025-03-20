package com.EMagic.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;
import com.EMagic.spells.Spell;
import com.EMagic.spells.SpellCastResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MagicCommand extends PluginCommand<ElementalMagicSystem> {
    
    public MagicCommand(ElementalMagicSystem plugin) {
        super("magic", plugin);
        this.setDescription("Base command for the elemental magic system");
        this.setUsage("/magic [element|learn|cast|combine|list]");
        this.setPermission("elementalmagic.command.magic");
    }
    
    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "This command can only be used by players!");
            return false;
        }
        
        Player player = (Player) sender;
        ElementalMagicSystem plugin = getPlugin();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "element":
                handleElementCommand(player, magicPlayer, args);
                break;
                
            case "learn":
                handleLearnCommand(player, magicPlayer, args);
                break;
                
            case "cast":
                handleCastCommand(player, magicPlayer, args);
                break;
                
            case "combine":
                handleCombineCommand(player, magicPlayer, args);
                break;
                
            case "list":
                handleListCommand(player, magicPlayer, args);
                break;
                
            case "info":
                handleInfoCommand(player, magicPlayer);
                break;
                
            case "help":
                sendHelp(sender);
                break;
                
            default:
                sender.sendMessage(TextFormat.RED + "Unknown subcommand. Type /magic help for help.");
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextFormat.AQUA + "=== Elemental Magic Help ===");
        sender.sendMessage(TextFormat.GREEN + "/magic element <name>" + TextFormat.WHITE + " - Set your active element");
        sender.sendMessage(TextFormat.GREEN + "/magic learn <element>" + TextFormat.WHITE + " - Learn a new element (if eligible)");
        sender.sendMessage(TextFormat.GREEN + "/magic cast <spell>" + TextFormat.WHITE + " - Cast a specific spell");
        sender.sendMessage(TextFormat.GREEN + "/magic combine <element1> <element2>" + TextFormat.WHITE + " - Try to combine elements");
        sender.sendMessage(TextFormat.GREEN + "/magic list [elements|spells]" + TextFormat.WHITE + " - List elements or spells");
        sender.sendMessage(TextFormat.GREEN + "/magic info" + TextFormat.WHITE + " - Show your magic information");
    }
    
    private void handleElementCommand(Player player, MagicPlayer magicPlayer, String[] args) {
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage: /magic element <name>");
            return;
        }
        
        String elementName = args[1].toLowerCase();
        
        if (!getPlugin().getElementManager().hasElement(elementName)) {
            player.sendMessage(TextFormat.RED + "That element doesn't exist!");
            return;
        }
        
        if (!magicPlayer.hasUnlockedElement(elementName)) {
            player.sendMessage(TextFormat.RED + "You haven't unlocked that element yet!");
            return;
        }
        
        magicPlayer.setActiveElement(elementName);
        Element element = getPlugin().getElementManager().getElement(elementName);
        player.sendMessage(TextFormat.GREEN + "You have set your active element to: " + element.getDisplayName());
    }
    
    private void handleLearnCommand(Player player, MagicPlayer magicPlayer, String[] args) {
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage: /magic learn <element>");
            return;
        }
        
        String elementName = args[1].toLowerCase();
        
        if (!getPlugin().getElementManager().hasElement(elementName)) {
            player.sendMessage(TextFormat.RED + "That element doesn't exist!");
            return;
        }
        
        if (magicPlayer.hasUnlockedElement(elementName)) {
            player.sendMessage(TextFormat.RED + "You have already unlocked this element!");
            return;
        }
        
        Element element = getPlugin().getElementManager().getElement(elementName);
        
        // Check if element is a basic element
        if (element.getTier() == 1) {
            magicPlayer.unlockElement(elementName);
            player.sendMessage(TextFormat.GREEN + "You have learned the " + element.getDisplayName() + TextFormat.GREEN + " element!");
        } else {
            player.sendMessage(TextFormat.RED + "You can't learn advanced elements directly. You must combine other elements!");
            List<String> parentElements = element.getParentElements();
            StringBuilder hintBuilder = new StringBuilder(TextFormat.YELLOW + "Hint: This element requires ");
            
            for (int i = 0; i < parentElements.size(); i++) {
                String parentName = parentElements.get(i);
                Element parentElement = getPlugin().getElementManager().getElement(parentName);
                
                hintBuilder.append(parentElement.getDisplayName());
                
                if (i < parentElements.size() - 1) {
                    hintBuilder.append(TextFormat.YELLOW + ", ");
                }
            }
            
            player.sendMessage(hintBuilder.toString());
        }
    }
    
    private void handleCastCommand(Player player, MagicPlayer magicPlayer, String[] args) {
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage: /magic cast <spell>");
            return;
        }
        
        String spellName = args[1].toLowerCase();
        String activeElement = magicPlayer.getActiveElement();
        
        if (activeElement == null) {
            player.sendMessage(TextFormat.RED + "You need to set an active element first!");
            return;
        }
        
        SpellCastResult result = getPlugin().getSpellManager().castSpell(player, activeElement, spellName);
        
        if (result != SpellCastResult.SUCCESS) {
            player.sendMessage(TextFormat.RED + "Failed to cast the spell!");
        }
    }
    
    private void handleCombineCommand(Player player, MagicPlayer magicPlayer, String[] args) {
        if (args.length < 3) {
            player.sendMessage(TextFormat.RED + "Usage: /magic combine <element1> <element2>");
            return;
        }
        
        String element1 = args[1].toLowerCase();
        String element2 = args[2].toLowerCase();
        
        if (!getPlugin().getElementManager().hasElement(element1) || !getPlugin().getElementManager().hasElement(element2)) {
            player.sendMessage(TextFormat.RED + "One or both of those elements don't exist!");
            return;
        }
        
        if (!magicPlayer.hasUnlockedElement(element1) || !magicPlayer.hasUnlockedElement(element2)) {
            player.sendMessage(TextFormat.RED + "You haven't unlocked one or both of those elements!");
            return;
        }
        
        Element firstElement = getPlugin().getElementManager().getElement(element1);
        Element secondElement = getPlugin().getElementManager().getElement(element2);
        
        Element result = getPlugin().getElementManager().combineElements(firstElement, secondElement);
        
        if (result == null) {
            player.sendMessage(TextFormat.RED + "Those elements cannot be combined!");
            return;
        }
        
        if (magicPlayer.hasUnlockedElement(result.getName())) {
            player.sendMessage(TextFormat.YELLOW + "You already know the " + result.getDisplayName() + TextFormat.YELLOW + " element!");
            return;
        }

        int element1Mastery = magicPlayer.getElementMasteryLevel(element1);
        int element2Mastery = magicPlayer.getElementMasteryLevel(element2);
        
        if (element1Mastery < 750 || element2Mastery < 750) {
            player.sendMessage(TextFormat.RED + "You need at least 750 mastery in both elements to combine them!");
            player.sendMessage(TextFormat.YELLOW + "Your mastery: " + firstElement.getDisplayName() +
                    TextFormat.YELLOW + ": " + element1Mastery + "/750, " +
                    secondElement.getDisplayName() + TextFormat.YELLOW + ": " + element2Mastery + "/750");
            return;
        }

        magicPlayer.unlockElement(result.getName());
        player.sendMessage(TextFormat.GOLD + "You have successfully combined " + 
                           firstElement.getDisplayName() + TextFormat.GOLD + " and " + 
                           secondElement.getDisplayName() + TextFormat.GOLD + " to create " + 
                           result.getDisplayName() + TextFormat.GOLD + "!");
    }
    
    private void handleListCommand(Player player, MagicPlayer magicPlayer, String[] args) {
        if (args.length < 2) {
            player.sendMessage(TextFormat.RED + "Usage: /magic list [elements|spells]");
            return;
        }
        
        String listType = args[1].toLowerCase();
        
        if (listType.equals("elements")) {
            player.sendMessage(TextFormat.AQUA + "=== Available Elements ===");
            
            for (int tier = 1; tier <= 3; tier++) {
                List<Element> tierElements = getPlugin().getElementManager().getElementsByTier(tier);
                
                String tierName;
                switch (tier) {
                    case 1:
                        tierName = "Basic";
                        break;
                    case 2:
                        tierName = "Advanced";
                        break;
                    case 3:
                        tierName = "Divine";
                        break;
                    default:
                        tierName = "Unknown";
                }
                
                player.sendMessage(TextFormat.GOLD + "-- " + tierName + " Elements --");
                
                for (Element element : tierElements) {
                    boolean unlocked = magicPlayer.hasUnlockedElement(element.getName());
                    String status = unlocked ? TextFormat.GREEN + "[UNLOCKED]" : TextFormat.RED + "[LOCKED]";
                    
                    if (unlocked) {
                        int mastery = magicPlayer.getElementMasteryLevel(element.getName());
                        status += TextFormat.YELLOW + " (Mastery: " + mastery + "/100)";
                    }
                    
                    player.sendMessage(status + " " + element.getDisplayName());
                }
            }
        } else if (listType.equals("spells")) {
            String activeElement = magicPlayer.getActiveElement();
            
            if (activeElement == null) {
                player.sendMessage(TextFormat.RED + "You need to set an active element first!");
                return;
            }
            
            Element element = getPlugin().getElementManager().getElement(activeElement);
            List<Spell> spells = getPlugin().getSpellManager().getSpellsForElement(activeElement);
            
            player.sendMessage(TextFormat.AQUA + "=== Spells for " + element.getDisplayName() + TextFormat.AQUA + " ===");
            
            if (spells.isEmpty()) {
                player.sendMessage(TextFormat.YELLOW + "No spells available for this element yet.");
                return;
            }
            
            int masteryLevel = magicPlayer.getElementMasteryLevel(activeElement);
            
            for (Spell spell : spells) {
                boolean available = masteryLevel >= spell.getRequiredMasteryLevel();
                String status = available ? TextFormat.GREEN + "[AVAILABLE]" : TextFormat.RED + "[LOCKED]";
                
                if (available) {
                    if (magicPlayer.hasCooldown(spell.getName())) {
                        long cooldown = magicPlayer.getRemainingCooldown(spell.getName()) / 1000;
                        status = TextFormat.YELLOW + "[COOLDOWN: " + cooldown + "s]";
                    }
                } else {
                    status += TextFormat.YELLOW + " (Requires " + spell.getRequiredMasteryLevel() + " mastery)";
                }
                
                player.sendMessage(status + " " + TextFormat.WHITE + spell.getName() + ": " + spell.getDescription());
            }
        } else {
            player.sendMessage(TextFormat.RED + "Invalid list type. Use 'elements' or 'spells'.");
        }
    }
    
    private void handleInfoCommand(Player player, MagicPlayer magicPlayer) {
        player.sendMessage(TextFormat.AQUA + "=== Your Magic Information ===");
        player.sendMessage(TextFormat.BLUE + "Mana: " + magicPlayer.getMana() + "/" + magicPlayer.getMaxMana());
        player.sendMessage(TextFormat.BLUE + "Mana Regeneration Rate: " + magicPlayer.getManaRegenRate() + " per tick");
        String activeElement = magicPlayer.getActiveElement();
        if (activeElement != null) {
            Element element = getPlugin().getElementManager().getElement(activeElement);
            player.sendMessage(TextFormat.GREEN + "Active Element: " + element.getDisplayName());
        } else {
            player.sendMessage(TextFormat.YELLOW + "No active element selected");
        }

        Set<String> unlockedElements = magicPlayer.getUnlockedElements();
        player.sendMessage(TextFormat.GREEN + "Unlocked Elements: " + unlockedElements.size());
        player.sendMessage(TextFormat.GOLD + "Elemental Mastery Levels:");
        for (String elementName : unlockedElements) {
            Element element = getPlugin().getElementManager().getElement(elementName);
            int mastery = magicPlayer.getElementMasteryLevel(elementName);
            player.sendMessage("  " + element.getDisplayName() + TextFormat.WHITE + ": " + mastery + "/100");
        }
    }
} 
