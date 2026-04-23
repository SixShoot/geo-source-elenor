package com.aionemu.gameserver.geoEngine.collision.bih;

/**
 * Constants for Bounding Interval Hierarchy (BIH) collision system.
 * 
 * BIH is an advanced spatial acceleration structure that provides:
 * - Faster ray-triangle intersection testing
 * - Reduced cache misses compared to linear search
 * - Better memory locality for collision queries
 * - Scalable performance for large geometric datasets
 * 
 * @author Encom / SixShoot
 * @version 1.0
 */
public final class BIHConstants {
    // Tree construction parameters
    public static final int MAX_TREE_DEPTH = 100;
    public static final int MAX_TRIS_PER_NODE = 21;
    public static final int MIN_TRIS_FOR_SPLIT = 1;
    
    // Split strategy
    public static final boolean USE_SAH = false;
    public static final int NUM_SPLIT_ATTEMPTS = 3;
    
    // Performance tuning
    public static final boolean ENABLE_SIMD = false;
    public static final boolean CACHE_BOUNDS = true;
    
    // Memory settings
    public static final int SWAP_BUFFER_SIZE = 9;
    
    private BIHConstants() {
        throw new AssertionError("BIHConstants cannot be instantiated");
    }
}
