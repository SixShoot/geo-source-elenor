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
2. ⬜ Replace hardcoded values in existing code

### Expected Performance Improvements

| Operation | Before | After | Improvement |
|-----------|--------|-------|-------------|
| Ray-mesh intersection | ~5ms (1K tris) | ~0.5ms | **10x faster** |
| Terrain Z lookup | ~3ms | ~0.3ms | **10x faster** |
| Path validation | ~8ms | ~0.8ms | **10x faster** |

---

## Phase 2: Collision System (BIH) ⬜ NEXT

### What's BIH (Bounding Interval Hierarchy)?

A spatial acceleration structure that provides:
- **Fast ray casting**: O(log n) vs O(n) linear search
- **Cache efficiency**: Better memory access patterns
- **Scalability**: Handles 1M+ triangles efficiently

---

## Phase 3: Navigation System ⬜ PLANNED

### NavService Framework

Implement advanced pathfinding:
- A* algorithm on navigation mesh
- Corridor-based path smoothing (funnel algorithm)
- Direct line-of-sight validation
- Soft caching with garbage collection

---

## Phase 4: Testing & Optimization ⬜ PLANNED

### Performance Targets

- Collision queries: < 1ms average
- Pathfinding: < 10ms for 200-node path
- Memory usage: < 500MB for full geo data
- GC pauses: < 50ms

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

### Phase 2
- [ ] Create BIHTree.java
- [ ] Create BIHNode.java
- [ ] Create TriangleAxisComparator.java
- [ ] Integrate BIH with GeoMap
- [ ] Performance testing

### Phase 3
- [ ] Complete NavService.java implementation
- [ ] Create NavData.java with lazy loading
- [ ] Create NavHelper.java for A* algorithm
- [ ] Implement funnel algorithm

### Phase 4
- [ ] Write unit tests
- [ ] Stress testing
- [ ] Performance optimization

---

## 📞 Support

- GitHub: [geo-source-elenor](https://github.com/SixShoot/geo-source-elenor)
- Reference: [AionEncomBase AL-Game](https://github.com/MATTYOneInc/AionEncomBase_Java8/tree/main/AL-Game)

---

**Last Updated**: 2026-04-23  
**Next Review**: After Phase 1 Completion
