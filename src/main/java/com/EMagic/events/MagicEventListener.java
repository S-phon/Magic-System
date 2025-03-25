package com.EMagic.events;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDeathEvent;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.PlayerMoveEvent;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.item.Item;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.level.Position;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.player.MagicPlayer;
import com.EMagic.elements.Element;
import com.EMagic.spells.SpellCastResult;
import com.EMagic.spells.SpellManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MagicEventListener implements Listener {
    
    private ElementalMagicSystem plugin;
    private Random random = new Random();
    
    // Block IDs for element mastery using numeric IDs instead of constants
    // Fire-related blocks
    private static final int[] FIRE_BLOCKS = {51, 10, 11, 61, 62}; // Fire, Lava, Flowing Lava, Furnace, Burning Furnace
    // Water-related blocks
    private static final int[] WATER_BLOCKS = {8, 9, 79, 212}; // Water, Flowing Water, Ice, Frosted Ice
    // Earth-related blocks
    private static final int[] EARTH_BLOCKS = {1, 3, 2, 12, 13, 82}; // Stone, Dirt, Grass, Sand, Gravel, Clay Block
    // Air-related blocks
    private static final int[] AIR_BLOCKS = {0, 18, 161}; // Air, Leaves, Leaves2
    // Light-related blocks
    private static final int[] LIGHT_BLOCKS = {89, 50, 169, 198}; // Glowstone, Torch, Sea Lantern, End Rod
    // Dark-related blocks
    private static final int[] DARK_BLOCKS = {49, 173, 87, 121}; // Obsidian, Coal Block, Netherrack, End Stone
    
    // Item IDs for element mastery
    private static final int[] FIRE_ITEMS = {259, 369, 377, 385}; // Flint and Steel, Blaze Rod, Blaze Powder, Fire Charge
    private static final int[] WATER_ITEMS = {326, 373, 438}; // Water Bucket, Potion, Splash Potion
    private static final int[] EARTH_ITEMS = {257, 278, 285, 274, 270}; // Iron Pickaxe, Diamond Pickaxe, Gold Pickaxe, Stone Pickaxe, Wooden Pickaxe
    private static final int[] AIR_ITEMS = {288, 444}; // Feather, Elytra
    private static final int[] LIGHT_ITEMS = {266, 348, 399}; // Gold Ingot, Glowstone Dust, Nether Star
    private static final int[] DARK_ITEMS = {263, 351, 368}; // Coal, Dye, Ender Pearl
    
    // Entity network IDs for element mastery
    private static final int[] FIRE_ENTITIES = {43, 41, 42}; // Blaze, Ghast, Magma Cube
    private static final int[] WATER_ENTITIES = {17, 49, 50}; // Squid, Guardian, Elder Guardian
    private static final int[] EARTH_ENTITIES = {32, 47, 39}; // Zombie, Husk, Silverfish
    private static final int[] AIR_ENTITIES = {19, 30, 58}; // Bat, Parrot, Phantom
    private static final int[] LIGHT_ENTITIES = {15, 20}; // Villager, Iron Golem
    private static final int[] DARK_ENTITIES = {34, 48, 52, 38}; // Skeleton, Wither Skeleton, Wither, Enderman
    
    // Timer for environment mastery gain
    private static final int ENVIRONMENT_MASTERY_TIMER = 200; // 10 seconds
    private Map<String, Long> lastEnvironmentMasteryTime = new HashMap<>();
    
    // Timer for tree mana regeneration (5 seconds)
    private static final int TREE_MANA_REGEN_TIMER = 100; // 5 seconds
    private Map<String, Long> lastTreeManaRegenTime = new HashMap<>();
    
    // Tree block IDs
    private static final int[] TREE_BLOCKS = {17, 162, 18, 161}; // Log, Log2, Leaves, Leaves2
    
    public MagicEventListener(ElementalMagicSystem plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().addMagicPlayer(player);

        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        player.sendMessage(TextFormat.AQUA + "Welcome to Elemental Magic");
        
        if (magicPlayer.getActiveElement() != null) {
            Element element = plugin.getElementManager().getElement(magicPlayer.getActiveElement());
            if (element != null) {
                player.sendMessage(TextFormat.GREEN + "Your active element is: " + element.getDisplayName());
            } else {
                magicPlayer.setActiveElement(null);
                player.sendMessage(TextFormat.YELLOW + "You don't have an active element yet. Use /magic learn to get started!");
            }
        } else {
            player.sendMessage(TextFormat.YELLOW + "You don't have an active element yet. Use /magic learn to get started!");
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getPlayerManager().savePlayerData(player.getUniqueId());
        plugin.getPlayerManager().removeMagicPlayer(player.getUniqueId());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        

        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || 
            event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            
            Item item = event.getItem();
            
            if (item != null && item.getId() == 280) { // Item.STICK = 280
                String activeElement = magicPlayer.getActiveElement();
                
                if (activeElement == null) {
                    player.sendMessage(TextFormat.RED + "You have no active element selected!");
                    return;
                }
                
                SpellManager spellManager = plugin.getSpellManager();
                SpellCastResult result;
                
                // Check if a spell is bound to the stick
                if (magicPlayer.hasSpellBound()) {
                    String boundSpellElement = magicPlayer.getBoundSpellElement();
                    String boundSpellName = magicPlayer.getBoundSpellName();
                    
                    // Make sure the player still has the element unlocked
                    if (magicPlayer.hasUnlockedElement(boundSpellElement)) {
                        result = spellManager.castSpell(player, boundSpellElement, boundSpellName);
                        
                        if (result == SpellCastResult.SUCCESS) {
                            magicPlayer.increaseElementMastery(boundSpellElement, 1);
                            
                            if (magicPlayer.getElementMasteryLevel(boundSpellElement) >= 50) {
                                checkElementCombinations(player, magicPlayer);
                            }
                        }
                    } else {
                        // Player no longer has the element unlocked
                        player.sendMessage(TextFormat.RED + "You've lost access to the bound spell's element!");
                        magicPlayer.setBoundSpell(null, null);
                        
                        // Fall back to the basic spell
                        result = spellManager.castBasicSpell(player, activeElement);
                    }
                } else {
                    // No spell bound, cast the basic spell
                    result = spellManager.castBasicSpell(player, activeElement);
                    
                    if (result == SpellCastResult.SUCCESS) {
                        magicPlayer.increaseElementMastery(activeElement, 1);
                        
                        if (magicPlayer.getElementMasteryLevel(activeElement) >= 50) {
                            checkElementCombinations(player, magicPlayer);
                        }
                    }
                }
            }
            
            if (event.getBlock() != null) {
                int blockId = event.getBlock().getId();
                for (String elementName : magicPlayer.getUnlockedElements()) {
                    int masteryGain = getMasteryGainForBlockInteraction(elementName, blockId);
                    if (masteryGain > 0 && random.nextInt(100) < 20) { // 20% chance
                        magicPlayer.increaseElementMastery(elementName, masteryGain);
                        player.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                          elementName + " mastery from block interaction!");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) event;
            
            if (edbe.getDamager() instanceof Player) {
                Player damager = (Player) edbe.getDamager();
                MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(damager);
                String activeElement = magicPlayer.getActiveElement();
                if (activeElement != null) {
                    float damageMultiplier = 1.0f;
                    switch (activeElement) {
                        case Element.FIRE:
                            if (event.getCause() == EntityDamageEvent.DamageCause.FIRE ||
                                event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                                event.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                                damageMultiplier = 1.5f;
                            }
                            break;
                        case Element.WATER:
                            if (event.getCause() == EntityDamageEvent.DamageCause.DROWNING) {
                                damageMultiplier = 1.5f;
                            }
                            break;
                        case Element.EARTH:
                            if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                                event.getCause() == EntityDamageEvent.DamageCause.CONTACT) {
                                damageMultiplier = 1.5f;
                            }
                            break;
                        case Element.AIR:
                            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                                damageMultiplier = 0.5f;
                            }
                            break;
                        case Element.LIGHT:
                            break;
                        case Element.DARK:
                            if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                                damageMultiplier = 1.3f;
                            }
                            break;
                    }
                    
                    event.setDamage((float) event.getDamage() * damageMultiplier);
                }
                
                EntityDamageEvent.DamageCause cause = event.getCause();
                
                for (String elementName : magicPlayer.getUnlockedElements()) {
                    int masteryGain = getMasteryGainForDamageCause(elementName, cause);
                    if (masteryGain > 0) {
                        magicPlayer.increaseElementMastery(elementName, masteryGain);
                        damager.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                          elementName + " mastery from elemental damage!");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDamageEvent lastDamage = entity.getLastDamageCause();
        
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbe = (EntityDamageByEntityEvent) lastDamage;
            
            if (edbe.getDamager() instanceof Player) {
                Player player = (Player) edbe.getDamager();
                MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
                int entityId = entity.getNetworkId();
                
                for (String elementName : magicPlayer.getUnlockedElements()) {
                    int masteryGain = getMasteryGainForEntityKill(elementName, entityId);
                    if (masteryGain > 0) {
                        magicPlayer.increaseElementMastery(elementName, masteryGain);
                        player.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                          elementName + " mastery from mob kill!");
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        int blockId = event.getBlock().getId();
        
        for (String elementName : magicPlayer.getUnlockedElements()) {
            int masteryGain = getMasteryGainForBlock(elementName, blockId);
            if (masteryGain > 0 && random.nextInt(100) < 30) { 
                magicPlayer.increaseElementMastery(elementName, masteryGain);
                player.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                  elementName + " mastery from mining!");
            }
        }
    }
    
    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        
        int itemId = event.getRecipe().getResult().getId();
        
        for (String elementName : magicPlayer.getUnlockedElements()) {
            int masteryGain = getMasteryGainForItem(elementName, itemId);
            if (masteryGain > 0) {
                magicPlayer.increaseElementMastery(elementName, masteryGain);
                player.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                  elementName + " mastery from crafting!");
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        MagicPlayer magicPlayer = plugin.getPlayerManager().getMagicPlayer(player);
        long currentTime = System.currentTimeMillis();
        String playerName = player.getName();

        if (!lastTreeManaRegenTime.containsKey(playerName) || 
            currentTime - lastTreeManaRegenTime.get(playerName) >= TREE_MANA_REGEN_TIMER * 50) { 
            lastTreeManaRegenTime.put(playerName, currentTime);

            if (isNearTree(player)) {

                int manaToAdd = 1 + random.nextInt(3);
                magicPlayer.addMana(manaToAdd);
                player.sendMessage(TextFormat.GREEN + "+" + manaToAdd + " mana from being near trees!");
            }
        }

        if (!lastEnvironmentMasteryTime.containsKey(playerName) || 
            currentTime - lastEnvironmentMasteryTime.get(playerName) >= ENVIRONMENT_MASTERY_TIMER * 50) { // Convert ticks to ms
            lastEnvironmentMasteryTime.put(playerName, currentTime);
            Position position = player.getPosition();
            Block blockStandingIn = player.getLevel().getBlock(position);
            Block blockBelow = player.getLevel().getBlock(position.add(0, -1, 0));
            
            for (String elementName : magicPlayer.getUnlockedElements()) {
                int masteryGain = Math.max(
                    getMasteryGainForBlock(elementName, blockStandingIn.getId()),
                    getMasteryGainForBlock(elementName, blockBelow.getId())
                );
                
                if (elementName.equals(Element.FIRE) && (player.isOnFire() || blockStandingIn.getId() == 51)) { // Block.FIRE = 51
                    masteryGain = Math.max(masteryGain, 1);
                } else if (elementName.equals(Element.WATER) && (blockStandingIn.getId() == 8 || blockStandingIn.getId() == 9)) { // Block.WATER = 8, Block.FLOWING_WATER = 9
                    masteryGain = Math.max(masteryGain, 1);
                } else if (elementName.equals(Element.AIR) && player.getY() > 100) { 
                    masteryGain = Math.max(masteryGain, 1);
                } else if (elementName.equals(Element.LIGHT) && player.getLevel().getTime() < 12000) { // Daytime
                    masteryGain = Math.max(masteryGain, 1);
                } else if (elementName.equals(Element.DARK) && player.getLevel().getTime() >= 12000) { // Nighttime
                    masteryGain = Math.max(masteryGain, 1);
                }
                
                if (masteryGain > 0 && random.nextInt(100) < 50) { 
                    magicPlayer.increaseElementMastery(elementName, masteryGain);
                    player.sendMessage(TextFormat.DARK_PURPLE + "+" + masteryGain + " " + 
                                      elementName + " mastery from your surroundings!");
                }
            }
        }
    }
    
    private int getMasteryGainForBlock(String elementName, int blockId) {
        switch (elementName) {
            case Element.FIRE:
                for (int id : FIRE_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.WATER:
                for (int id : WATER_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.EARTH:
                for (int id : EARTH_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.AIR:
                for (int id : AIR_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.LIGHT:
                for (int id : LIGHT_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.DARK:
                for (int id : DARK_BLOCKS) {
                    if (id == blockId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
        }
        return 0;
    }
    
    private int getMasteryGainForItem(String elementName, int itemId) {
        switch (elementName) {
            case Element.FIRE:
                for (int id : FIRE_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.WATER:
                for (int id : WATER_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.EARTH:
                for (int id : EARTH_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.AIR:
                for (int id : AIR_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.LIGHT:
                for (int id : LIGHT_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.DARK:
                for (int id : DARK_ITEMS) {
                    if (id == itemId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
        }
        return 0;
    }
    
    private int getMasteryGainForEntityKill(String elementName, int entityId) {
        switch (elementName) {
            case Element.FIRE:
                for (int id : FIRE_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0; // 50% chance for 1 point, averaging 0.5
                }
                break;
            case Element.WATER:
                for (int id : WATER_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.EARTH:
                for (int id : EARTH_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.AIR:
                for (int id : AIR_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.LIGHT:
                for (int id : LIGHT_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.DARK:
                for (int id : DARK_ENTITIES) {
                    if (id == entityId) return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
        }
        return 0;
    }
    
    private int getMasteryGainForDamageCause(String elementName, EntityDamageEvent.DamageCause cause) {
        switch (elementName) {
            case Element.FIRE:
                if (cause == EntityDamageEvent.DamageCause.FIRE ||
                    cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    cause == EntityDamageEvent.DamageCause.LAVA) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.WATER:
                if (cause == EntityDamageEvent.DamageCause.DROWNING) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.EARTH:
                if (cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    cause == EntityDamageEvent.DamageCause.CONTACT) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.AIR:
                if (cause == EntityDamageEvent.DamageCause.FALL) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.LIGHT:
                if (cause == EntityDamageEvent.DamageCause.LIGHTNING) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
            case Element.DARK:
                if (cause == EntityDamageEvent.DamageCause.MAGIC ||
                    cause == EntityDamageEvent.DamageCause.VOID) {
                    return random.nextInt(100) < 50 ? 1 : 0;
                }
                break;
        }
        return 0;
    }
    
    private int getMasteryGainForBlockInteraction(String elementName, int blockId) {
        return getMasteryGainForBlock(elementName, blockId);
    }
    
    private void checkElementCombinations(Player player, MagicPlayer magicPlayer) {
        for (Element element : plugin.getElementManager().getAllElements()) {
            if (element.getTier() > 1) { 
                boolean canCombine = true;
                for (String parentName : element.getParentElements()) {
                    if (!magicPlayer.hasUnlockedElement(parentName) || 
                        magicPlayer.getElementMasteryLevel(parentName) < 50) {
                        canCombine = false;
                        break;
                    }
                }

                if (canCombine && !magicPlayer.hasUnlockedElement(element.getName())) {
                    magicPlayer.unlockElement(element.getName());
                    player.sendMessage(TextFormat.GOLD + "You have discovered a new element: " + 
                                      element.getDisplayName() + TextFormat.GOLD + "!");
                }
            }
        }
    }
    
    /**
     * Checks if a player is near a tree (logs or leaves)
     * @param player The player to check
     * @return true if player is near tree blocks
     */
    private boolean isNearTree(Player player) {
        Position pos = player.getPosition();
        int radius = 1 + random.nextInt(2);
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = player.getLevel().getBlock(pos.add(x, y, z));
                    int blockId = block.getId();
                    for (int treeId : TREE_BLOCKS) {
                        if (blockId == treeId) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
} 