# Geo-Source-Elenor Upgrade Guide

## From Baseline to AionEncomBase Standards

**Date**: 2026-04-23  
**Version**: 1.0  
**Target**: geo-source-elenor  
**Source**: AionEncomBase_Java8  

---

## 📊 Overview

This guide documents the systematic upgrade of geo-source-elenor to match the advanced features and best practices from AionEncomBase.

### Migration Phases

| Phase | Timeline | Focus | Status |
|-------|----------|-------|--------|
| **Phase 1** | Week 1 | Constants & Configuration | ✅ IN PROGRESS |
| **Phase 2** | Week 2-3 | Collision System (BIH) | ⬜ NEXT |
| **Phase 3** | Week 4-5 | Navigation System | ⬜ PLANNED |
| **Phase 4** | Week 6-8 | Testing & Optimization | ⬜ PLANNED |

---

## Phase 1: Constants & Configuration ✅ IN PROGRESS

### What's New

#### 1. **FastMathConstants.java**
- Centralizes all magic numbers from codebase
- Provides semantic meaning through naming
- Enables easy configuration tuning

**Before** (Scattered throughout codebase):
```java
center.multLocal(0.33333334f);  // Magic number - what is this?
return dist < 1.1920929E-7f;     // Epsilon value - but which type?
Vector3f pos = new Vector3f(x, y, z + 2.0f);  // What's the 2.0f offset?
```

**After** (Using FastMathConstants):
```java
center.multLocal(FastMathConstants.ONE_THIRD);
return dist < FastMathConstants.FLT_EPSILON;
Vector3f pos = new Vector3f(x, y, z + FastMathConstants.COLLISION_Z_OFFSET);
```

#### 2. **BIHConstants.java**
- Configuration for Bounding Interval Hierarchy collision system
- Optimization parameters for tree construction
- Performance tuning knobs

#### 3. **NavConstants.java**
- Pathfinding algorithm parameters
- Navigation mesh constants
- Funnel algorithm tuning

### Migration Steps

1. ✅ Create constant classes
2. ⬜ Replace hardcoded values in existing code:
   ```bash
   # Files to update:
   src/com/aionemu/gameserver/geoEngine/models/GeoMap.java
   src/com/aionemu/gameserver/geoEngine/math/Plane.java
   src/com/aionemu/gameserver/geoEngine/math/Triangle.java
   src/com/aionemu/gameserver/geoEngine/math/Ray.java
   src/eleanor/processors/movement/motor/FollowMotor.java
   ```

### Refactoring Examples

**Triangle.java** - Line 148:
```java
// Before
this.center.addLocal(this.pointb).addLocal(this.pointc).multLocal(0.33333334f);

// After
import static com.aionemu.gameserver.geoEngine.math.FastMathConstants.*;
this.center.addLocal(this.pointb).addLocal(this.pointc).multLocal(ONE_THIRD);
```

**Plane.java** - Line 83:
```java
// Before
return dist < 1.1920929E-7f && dist > -1.1920929E-7f;

// After
import static com.aionemu.gameserver.geoEngine.math.FastMathConstants.*;
return dist < FLT_EPSILON && dist > -FLT_EPSILON;
```

**GeoMap.java** - Line 160:
```java
// Before
Vector3f pos = new Vector3f(x, y, z + 2.0f);
Vector3f dir = new Vector3f(x, y, z - 100.0f);

// After
import static com.aionemu.gameserver.world.geo.nav.NavConstants.*;
Vector3f pos = new Vector3f(x, y, z + COLLISION_Z_OFFSET);
Vector3f dir = new Vector3f(x, y, z - COLLISION_Z_DEPTH);
```

---

## Phase 2: Collision System (BIH) ⬜ NEXT

### What's BIH (Bounding Interval Hierarchy)?

A spatial acceleration structure that provides:
- **Fast ray casting**: O(log n) vs O(n) linear search
- **Cache efficiency**: Better memory access patterns
- **Scalability**: Handles 1M+ triangles efficiently

### Implementation Plan

1. Create BIHTree.java (port from AionEncomBase)
2. Create BIHNode.java (tree node structure)
3. Create TriangleAxisComparator.java (sorting)
4. Integrate with existing collision system
5. Performance testing & benchmarking

### Expected Performance Improvements

| Operation | Before | After | Improvement |
|-----------|--------|-------|-------------|
| Ray-mesh intersection | ~5ms (1K tris) | ~0.5ms | **10x faster** |
| Terrain Z lookup | ~3ms | ~0.3ms | **10x faster** |
| Path validation | ~8ms | ~0.8ms | **10x faster** |

---

## Phase 3: Navigation System ⬜ PLANNED

### NavService Framework

Implement advanced pathfinding:
- A* algorithm on navigation mesh
- Corridor-based path smoothing (funnel algorithm)
- Direct line-of-sight validation
- Soft caching with garbage collection

### Key Methods

```java
public float[][] navigateToTarget(Creature creature, Creature target)
public float[][] navigateToLocation(Creature creature, float x, float y, float z)
public boolean canPullTarget(Creature creature, Creature target)
```

### Benefits

- More realistic NPC movement
- AI can navigate around obstacles
- Supports game mechanics (pulling, force move)
- Configurable pathfinding cost

---

## Phase 4: Testing & Optimization ⬜ PLANNED

