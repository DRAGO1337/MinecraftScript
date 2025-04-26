
package com.minecraft.bot;

import java.util.*;

public class SafetyChecker {
    private static final Set<String> DANGEROUS_BLOCKS = new HashSet<>(Arrays.asList(
        "lava", "fire", "cactus", "magma_block", "powder_snow"
    ));
    
    private static final int SAFE_DISTANCE = 2;
    
    public static boolean isSafePosition(Position pos, WorldScanner scanner) {
        // Check for dangerous blocks nearby
        for (int x = -SAFE_DISTANCE; x <= SAFE_DISTANCE; x++) {
            for (int y = -SAFE_DISTANCE; y <= SAFE_DISTANCE; y++) {
                for (int z = -SAFE_DISTANCE; z <= SAFE_DISTANCE; z++) {
                    Position checkPos = new Position(
                        pos.x + x, pos.y + y, pos.z + z
                    );
                    Block block = scanner.getBlockAt(checkPos);
                    if (block != null && DANGEROUS_BLOCKS.contains(block.type)) {
                        return false;
                    }
                }
            }
        }
        
        // Check if there's a block below (prevent falling)
        Position below = new Position(pos.x, pos.y - 1, pos.z);
        Block blockBelow = scanner.getBlockAt(below);
        return blockBelow != null && blockBelow.isSolid;
    }
    
    public static boolean isPathSafe(List<Position> path, WorldScanner scanner) {
        return path.stream().allMatch(pos -> isSafePosition(pos, scanner));
    }
}
