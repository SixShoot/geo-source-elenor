package com.aionemu.gameserver.geoEngine.models;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.bounding.BoundingBox;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.CollisionResult;
import com.aionemu.gameserver.geoEngine.collision.CollisionResults;
import com.aionemu.gameserver.geoEngine.math.Ray;
import com.aionemu.gameserver.geoEngine.math.Triangle;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.geoEngine.scene.Node;
import com.aionemu.gameserver.geoEngine.scene.Spatial;
import com.aionemu.gameserver.geoEngine.scene.mesh.DoorGeometry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeoMap
extends Node {
    private static Logger _log = LoggerFactory.getLogger(GeoMap.class);
    private short[] terrainData;
    private List<BoundingBox> tmpBox = new ArrayList<BoundingBox>();
    private Map<String, DoorGeometry> doors = new FastMap();

    public GeoMap(String name, int worldSize) {
        this.setCollisionFlags((short)(CollisionIntention.ALL.getId() << 8));
        for (int x = 0; x < worldSize; x += 256) {
            for (int y = 0; y < worldSize; y += 256) {
                Node geoNode = new Node("");
                geoNode.setCollisionFlags((short)(CollisionIntention.ALL.getId() << 8));
                this.tmpBox.add(new BoundingBox(new Vector3f(x, y, 0.0f), new Vector3f(x + 256, y + 256, 4000.0f)));
                super.attachChild(geoNode);
            }
        }
    }

    public String getDoorName(int worldId, String meshFile, float x, float y, float z) {
        if (!GeoDataConfig.GEO_DOORS_ENABLE) {
            return null;
        }
        String mesh = meshFile.toUpperCase();
        Vector3f templatePoint = new Vector3f(x, y, z);
        float distance = Float.MAX_VALUE;
        DoorGeometry foundDoor = null;
        for (Map.Entry<String, DoorGeometry> door : this.doors.entrySet()) {
            if (!door.getKey().startsWith(Integer.toString(worldId)) || !door.getKey().endsWith(mesh)) continue;
            DoorGeometry checkDoor = this.doors.get(door.getKey());
            float doorDistance = checkDoor.getWorldBound().distanceTo(templatePoint);
            if (distance > doorDistance) {
                distance = doorDistance;
                foundDoor = checkDoor;
            }
            if (!checkDoor.getWorldBound().intersects(templatePoint)) continue;
            foundDoor = checkDoor;
            break;
        }
        if (foundDoor == null) {
            _log.warn("Could not find static door: " + worldId + " " + meshFile + " " + templatePoint);
            return null;
        }
        foundDoor.setFoundTemplate(true);
        return foundDoor.getName();
    }

    public void setDoorState(int instanceId, String name, boolean isOpened) {
        DoorGeometry door = this.doors.get(name);
        if (door != null) {
            door.setDoorState(instanceId, isOpened);
        }
    }

    @Override
    public int attachChild(Spatial child) {
        int i = 0;
        if (child instanceof DoorGeometry) {
            this.doors.put(child.getName(), (DoorGeometry)child);
        }
        for (Spatial spatial : this.getChildren()) {
            if (this.tmpBox.get(i).intersects(child.getWorldBound())) {
                ((Node)spatial).attachChild(child);
            }
            ++i;
        }
        return 0;
    }

    public void setTerrainData(short[] terrainData) {
        this.terrainData = terrainData;
    }

    public float getZ(float x, float y) {
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), false, 1);
        Vector3f pos = new Vector3f(x, y, 4000.0f);
        Vector3f dir = new Vector3f(x, y, 0.0f);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        this.collideWith(r, results);
        Vector3f terrain = null;
        terrain = this.terrainData.length == 1 ? new Vector3f(x, y, (float)this.terrainData[0] / 32.0f) : this.terraionCollision(x, y, r);
        if (terrain != null) {
            CollisionResult result = new CollisionResult(terrain, Math.max(0.0f, Math.max(4000.0f - terrain.z, terrain.z)));
            results.addCollision(result);
        }
        if (results.size() == 0) {
            return 0.0f;
        }
        return results.getClosestCollision().getContactPoint().z;
    }

    public float getZW(float x, float y) {
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), true, 1);
        Vector3f pos = new Vector3f(x, y, 4000.0f);
        Vector3f dir = new Vector3f(x, y, 0.0f);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        this.collideWith(r, results);
        Vector3f terrain = null;
        terrain = this.terrainData.length == 1 ? new Vector3f(x, y, (float)this.terrainData[0] / 32.0f) : this.terraionCollision(x, y, r);
        if (terrain != null) {
            CollisionResult result = new CollisionResult(terrain, Math.max(0.0f, Math.max(4000.0f - terrain.z, terrain.z)));
            results.addCollision(result);
        }
        if (results.size() == 0) {
            return 0.0f;
        }
        return results.getClosestCollision().getContactPoint().z;
    }

    public float getZ(float x, float y, float z, int instanceId) {
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), false, instanceId);
        Vector3f pos = new Vector3f(x, y, z + 2.0f);
        Vector3f dir = new Vector3f(x, y, z - 100.0f);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        this.collideWith(r, results);
        Vector3f terrain = null;
        if (this.terrainData.length == 1) {
            if (this.terrainData[0] != 0) {
                terrain = new Vector3f(x, y, (float)this.terrainData[0] / 32.0f);
            }
        } else {
            terrain = this.terraionCollision(x, y, r);
        }
        if (terrain != null && terrain.z > 0.0f && terrain.z < z + 2.0f) {
            CollisionResult result = new CollisionResult(terrain, Math.abs(z - terrain.z + 2.0f));
            results.addCollision(result);
        }
        if (results.size() == 0) {
            return z;
        }
        return results.getClosestCollision().getContactPoint().z;
    }

    public float getZW(float x, float y, float z, int instanceId) {
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), true, instanceId);
        Vector3f pos = new Vector3f(x, y, z + 2.0f);
        Vector3f dir = new Vector3f(x, y, z - 100.0f);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        this.collideWith(r, results);
        Vector3f terrain = null;
        if (this.terrainData.length == 1) {
            if (this.terrainData[0] != 0) {
                terrain = new Vector3f(x, y, (float)this.terrainData[0] / 32.0f);
            }
        } else {
            terrain = this.terraionCollision(x, y, r);
        }
        if (terrain != null && terrain.z > 0.0f && terrain.z < z + 2.0f) {
            CollisionResult result = new CollisionResult(terrain, Math.abs(z - terrain.z + 2.0f));
            results.addCollision(result);
        }
        if (results.size() == 0) {
            return z;
        }
        return results.getClosestCollision().getContactPoint().z;
    }

    public Vector3f getClosestCollision(float x, float y, float z, float targetX, float targetY, float targetZ, boolean changeDirection, boolean fly, int instanceId, byte intentions) {
        float zChecked1 = 0.0f;
        float zChecked2 = 0.0f;
        if (!fly && changeDirection) {
            zChecked1 = z;
            z = this.getZ(x, y, z + 2.0f, instanceId);
        }
        Vector3f start = new Vector3f(x, y, z += 1.0f);
        Vector3f end = new Vector3f(targetX, targetY, targetZ += 1.0f);
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f dir = new Vector3f(targetX, targetY, targetZ);
        CollisionResults results = new CollisionResults(intentions, false, instanceId);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        Vector3f terrain = this.calculateTerrainCollision(start.x, start.y, start.z, end.x, end.y, end.z, r);
        if (terrain != null) {
            CollisionResult result = new CollisionResult(terrain, terrain.distance(pos));
            results.addCollision(result);
        }
        this.collideWith(r, results);
        float geoZ = 0.0f;
        if (results.size() == 0) {
            if (fly) {
                return end;
            }
            if (zChecked1 > 0.0f && targetX == x && targetY == y && targetZ - 1.0f == zChecked1) {
                geoZ = z - 1.0f;
            } else {
                zChecked2 = targetZ;
                geoZ = this.getZ(targetX, targetY, targetZ + 2.0f, instanceId);
            }
            if (Math.abs(geoZ - targetZ) < start.distance(end)) {
                return end.setZ(geoZ);
            }
            return start;
        }
        Vector3f contactPoint = results.getClosestCollision().getContactPoint();
        float distance = results.getClosestCollision().getDistance();
        if (distance < 1.0f) {
            return start;
        }
        contactPoint = contactPoint.subtract(dir);
        if (!fly && changeDirection) {
            contactPoint.z = zChecked1 > 0.0f && contactPoint.x == x && contactPoint.y == y && contactPoint.z == zChecked1 ? z - 1.0f : (zChecked2 > 0.0f && contactPoint.x == targetX && contactPoint.y == targetY && contactPoint.z == zChecked2 ? geoZ : this.getZ(contactPoint.x, contactPoint.y, contactPoint.z + 2.0f, instanceId));
        }
        if (!fly && Math.abs(start.z - contactPoint.z) > distance) {
            return start;
        }
        return contactPoint;
    }

    public CollisionResults getCollisions(float x, float y, float z, float targetX, float targetY, float targetZ, boolean changeDirection, boolean fly, int instanceId, byte intentions) {
        if (!fly && changeDirection) {
            z = this.getZ(x, y, z + 2.0f, instanceId);
        }
        Vector3f start = new Vector3f(x, y, z += 1.0f);
        Vector3f end = new Vector3f(targetX, targetY, targetZ += 1.0f);
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f dir = new Vector3f(targetX, targetY, targetZ);
        CollisionResults results = new CollisionResults(intentions, false, instanceId);
        Float limit = Float.valueOf(pos.distance(dir));
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit.floatValue());
        Vector3f terrain = this.calculateTerrainCollision(start.x, start.y, start.z, end.x, end.y, end.z, r);
        if (terrain != null) {
            CollisionResult result = new CollisionResult(terrain, terrain.distance(pos));
            results.addCollision(result);
        }
        this.collideWith(r, results);
        return results;
    }

    private Vector3f calculateTerrainCollision(float x, float y, float z, float targetX, float targetY, float targetZ, Ray ray) {
        float x2 = targetX - x;
        float y2 = targetY - y;
        int intD = (int)Math.abs(ray.getLimit());
        for (float s = 0.0f; s < (float)intD; s += 2.0f) {
            float tempY;
            float tempX = x + x2 * s / ray.getLimit();
            Vector3f result = this.terraionCollision(tempX, tempY = y + y2 * s / ray.getLimit(), ray);
            if (result == null) continue;
            return result;
        }
        return null;
    }

    private Vector3f terraionCollision(float x, float y, Ray ray) {
        Triangle tringle2;
        Triangle tringle1;
        float p1;
        float p2;
        float p3;
        float p4;
        int xInt = (int)(x /= 2.0f);
        int yInt = (int)(y /= 2.0f);
        if (this.terrainData.length == 1) {
            p3 = p4 = (float)this.terrainData[0] / 32.0f;
            p2 = p4;
            p1 = p4;
        } else {
            int size = (int)Math.sqrt(this.terrainData.length);
            try {
                p1 = (float)this.terrainData[yInt + xInt * size] / 32.0f;
                p2 = (float)this.terrainData[yInt + 1 + xInt * size] / 32.0f;
                p3 = (float)this.terrainData[yInt + (xInt + 1) * size] / 32.0f;
                p4 = (float)this.terrainData[yInt + 1 + (xInt + 1) * size] / 32.0f;
            }
            catch (Exception e) {
                return null;
            }
        }
        Vector3f result = new Vector3f();
        if (p1 >= 0.0f && p2 >= 0.0f && p3 >= 0.0f && ray.intersectWhere(tringle1 = new Triangle(new Vector3f(xInt * 2, yInt * 2, p1), new Vector3f(xInt * 2, (yInt + 1) * 2, p2), new Vector3f((xInt + 1) * 2, yInt * 2, p3)), result)) {
            return result;
        }
        if (p4 >= 0.0f && p2 >= 0.0f && p3 >= 0.0f && ray.intersectWhere(tringle2 = new Triangle(new Vector3f((xInt + 1) * 2, (yInt + 1) * 2, p4), new Vector3f(xInt * 2, (yInt + 1) * 2, p2), new Vector3f((xInt + 1) * 2, yInt * 2, p3)), result)) {
            return result;
        }
        return null;
    }

    public boolean canSee(float x, float y, float z, float targetX, float targetY, float targetZ, float limit, int instanceId) {
        targetZ += 1.0f;
        z += 1.0f;
        float x2 = x - targetX;
        float y2 = y - targetY;
        float distance = (float)Math.sqrt(x2 * x2 + y2 * y2);
        if (distance > 80.0f) {
            return false;
        }
        int intD = (int)Math.abs(distance);
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f dir = new Vector3f(targetX, targetY, targetZ);
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit);
        for (float s = 2.0f; s < (float)intD; s += 2.0f) {
            float tempX = targetX + x2 * s / distance;
            float tempY = targetY + y2 * s / distance;
            Vector3f result = this.terraionCollision(tempX, tempY, r);
            if (result == null) continue;
            return false;
        }
        CollisionResults results = new CollisionResults((byte)(CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId()), false, instanceId);
        int collisions = this.collideWith(r, results);
        return results.size() == 0 && collisions == 0;
    }

    public boolean canPass(float x, float y, float z, float targetX, float targetY, float targetZ, float limit, int instanceId) {
        float x2 = x - targetX;
        float y2 = y - targetY;
        float distance = (float)Math.sqrt(x2 * x2 + y2 * y2);
        if (distance > 65.0f) {
            return false;
        }
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f dir = new Vector3f(targetX, targetY, targetZ);
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit);
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), false, instanceId);
        int collisions = this.collideWith(r, results);
        return results.size() == 0 && collisions == 0;
    }

    public boolean canPassWalker(float x, float y, float z, float targetX, float targetY, float targetZ, float limit, int instanceId) {
        float x2 = x - targetX;
        float y2 = y - targetY;
        float distance = (float)Math.sqrt(x2 * x2 + y2 * y2);
        if (distance > 50.0f) {
            return false;
        }
        Vector3f pos = new Vector3f(x, y, z);
        Vector3f dir = new Vector3f(targetX, targetY, targetZ);
        dir.subtractLocal(pos).normalizeLocal();
        Ray r = new Ray(pos, dir);
        r.setLimit(limit);
        CollisionResults results = new CollisionResults(CollisionIntention.PHYSICAL.getId(), true, instanceId);
        int collisions = this.collideWith(r, results);
        return results.size() == 0 && collisions == 0;
    }

    @Override
    public void updateModelBound() {
        if (this.getChildren() != null) {
            Iterator<Spatial> i = this.getChildren().iterator();
            while (i.hasNext()) {
                Spatial s = i.next();
                if (!(s instanceof Node) || !((Node)s).getChildren().isEmpty()) continue;
                i.remove();
            }
        }
        super.updateModelBound();
    }
}