### Testing Strategy

1. **Unit Tests**
   - FastMath constant validation
   - BIH tree construction
   - Ray-triangle intersection

2. **Integration Tests**
   - Full pathfinding pipeline
   - Multi-map navigation
   - Performance under load

3. **Stress Tests**
   - 1000+ concurrent entities
   - Large geographic meshes
   - Memory profiling

### Performance Targets

- Collision queries: < 1ms average
- Pathfinding: < 10ms for 200-node path
- Memory usage: < 500MB for full geo data
- GC pauses: < 50ms

---

## 📚 File Structure After Upgrade

```
src/com/aionemu/gameserver/geoEngine/
├── math/
│   ├── FastMathConstants.java       ✅ NEW
│   ├── FastMath.java
│   ├── Vector2f.java
│   ├── Vector3f.java
│   ├── Plane.java                   ⬜ WILL REFACTOR
│   ├── Ray.java                     ⬜ WILL REFACTOR
│   ├── Triangle.java                ⬜ WILL REFACTOR
│   ├── Matrix3f.java
│   └── Matrix4f.java
├── collision/
│   ├── bih/
│   │   ├── BIHConstants.java        ✅ NEW
│   │   ├── BIHTree.java             ⬜ TODO
│   │   ├── BIHNode.java             ⬜ TODO
│   │   └── TriangleAxisComparator.java ⬜ TODO
│   ├── CollisionResults.java
│   └── ...
├── models/
│   └── GeoMap.java
└── scene/
    ├── NavGeometry.java
    └── ...

world/geo/
├── nav/
│   ├── NavConstants.java            ✅ NEW
│   ├── NavService.java              ✅ NEW (stub)
│   ├── NavData.java                 ⬜ TODO
│   ├── NavHelper.java               ⬜ TODO
│   └── ...
├── GeoService.java
└── ...
```

---

## 🔧 Configuration

### GeoDataConfig.java (Update Required)

```java
public class GeoDataConfig {
    // Existing
    public static boolean GEO_ENABLE = true;
    
    // NEW - Add these
    public static boolean GEO_USE_BIH = true;           // Enable BIH acceleration
    public static boolean GEO_NAV_ENABLE = true;        // Enable navigation system
    public static boolean GEO_NAV_SOFT_CACHE = true;    // Use soft references
    public static int GEO_NAV_CACHE_SIZE = 1000;        // Max cached tiles
    public static float GEO_NAV_HEURISTIC_WEIGHT = 1.0f;// A* heuristic tuning
}
```

---

## 📈 Metrics to Track

### Before vs After

Create performance benchmarks:

```bash
# Collision Performance
benchmark.raycast.time_ms
benchmark.terrain.lookup_ms
benchmark.path.validation_ms

# Memory Usage
benchmark.memory.geo_data_mb
benchmark.memory.nav_cache_mb
benchmark.memory.peak_usage_mb

# Quality Metrics
benchmark.pathfinding.success_rate
benchmark.pathfinding.path_length
benchmark.movement.smoothness
```

---

## ✅ Checklist

### Phase 1
- [x] Create FastMathConstants.java
- [x] Create BIHConstants.java
- [x] Create NavConstants.java
- [x] Create NavService.java (stub)
- [ ] Update GeoMap.java to use constants
- [ ] Update Plane.java to use constants
- [ ] Update Triangle.java to use constants
- [ ] Update Ray.java to use constants
- [ ] Update FollowMotor.java to use constants
- [ ] Update GeoDataConfig.java with new settings

### Phase 2
- [ ] Create BIHTree.java
- [ ] Create BIHNode.java
- [ ] Create TriangleAxisComparator.java
- [ ] Integrate BIH with GeoMap
- [ ] Performance testing
- [ ] Memory profiling

### Phase 3
- [ ] Complete NavService.java implementation
- [ ] Create NavData.java with lazy loading
- [ ] Create NavHelper.java for A* algorithm
- [ ] Create NavGeometry interface
- [ ] Implement funnel algorithm
- [ ] Path smoothing

### Phase 4
- [ ] Write unit tests
- [ ] Write integration tests
- [ ] Stress testing
- [ ] Performance optimization
- [ ] Documentation

---

## 🎓 Learning Resources

1. **Bounding Interval Hierarchy**
   - https://github.com/MATTYOneInc/AionEncomBase_Java8/blob/main/AL-Game/src/com/aionemu/gameserver/geoEngine/collision/bih/BIHTree.java
   - Fast Collision Detection Using Bounding Volume Hierarchies

2. **Navigation Mesh Pathfinding**
   - https://github.com/MATTYOneInc/AionEncomBase_Java8/blob/main/AL-Game/src/com/aionemu/gameserver/world/geo/nav/NavService.java
   - Funnel algorithm for path smoothing
   - A* on polygonal meshes

3. **Performance Optimization**
   - Profile-guided optimization
   - Cache-aware algorithms
   - Memory pooling strategies

---

## 📞 Support

- GitHub Issues: [geo-source-elenor/issues](https://github.com/SixShoot/geo-source-elenor/issues)
- Reference: [AionEncomBase AL-Game](https://github.com/MATTYOneInc/AionEncomBase_Java8/tree/main/AL-Game)

---

**Last Updated**: 2026-04-23  
**Next Review**: After Phase 1 Completion
