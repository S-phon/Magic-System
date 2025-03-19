package com.EMagic.spells;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockAir;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.GenericParticle;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.TextFormat;

import com.EMagic.ElementalMagicSystem;
import com.EMagic.elements.Element;
import com.EMagic.player.MagicPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class IlluminateSpell extends BasicSpell {
    
    private static final int RADIUS = 10;
    private static final int DURATION = 30; 
    private static final int LIGHT_SPACING = 3; 
    private static final int LIGHT_BLOCK_ID = 124; // Glowstone

    private static Map<String, List<LightSource>> playerLightSources = new HashMap<>();
    
    public IlluminateSpell() {
        super(
            "illuminate",
            "Creates light in the area",
            Element.LIGHT,
            10, // Mana cost
            1,  // Required mastery level
            1000, // Cooldown time
            Sound.RANDOM_ORB // Cast sound
        );
    }

    @Override
    protected SpellCastResult executeSpell(Player player, MagicPlayer magicPlayer) {
        clearExistingLights(player);
        List<LightSource> lightSources = createLightSources(player);
        playerLightSources.put(player.getName(), lightSources);
        scheduleRemoval(player);
        createLightBurst(player);
        player.sendMessage(TextFormat.YELLOW + "You illuminate the area around you!");
        
        return SpellCastResult.SUCCESS;
    }
    
    private void clearExistingLights(Player player) {
        List<LightSource> existing = playerLightSources.get(player.getName());
        if (existing != null) {
            for (LightSource light : existing) {
                light.restore();
            }
            playerLightSources.remove(player.getName());
        }
    }
    
    private List<LightSource> createLightSources(Player player) {
        List<LightSource> lights = new ArrayList<>();
        Location center = player.getLocation();
        for (int x = -RADIUS; x <= RADIUS; x += LIGHT_SPACING) {
            for (int z = -RADIUS; z <= RADIUS; z += LIGHT_SPACING) {
                if (x*x + z*z > RADIUS*RADIUS) {
                    continue;
                }

                int y = findHighestBlock(player, center.getFloorX() + x, center.getFloorZ() + z);
                placeLight(player, new Vector3(center.getFloorX() + x, y + 1, center.getFloorZ() + z), lights);
            }
        }

        placeLight(player, new Vector3(center.getFloorX(), center.getFloorY() + 2, center.getFloorZ()), lights);
        
        return lights;
    }
    
    private int findHighestBlock(Player player, int x, int z) {
        for (int y = player.getFloorY() + 5; y >= player.getFloorY() - 5; y--) {
            Block block = player.getLevel().getBlock(new Vector3(x, y, z));
            Block blockAbove = player.getLevel().getBlock(new Vector3(x, y + 1, z));
            
            if (!block.canBeFlowedInto() && blockAbove.canBeFlowedInto()) {
                return y;
            }
        }
        
        return player.getFloorY();
    }
    
    private void placeLight(Player player, Vector3 pos, List<LightSource> lights) {
        Block original = player.getLevel().getBlock(pos);

        if (original.getId() == Block.AIR) {
            LightSource light = new LightSource(player.getLevel(), pos, original);
            lights.add(light);
            Block lightBlock = Block.get(LIGHT_BLOCK_ID);
            player.getLevel().setBlock(pos, lightBlock, false, false);
            player.getLevel().addParticle(new GenericParticle(
                pos.add(0.5, 0.5, 0.5),
                Particle.TYPE_FIREWORKS_SPARK
            ));
        }
    }
    
    private void scheduleRemoval(Player player) {
        ElementalMagicSystem plugin = (ElementalMagicSystem) player.getServer().getPluginManager().getPlugin("ElementalMagicSystem");
        
        player.getServer().getScheduler().scheduleDelayedTask(plugin, () -> {
            clearExistingLights(player);
        }, DURATION * 20); // 20 tick
    }
    
    private void createLightBurst(Player player) {
        Vector3 center = player.add(0, 1, 0);
        for (int ring = 0; ring < 4; ring++) {
            double radius = ring * 0.8;
            int particles = 8 + (ring * 4);
            for (int i = 0; i < particles; i++) {
                double angle1 = 2 * Math.PI * i / particles;
                for (int j = 0; j < particles/2; j++) {
                    double angle2 = Math.PI * j / (particles/2);
                    double x = center.x + radius * Math.sin(angle1) * Math.sin(angle2);
                    double y = center.y + radius * Math.cos(angle2);
                    double z = center.z + radius * Math.cos(angle1) * Math.sin(angle2);
                    Vector3 particlePos = new Vector3(x, y, z);
                    player.getLevel().addParticle(new GenericParticle(
                        particlePos,
                        Particle.TYPE_FIREWORKS_SPARK
                    ));
                }
            }
        }
    }
    
    private static class LightSource {
        private final cn.nukkit.level.Level level;
        private final Vector3 position;
        private final Block originalBlock;
        
        public LightSource(cn.nukkit.level.Level level, Vector3 position, Block originalBlock) {
            this.level = level;
            this.position = position;
            this.originalBlock = originalBlock;
        }
        
        public void restore() {
            level.setBlock(position, originalBlock, false, false);
        }
    }
}
