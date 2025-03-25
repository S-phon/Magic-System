package com.EMagic.events;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

import com.EMagic.spells.Spell;

/**
 * Event fired when a player casts a spell
 */
public class SpellCastEvent extends Event implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final Spell spell;
    private boolean cancelled;
    
    /**
     * Creates a new SpellCastEvent
     * @param player The player casting the spell
     * @param spell The spell being cast
     */
    public SpellCastEvent(Player player, Spell spell) {
        this.player = player;
        this.spell = spell;
        this.cancelled = false;
    }
    
    /**
     * Gets the player casting the spell
     * @return The player casting the spell
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Gets the spell being cast
     * @return The spell being cast
     */
    public Spell getSpell() {
        return spell;
    }
    
    /**
     * Gets the handlers for this event
     * @return The handler list
     */
    public HandlerList getHandlers() {
        return handlers;
    }
    
    /**
     * Gets the handler list for this event type
     * @return The handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
} 