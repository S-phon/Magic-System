# Elements Magic System

A magic system with elements powers, combinations, and progression for Nukkit.

## Features

- 6 Basic elements: Fire, Water, Earth, Air, Light, and Dark
- Advanced elements created by combining basic elements (Lightning, Ice, Lava, etc.)
- Divine elements through rare combinations
- Mastery progression system for each element
- Mana system with regeneration
- Tree-based mana regeneration (1-3 mana every 5 seconds when near trees)
- Cooldown-based spells with effects that improve as you master elements
- Element-specific damage modifiers
- Element combination system

## Commands

- `/magic element <n>` - Set your active element
- `/magic learn <element>` - Learn a new element (basic elements only)
- `/magic cast <spell>` - Cast a specific spell
- `/magic combine <element1> <element2>` - Try to combine elements
- `/magic list [elements|spells]` - List elements or spells
- `/magic info` - Show your magic information

## How to Use

1. Start with a basic element (`/magic learn fire`)
2. Set it as your active element (`/magic element fire`)
3. Cast spells using a wand (stick) with right-click
4. Gain mastery by successfully casting spells, killing related mobs, breaking elemental blocks, and more
5. Stand near trees (logs or leaves) to regenerate mana faster
6. Once you reach 50 mastery in an element, you can combine it with others
7. Discover new advanced and divine elements

## Mastery Progression

Mastery points can be gained from various activities:
- Killing element-related mobs (50% chance for 1 point)
- Breaking element-related blocks (50% chance for 1 point)
- Crafting element-related items (50% chance for 1 point)
- Taking elemental damage types (50% chance for 1 point)
- Casting spells (1 point per successful cast)
- Being in appropriate environments (forests, mountains, etc.)

## Element Combinations

- Fire + Air = Lightning
- Water + Air = Ice
- Fire + Earth = Lava
- Earth + Water = Nature
- Light + Dark = Arcane
- Light + Air + Fire = Cosmic (Divine)
- Dark + Water + Earth = Void (Divine)

## Installation

1. Place the ElementsMagicSystem.jar file in your server's plugins folder
2. Restart your server
3. Configure the plugin in the config.yml file

## Configuration

- `config.yml` - General plugin settings

## License

This project is licensed under the MIT License - see the LICENSE file for details. 