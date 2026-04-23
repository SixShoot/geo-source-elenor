package com.aionemu.gameserver.world.geo.nav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.models.GeoMap;
import com.aionemu.gameserver.geoEngine.scene.NavGeometry;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * Navigation Service - Entry point for pathfinding and navigation queries.
 * 
 * This service provides:
 * - NavMesh-based pathfinding
 * - Path smoothing using funnel algorithm
 * - Direct line-of-sight validation
 * - Entity pulling validation
 * 
 * Ported from AionEncomBase with enhancements for geo-source-elenor.
 * 
 * @author Aion Reconstruction Project / SixShoot
 * @version 1.0
 */
public final class NavService {
    
    private static final Logger LOG = LoggerFactory.getLogger(NavService.class);
    private final NavData navData = NavData.getInstance();
    
    /**
     * Initializes the navigation system.
     */
    public void initializeNav() {
        if (GeoDataConfig.GEO_ENABLE) {
            LOG.info("Navigational Data is Enabled.");
            if (!navData.isLoaded()) {
                navData.loadNavMaps();
            } else {
                LOG.warn("Attempted Double Loading of Navigational Data.");
            }
        } else {
            LOG.info("Navigational Data is Disabled.");
        }
    }
    
    /**
     * Checks if a creature can pull (forcibly move) a target.
     * 
     * @param creature The entity attempting to pull
     * @param target The target entity to be pulled
     * @return true if the target can be pulled, false otherwise
     */
    public boolean canPullTarget(Creature creature, Creature target) {
        if (!GeoDataConfig.GEO_ENABLE) return true;
        if (target.isFlying()) return true;
        
        float x1 = creature.getX(), y1 = creature.getY(), z1 = creature.getZ();
        NavGeometry tile1 = getNavTile(creature.getWorldId(), x1, y1, z1);
        if (tile1 == null) {
            tile1 = getNavTileWithBox(creature.getWorldId(), x1, y1, z1);
            if (tile1 == null) return false;
        }
        
        float x2 = target.getX(), y2 = target.getY(), z2 = target.getZ();
        NavGeometry tile2 = getNavTile(target.getWorldId(), x2, y2, z2);
        if (tile2 == null) {
            tile2 = getNavTileWithBox(target.getWorldId(), x2, y2, z2);
            if (tile2 == null) return false;
        }
        
        float[][] path = attemptStraightLinePath(tile2, tile1, x2, y2, z2, x1, y1, z1);
        return path != null && path.length == 1;
    }
    
    /**
     * Attempts a straight-line path through the navmesh.
     */
    private float[][] attemptStraightLinePath(NavGeometry tile1, NavGeometry tile2, 
                                              float x1, float y1, float z1, 
                                              float x2, float y2, float z2) {
        if (tile1 == null) return null;
        if (tile2 == null) return null;
        if (tile1 == tile2) return new float[][] {{x2, y2, z2}};
        
        // TODO: Implement funnel-based pathfinding
        return null;
    }
    
    /**
     * Finds a path from one creature to another.
     * 
     * @param pathOwner The creature finding the path
     * @param target The target creature
     * @return Array of waypoints [[x,y,z], [x,y,z], ...], or null if no path found
     */
    public float[][] navigateToTarget(Creature pathOwner, Creature target) {
        if (pathOwner == null || target == null) return null;
        if (pathOwner.getLifeStats().isAlreadyDead()) return null;
        if (pathOwner.getWorldId() != target.getWorldId()) return null;
        
        int worldId = pathOwner.getWorldId();
        float x1 = pathOwner.getX(), y1 = pathOwner.getY(), z1 = pathOwner.getZ();
        float x2 = target.getX(), y2 = target.getY(), z2 = target.getZ();
        
        return navigateFromLocationToLocation(worldId, null, null, x1, y1, z1, x2, y2, z2);
    }
    
    /**
     * Finds a path from a creature to a specific location.
     * 
     * @param pathOwner The creature finding the path
     * @param x Target X coordinate
     * @param y Target Y coordinate
     * @param z Target Z coordinate
     * @return Array of waypoints, or null if no path found
     */
    public float[][] navigateToLocation(Creature pathOwner, float x, float y, float z) {
        if (pathOwner == null) return null;
        if (pathOwner.getLifeStats().isAlreadyDead()) return null;
        
        int worldId = pathOwner.getWorldId();
        float x1 = pathOwner.getX(), y1 = pathOwner.getY(), z1 = pathOwner.getZ();
        
        return navigateFromLocationToLocation(worldId, null, null, x1, y1, z1, x, y, z);
    }
    
    /**
     * Core pathfinding algorithm.
     */
    private float[][] navigateFromLocationToLocation(int worldId, NavGeometry tile1, NavGeometry tile2,
                                                     float x1, float y1, float z1,
                                                     float x2, float y2, float z2) {
        // TODO: Implement A* pathfinding with corridor smoothing
        return null;
    }
    
    /**
     * Gets the navigation tile at the specified location using ray casting.
     */
    private NavGeometry getNavTile(int worldId, float x, float y, float z) {
        GeoMap navMap = navData.getNavMap(worldId);
        if (navMap == null) return null;
        
        Vector3f pos = Vector3f.newInstance().set(x, y, z + NavConstants.NAV_RAY_Z_OFFSET);
        Vector3f dir = Vector3f.newInstance().set(0, 0, -1);
        Ray ray = new Ray(pos, dir);
        ray.setLimit(NavConstants.NAV_RAY_LIMIT);
        
        CollisionResults results = new CollisionResults((byte) 1, false, 0);
        int collisionCount = navMap.collideWith(ray, results);
        
        Vector3f.recycle(pos);
        Vector3f.recycle(dir);
        
        if (collisionCount == 0) return null;
        
        Spatial ret = results.getClosestCollision().getGeometry();
        if (ret instanceof NavGeometry) {
            return (NavGeometry) ret;
        }
        return null;
    }
    
    /**
     * Gets the navigation tile using a bounding box search.
     */
    private NavGeometry getNavTileWithBox(int worldId, float x, float y, float z) {
        GeoMap navMap = navData.getNavMap(worldId);
        if (navMap == null) return null;
        
        Vector3f min = Vector3f.newInstance().set(
            x - NavConstants.NAV_BOX_X_EXTENT,
            y - NavConstants.NAV_BOX_Y_EXTENT,
            z + NavConstants.NAV_BOX_Z_MIN
        );
        Vector3f max = Vector3f.newInstance().set(
            x + NavConstants.NAV_BOX_X_EXTENT,
            y + NavConstants.NAV_BOX_Y_EXTENT,
            z + NavConstants.NAV_BOX_Z_MAX
        );
        Vector3f center = Vector3f.newInstance().set(
            x, y, z + NavConstants.NAV_BOX_CENTER_Z_OFFSET
        );
        
        BoundingBox box = new BoundingBox(min, max);
        box.setCenter(center);
        
        CollisionResults results = new CollisionResults((byte) 1, false, 0);
        int collisionCount = navMap.collideWith(box, results);
        
        Vector3f.recycle(min);
        Vector3f.recycle(max);
        Vector3f.recycle(center);
        
        if (collisionCount == 0) return null;
        
        Spatial ret = results.getClosestCollision().getGeometry();
        if (ret instanceof NavGeometry) {
            return (NavGeometry) ret;
        }
        return null;
    }
    
    /**
     * Gets the singleton instance.
     */
    public static final NavService getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private static final class SingletonHolder {
        protected static final NavService INSTANCE = new NavService();
    }
}
