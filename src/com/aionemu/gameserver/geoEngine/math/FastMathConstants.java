package com.aionemu.gameserver.geoEngine.math;

/**
 * Centralized math constants extracted from magic numbers throughout the codebase.
 * Ported from AionEncomBase with enhancements for geo-source-elenor.
 * 
 * These constants should be used instead of hardcoded values for:
 * - Epsilon values for floating-point comparisons
 * - Trigonometric constants
 * - Collision tolerances
 * - Geometric calculations
 * 
 * @author AION Team / SixShoot
 * @version 1.0
 */
public final class FastMathConstants {
    // Floating-point precision constants
    public static final double DBL_EPSILON = 2.220446049250313E-16;
    public static final float FLT_EPSILON = 1.1920929E-7f;
    public static final float ZERO_TOLERANCE = 1.0E-4f;
    
    // Common multipliers
    public static final float ONE_THIRD = 0.33333334f;
    public static final float TWO_THIRDS = 0.66666667f;
    
    // Trigonometric constants
    public static final float PI = (float) Math.PI;
    public static final float TWO_PI = (float) Math.PI * 2;
    public static final float HALF_PI = 1.5707964f;
    public static final float QUARTER_PI = 0.7853982f;
    public static final float INV_PI = 0.31830987f;
    public static final float INV_TWO_PI = 0.15915494f;
    
    // Angle conversion constants
    public static final float DEG_TO_RAD = (float) Math.PI / 180;
    public static final float RAD_TO_DEG = 57.295776f;
    
    // Collision system constants
    public static final float COLLISION_Z_OFFSET = 2.0f;
    public static final float COLLISION_Z_DEPTH = 100.0f;
    public static final float COLLISION_TOLERANCE = 0.001f;
    
    // Movement constants
    public static final float MOVEMENT_OFFSET = 0.05f;
    public static final float MOVEMENT_CHECK_OFFSET = 0.1f;
    
    // Geometric calculation constants
    public static final float TRIANGLE_CENTER_FACTOR = ONE_THIRD;
    public static final float BOX_INTERIOR_THRESHOLD = 0.8f;
    public static final float BOX_VERTICAL_THRESHOLD = 1.0f;
    public static final float BOX_VERTICAL_OFFSET = 4.0f;
    public static final float BOX_CENTER_OFFSET = 0.2f;
    
    // Ray casting constants
    public static final float RAY_LIMIT_SHORT = 5.0f;
    public static final float RAY_LIMIT_MEDIUM = 100.0f;
    public static final float RAY_LIMIT_LONG = 1000.0f;
    
    private FastMathConstants() {
        throw new AssertionError("FastMathConstants cannot be instantiated");
    }
    
    /**
     * Validates a float value is within reasonable bounds.
     * @param value the value to check
     * @return true if the value is finite and not NaN
     */
    public static boolean isValid(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }
    
    /**
     * Checks if a float value is close to zero using ZERO_TOLERANCE.
     * @param value the value to check
     * @return true if the value is near zero
     */
    public static boolean isNearZero(float value) {
        return Math.abs(value) < ZERO_TOLERANCE;
    }
    
    /**
     * Checks if two float values are approximately equal using FLT_EPSILON.
     * @param a first value
     * @param b second value
     * @return true if values are approximately equal
     */
    public static boolean approximately(float a, float b) {
        float diff = Math.abs(a - b);
        return diff < FLT_EPSILON || diff < Math.max(Math.abs(a), Math.abs(b)) * FLT_EPSILON;
    }
}
