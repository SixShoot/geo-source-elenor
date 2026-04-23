package com.aionemu.gameserver.world.geo.nav;

/**
 * Constants for Navigation Mesh (NavMesh) pathfinding system.
 * 
 * The NavMesh system implements advanced features:
 * - Corridor-based pathfinding
 * - Funnel algorithm for path smoothing
 * - Direct path checking
 * - Soft cache with garbage collection support
 * 
 * @author Aion Reconstruction Project / SixShoot
 * @version 1.0
 */
public final class NavConstants {
    // Pathfinding parameters
    public static final float ARBITRARY_SMALL_VALUE = 5.0f;
    public static final int ARBITRARY_LARGE_VALUE = 800;
    public static final float PATH_WEIGHT = 0.2f;
    public static final float HEURISTIC_WEIGHT = 1.0f;
    
    // Direct path validation
    public static final int MAX_STRAIGHT_LINE_ATTEMPTS = 50;
    
    // Funnel algorithm constants
    public static final int MAX_FUNNEL_ITERATIONS = 800;
    public static final float FUNNEL_MARGIN = 0.1f;
    
    // Ray casting for tile detection
    public static final float NAV_RAY_Z_OFFSET = 1.0f;
    public static final float NAV_RAY_LIMIT = 5.0f;
    
    // Bounding box for tile detection
    public static final float NAV_BOX_X_EXTENT = 0.8f;
    public static final float NAV_BOX_Y_EXTENT = 0.8f;
    public static final float NAV_BOX_Z_MIN = -1.0f;
    public static final float NAV_BOX_Z_MAX = 4.0f;
    public static final float NAV_BOX_CENTER_Z_OFFSET = 0.2f;
    
    // Performance optimization
    public static final boolean ENABLE_SOFT_CACHE = true;
    public static final boolean USE_LAZY_LOADING = true;
    public static final int MAX_CACHED_TILES = 1000;
    
    // Debug and logging
    public static final boolean LOG_PATHFINDING_ERRORS = false;
    public static final boolean LOG_PERFORMANCE_STATS = false;
    
    private NavConstants() {
        throw new AssertionError("NavConstants cannot be instantiated");
    }
}
