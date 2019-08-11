package jagd;

import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This class provides methods for calculating Field of View in grids. Field of
 * View (FOV) algorithms determine how much area surrounding a point can be
 * seen. They return a two dimensional array of doubles, representing the amount
 * of view (typically sight, but perhaps sound, smell, etc.) which the origin
 * has of each cell.
 * <br>
 * The input resistanceMap is considered the percent of opacity. This resistance
 * is on top of the resistance applied from the light spreading out. You can
 * obtain a resistance map easily with the DungeonUtility.generateResistances()
 * method, which uses defaults for common chars used in SquidLib, but you may
 * also want to create a resistance map manually if a given char means something
 * very different in your game. This is easy enough to do by looping over all the
 * x,y positions in your char[][] map and running a switch statement on each char,
 * assigning a double to the same x,y position in a double[][]. The value should
 * be between 0.0 (unblocked) for things light passes through, 1.0 (blocked) for
 * things light can't pass at all, and possibly other values if you have
 * translucent obstacles.
 * <br>
 * The returned light map is considered the percent of light in the cells.
 * <br>
 * Currently, all implementations provide percentage levels of light from 0.0
 * (unlit) to 1.0 (fully lit).
 * <br>
 * All solvers perform bounds checking so solid borders in the map are not
 * required.
 * <br>
 * Static methods are provided to add together FOV maps in the simple way
 * (disregarding visibility of distant FOV from a given cell), or the more
 * practical way for roguelikes (where a cell needs to be within line-of-sight
 * in the first place for a distant light to illuminate it). The second method
 * relies on an LOS map, which is essentially the same as a very-high-radius
 * FOV map and can be easily obtained with calculateLOSMap().
 * <br>
 * If you want to iterate through cells that are visible in a double[][] returned
 * by FOV, you can pass that double[][] to the constructor for Region, and you
 * can use the Region as a reliably-ordered List of GridPoint2 (among other things).
 * The order Region iterates in is somewhat strange, and doesn't, for example,
 * start at the center of an FOV map, but it will be the same every time you
 * create a Region with the same FOV map (or the same visible GridPoint2s).
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class FOV {
    protected static final GridPoint2[] 
            ccw_full = new GridPoint2[]{new GridPoint2(1, 0), new GridPoint2(1, 1), new GridPoint2(0, 1), new GridPoint2(-1, 1),
            new GridPoint2(-1, 0), new GridPoint2(-1, -1), new GridPoint2(0, -1), new GridPoint2(1, -1)};

    /**
     * Unneeded.
     */
    protected FOV() {
    }

    public static void fill(double[][] array, double value)
    {
        for (int i = 0; i < array.length; i++) {
            Arrays.fill(array[i], value);
        }
    }
    public static double radius (double x1, double y1, double x2, double y2) {
        final double x_d = x2 - x1;
        final double y_d = y2 - y1;
        return Math.sqrt(x_d * x_d + y_d * y_d);
    }
    public static double radiusSquared (double x1, double y1, double x2, double y2) {
        final double x_d = x2 - x1;
        final double y_d = y2 - y1;
        return x_d * x_d + y_d * y_d;
    }
    public static double radius (double x, double y) {
        return Math.sqrt(x * x + y * y);
    }
    public static double radiusSquared (double x, double y) {
        return x * x + y * y;
    }

    /**
     * Altered-range approximation of the frequently-used trigonometric method atan2, taking y and x positions as 
     * doubles and returning an angle measured in turns from 0.0 to 1.0 (inclusive), with one cycle over the range
     * equivalent to 360 degrees or 2PI radians. You can multiply the angle by {@code 6.2831855f} to change to radians,
     * or by {@code 360f} to change to degrees. Takes y and x (in that unusual order) as doubles. Will never return a
     * negative number, which may help avoid costly floating-point modulus when you actually want a positive number.
     * Credit to StackExchange user njuffa, who gave
     * <a href="https://math.stackexchange.com/a/1105038">this useful answer</a>. Note that
     * {@link Math#atan2(double, double)} returns an angle in radians and can return negative results, which may be fine
     * for many tasks; this method should be much faster but isn't quite as precise.
     * @param y y-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @param x x-component of the point to find the angle towards; note the parameter order is unusual by convention
     * @return the angle to the given point, as a double from 0.0 to 1.0, inclusive
     */
    public static double atan2_(final double y, final double x)
    {
        if(y == 0.0 && x >= 0.0) return 0.0;
        final double ax = Math.abs(x), ay = Math.abs(y);
        if(ax < ay)
        {
            final double a = ax / ay, s = a * a,
                    r = 0.25 - (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
        else {
            final double a = ay / ax, s = a * a,
                    r = (((-0.0464964749 * s + 0.15931422) * s - 0.327622764) * s * a + a) * 0.15915494309189535;
            return (x < 0.0) ? (y < 0.0) ? 0.5 + r : 0.5 - r : (y < 0.0) ? 1.0 - r : r;
        }
    }


    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter.  The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations. The light will be treated as having infinite possible
     * radius.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light a non-null 2D double array that will have its contents overwritten, modified, and returned
     * @param startx the horizontal component of the starting location
     * @param starty the vertical component of the starting location
     * @return the computed light grid (the same as {@code light})
     */
    public static double[][] calculateFOV(double[][] resistanceMap, double[][] light, int startx, int starty) {
        return reuseFOV(resistanceMap, light, startx, starty, Integer.MAX_VALUE);
    }

    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations based on Euclidean
     * calculations.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY, double radius)
    {
        double decay = 1.0 / radius;
        fill(light, 0);
        light[startX][startY] = Math.min(1.0, radius);//make the starting space full power unless radius is tiny


        shadowCast(0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap);
        shadowCast(1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap);

        shadowCast(0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap);
        shadowCast(1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap);

        shadowCast(0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap);
        shadowCast(-1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap);

        shadowCast(0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap);
        shadowCast(-1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap);
        return light;
    }
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates. Assigns to, and returns, a light map where the values
     * represent a percentage of fully lit. Always uses shadowcasting FOV,
     * which allows this method to be static since it doesn't need to keep any
     * state around, and can reuse the state the user gives it via the
     * {@code light} parameter. The values in light are always cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static double[][] reuseFOVSymmetrical(double[][] resistanceMap, double[][] light, int startX, int startY, double radius)
    {
        double decay = 1.0 / radius;
        fill(light, 0.0);
        light[startX][startY] = Math.min(1.0, radius);//make the starting space full power unless radius is tiny


        shadowCast(0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap);
        for (int row = 0; row <= radius + 1.0; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0; col++) {
                if(startX - col >= 0 && startY - row >= 0 && resistanceMap[startX - col][startY - row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX - col, startY - row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY - row] = 0.0;
            }
        }
        shadowCast(1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap);
        for (int col = 0; col <= radius + 1.0; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0; row++) {
                if(startX - col >= 0 && startY - row >= 0 && resistanceMap[startX - col][startY - row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX - col, startY - row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY - row] = 0.0;
            }
        }

        shadowCast(0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap);
        for (int row = 0; row <= radius + 1.0; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0; col++) {
                if(startX - col >= 0 && startY + row < light[0].length &&  resistanceMap[startX - col][startY + row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX - col, startY + row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY + row] = 0.0;
            }
        }
        shadowCast(1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap);
        for (int col = 0; col <= radius + 1.0; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0; row++) {
                if(startX - col >= 0 && startY + row < light[0].length && resistanceMap[startX - col][startY + row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX - col, startY + row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX - col][startY + row] = 0.0;
            }
        }

        shadowCast(0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap);
        for (int row = 0; row <= radius + 1.0; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0; col++) {
                if(startX + col < light.length && startY + row < light[0].length && resistanceMap[startX + col][startY + row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX + col, startY + row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY + row] = 0.0;
            }
        }
        shadowCast(-1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap);
        for (int col = 0; col <= radius + 1.0; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0; row++) {
                if(startX + col < light.length && startY + row < light[0].length && resistanceMap[startX + col][startY + row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX + col, startY + row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY + row] = 0.0;
            }
        }

        shadowCast(0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap);
        for (int row = 0; row <= radius + 1.0 && startY + row < light[0].length; row++) {
            for (int col = Math.max(1,row); col <= radius + 1.0; col++) {
                if(startX + col < light.length && startY - row >= 0 && resistanceMap[startX + col][startY - row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX + col, startY - row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY - row] = 0.0;
            }
        }
        shadowCast(-1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap);
        for (int col = 0; col <= radius + 1.0; col++) {
            for (int row = Math.max(1,col); row <= radius + 1.0; row++) {
                if(startX + col < light.length && startY - row >= 0 && resistanceMap[startX + col][startY - row] < 1.0 &&
                        !shadowCastCheck(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX + col, startY - row, decay, light, resistanceMap, 0, 0, light.length, light[0].length, startX, startY))
                    light[startX + col][startY - row] = 0.0;
            }
        }
        return light;
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light map where the values
     * are always either 0.0 for "not in line of sight" or 1.0 for "in line of
     * sight," which doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses shadowcasting FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "no line"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static double[][] reuseLOS(double[][] resistanceMap, double[][] light, int startX, int startY)
    {
        return reuseLOS(resistanceMap, light, startX, startY, 0, 0, light.length, light[0].length);
    }
    /**
     * Calculates which cells have line of sight from the given x, y coordinates.
     * Assigns to, and returns, a light map where the values
     * are always either 0.0 for "not in line of sight" or 1.0 for "in line of
     * sight," which doesn't mean a cell is actually visible if there's no light
     * in that cell. Always uses shadowcasting FOV, which allows this method to
     * be static since it doesn't need to keep any state around, and can reuse the
     * state the user gives it via the {@code light} parameter. The values in light
     * are always cleared before this is run, because prior state can make this give
     * incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell.
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "no line"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @return the computed light grid, which is the same 2D array as the value assigned to {@code light}
     */
    public static double[][] reuseLOS(double[][] resistanceMap, double[][] light, int startX, int startY,
                                      int minX, int minY, int maxX, int maxY)
    {
        double radius = light.length + light[0].length;
        double decay = 1.0 / radius;
        fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        
        shadowCastBinary(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        shadowCastBinary(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, minX, minY, maxX, maxY);
        
        return light;
    }
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting at the given angle in  degrees and covering a span
     * centered on that angle, also in degrees. Assigns to, and returns, a light
     * map where the values represent a percentage of fully lit. Always uses
     * shadowcasting FOV, which allows this method to be static since it doesn't
     * need to keep any state around, and can reuse the state the user gives it
     * via the {@code light} parameter. The values in light are cleared before
     * this is run, because prior state can make this give incorrect results.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy.  A conical section of FOV is lit by this method if
     * span is greater than 0.
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param span the angle in degrees that measures the full arc contained in the FOV cone
     * @return the computed light grid
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY,
                                          double radius, double angle, double span) {
        double decay = 1.0 / radius;
        fill(light, 0);
        light[startX][startY] = Math.min(1.0, radius);//make the starting space full power unless radius is tiny
        angle = ((angle >= 360.0 || angle < 0.0)
                ? (((angle % 360.0) + 360.0) % 360.0) : angle) * 0.002777777777777778;
        span = span * 0.002777777777777778;


        light = shadowCastLimited(1, 1.0, 0.0, 0, 1, 1, 0, radius, startX, startY, decay, light, resistanceMap, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, -1, 1, 0, radius, startX, startY, decay, light, resistanceMap, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, 1, radius, startX, startY, decay, light, resistanceMap, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, decay, light, resistanceMap, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, angle, span);

        light = shadowCastLimited(1, 1.0, 0.0, 0, 1, -1, 0, radius, startX, startY, decay, light, resistanceMap, angle, span);
        light = shadowCastLimited(1, 1.0, 0.0, 1, 0, 0, -1, radius, startX, startY, decay, light, resistanceMap, angle, span);
        return light;
    }
    
    private static void doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect) {
        final ArrayDeque<GridPoint2> dq = new ArrayDeque<>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(new GridPoint2(x, y));
        while (!dq.isEmpty()) {
            GridPoint2 p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (GridPoint2 dir : ccw_full) {
                int x2 = p.x + dir.x;
                int y2 = p.y + dir.y;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(new GridPoint2(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }



    private static void doRippleFOV(double[][] lightMap, int ripple, int x, int y, int startx, int starty, double decay, double radius, double[][] map, boolean[][] indirect, double angle, double span) {
        final ArrayDeque<GridPoint2> dq = new ArrayDeque<GridPoint2>();
        int width = lightMap.length;
        int height = lightMap[0].length;
        dq.offer(new GridPoint2(x, y));
        while (!dq.isEmpty()) {
            GridPoint2 p = dq.removeFirst();
            if (lightMap[p.x][p.y] <= 0 || indirect[p.x][p.y]) {
                continue;//no light to spread
            }

            for (GridPoint2 dir : ccw_full) {
                int x2 = p.x + dir.x;
                int y2 = p.y + dir.y;
                if (x2 < 0 || x2 >= width || y2 < 0 || y2 >= height //out of bounds
                        || radius(startx, starty, x2, y2) >= radius + 1) {//+1 to cover starting tile
                    continue;
                }
                double newAngle = atan2_(y2 - starty, x2 - startx);
                if (newAngle > span * 0.5 && newAngle < 1.0 - span * 0.5) 
                    continue;
//if (Math.abs(MathExtras.remainder(angle - newAngle, Math.PI * 2)) > span * 0.5)

                double surroundingLight = nearRippleLight(x2, y2, ripple, startx, starty, decay, lightMap, map, indirect);
                if (lightMap[x2][y2] < surroundingLight) {
                    lightMap[x2][y2] = surroundingLight;
                    if (map[x2][y2] < 1) {//make sure it's not a wall
                        dq.offer(new GridPoint2(x2, y2));//redo neighbors since this one's light changed
                    }
                }
            }
        }
    }

    private static double nearRippleLight(int x, int y, int rippleNeighbors, int startx, int starty, double decay, double[][] lightMap, double[][] map, boolean[][] indirect) {
        if (x == startx && y == starty) {
            return 1;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;
        List<GridPoint2> neighbors = new ArrayList<>();
        double tmpDistance = 0, testDistance;
        GridPoint2 c;
        for (GridPoint2 di : ccw_full) {
            int x2 = x + di.x;
            int y2 = y + di.y;
            if (x2 >= 0 && x2 < width && y2 >= 0 && y2 < height) {
                tmpDistance = radius(startx, starty, x2, y2);
                int idx = 0;
                for(int i = 0; i < neighbors.size() && i <= rippleNeighbors; i++)
                {
                    c = neighbors.get(i);
                    testDistance = radius(startx, starty, c.x, c.y);
                    if(tmpDistance < testDistance) {
                        break;
                    }
                    idx++;
                }
                neighbors.add(idx, new GridPoint2(x2, y2));
            }
        }

        if (neighbors.isEmpty()) {
            return 0;
        }
        neighbors = neighbors.subList(0, Math.min(neighbors.size(), rippleNeighbors));
        double light = 0;
        int lit = 0, indirects = 0;
        for (GridPoint2 p : neighbors) {
            if (lightMap[p.x][p.y] > 0) {
                lit++;
                if (indirect[p.x][p.y]) {
                    indirects++;
                }
                double dist = radius(x, y, p.x, p.y);
                light = Math.max(light, lightMap[p.x][p.y] - dist * decay - map[p.x][p.y]);
            }
        }

        if (map[x][y] >= 1 || indirects >= lit) {
            indirect[x][y] = true;
        }
        return light;
    }

    private static void shadowCast(int xx, int xy, int yx, int yy,
                                   double radius, int startx, int starty, double decay, double[][] lightMap,
                                   double[][] map) {
	    shadowCast(1, 1.0, 0.0, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map,
                0, 0, lightMap.length, lightMap[0].length);
    }

    private static void shadowCastBinary(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, double[][] lightMap,
                                         double[][] map,
                                         int minX, int minY, int maxX, int maxY) {
        double newStart = 0;
        if (start < end) {
            return;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                lightMap[currentX][currentY] = 1.0;

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCastBinary(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay,
                                lightMap, map, minX, minY, maxX, maxY);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }

    private static boolean shadowCastCheck(int row, double start, double end, int xx, int xy, int yx, int yy,
                                         double radius, int startx, int starty, double decay, double[][] lightMap,
                                         double[][] map,
                                         int minX, int minY, int maxX, int maxY, int targetX, int targetY) {
        double newStart = 0;
        if (start < end) {
            return false;
        }

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }

                if(currentX == targetX && currentY == targetY) return true;

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1.0) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1.0 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        if(shadowCastCheck(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay,
                                lightMap, map, minX, minY, maxX, maxY, targetX, targetY))
                            return true;
                        newStart = rightSlope;
                    }
                }
            }
        }
        return false;
    }

    private static void shadowCast(int row, double start, double end, int xx, int xy, int yx, int yy,
                                   double radius, int startx, int starty, double decay, double[][] lightMap,
                                   double[][] map, int minX, int minY, int maxX, int maxY) {
        double newStart = 0;
        if (start < end) {
            return;
        }
        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < maxX - minX + maxY - minY && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= minX && currentY >= minY && currentX < maxX && currentY < maxY) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double deltaRadius = radius(deltaX, deltaY);
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius) {
                    lightMap[currentX][currentY] = 1.0 - decay * deltaRadius; 
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        shadowCast(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay,
                                lightMap, map, minX, minY, maxX, maxY);
                        newStart = rightSlope;
                    }
                }
            }
        }
    }
    private static double[][] shadowCastLimited(int row, double start, double end, int xx, int xy, int yx, int yy,
                                                double radius, int startx, int starty, double decay, double[][] lightMap,
                                                double[][] map, double angle, double span) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double deltaRadius = radius(deltaX, deltaY),
                        at2 = Math.abs(angle - atan2_(currentY - starty, currentX - startx));// + 1.0) % 1.0;
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= radius
                        && (at2 <= span * 0.5
                        || at2 >= 1.0 - span * 0.5)) {
                    double bright = 1 - decay * deltaRadius;
                    lightMap[currentX][currentY] = bright;
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < radius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastLimited(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, decay, lightMap, map, angle, span);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    private static final double[] directionRanges = new double[8];
    /**
     * Calculates the Field Of View for the provided map from the given x, y
     * coordinates, lighting with the view "pointed at" the given {@code angle} in degrees,
     * extending to different ranges based on the direction the light is traveling.
     * The direction ranges are {@code forward}, {@code sideForward}, {@code side},
     * {@code sideBack}, and {@code back}; all are multiplied by {@code radius}.
     * Assigns to, and returns, a light map where the values represent a percentage of fully
     * lit. The values in light are cleared before this is run, because prior state can make
     * this give incorrect results. You can use {@link #addFOVsInto(double[][], double[][]...)}
     * if you want to mix FOV results, which works as an alternative to using the prior light state.
     * <br>
     * The starting point for the calculation is considered to be at the center
     * of the origin cell. Radius determinations are determined by the provided
     * RadiusStrategy. If all direction ranges are the same, this acts like
     * {@link #reuseFOV(double[][], double[][], int, int, double)}; otherwise
     * may produce conical shapes (potentially more than one, or overlapping ones).
     *
     * @param resistanceMap the grid of cells to calculate on; the kind made by DungeonUtility.generateResistances()
     * @param light the grid of cells to assign to; may have existing values, and 0.0 is used to mean "unlit"
     * @param startX the horizontal component of the starting location
     * @param startY the vertical component of the starting location
     * @param radius the distance the light will extend to (roughly); direction ranges will be multiplied by this
     * @param angle the angle in degrees that will be the center of the FOV cone, 0 points right
     * @param forward the range to extend when the light is within 22.5 degrees of angle; will be interpolated with sideForward
     * @param sideForward the range to extend when the light is between 22.5 and 67.5 degrees of angle; will be interpolated with forward or side
     * @param side the range to extend when the light is between 67.5 and 112.5 degrees of angle; will be interpolated with sideForward or sideBack
     * @param sideBack the range to extend when the light is between 112.5 and 157.5 degrees of angle; will be interpolated with side or back
     * @param back the range to extend when the light is more than 157.5 degrees away from angle; will be interpolated with sideBack
     * @return the computed light grid (the same as {@code light})
     */
    public static double[][] reuseFOV(double[][] resistanceMap, double[][] light, int startX, int startY,
                                      double radius, double angle,
                                      double forward, double sideForward, double side, double sideBack, double back) {
        directionRanges[0] = forward * radius;
        directionRanges[7] = directionRanges[1] = sideForward * radius;
        directionRanges[6] = directionRanges[2] = side * radius;
        directionRanges[5] = directionRanges[3] = sideBack * radius;
        directionRanges[4] = back * radius;

        radius = Math.max(1, radius);
        fill(light, 0);
        light[startX][startY] = 1;//make the starting space full power
        angle = ((angle >= 360.0 || angle < 0.0)
                ? (((angle % 360.0) + 360.0) % 360.0) : angle) * 0.002777777777777778;


        light = shadowCastPersonalized(1, 1.0, 0.0, 0, 1, 1, 0,   radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 1, 0, 0, 1,   radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, -1, 1, 0,  radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, -1, 0, 0, 1,  radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, -1, -1, 0, radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, -1, 0, 0, -1, radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 0, 1, -1, 0,  radius, startX, startY, light, resistanceMap, angle, directionRanges);
        light = shadowCastPersonalized(1, 1.0, 0.0, 1, 0, 0, -1,  radius, startX, startY, light, resistanceMap, angle, directionRanges);
        return light;
    }

    private static double[][] shadowCastPersonalized(int row, double start, double end, int xx, int xy, int yx, int yy,
                                                     double radius, int startx, int starty, double[][] lightMap,
                                                     double[][] map, double angle, final double[] directionRanges) {
        double newStart = 0;
        if (start < end) {
            return lightMap;
        }
        int width = lightMap.length;
        int height = lightMap[0].length;

        boolean blocked = false;
        for (int distance = row; distance <= radius && distance < width + height && !blocked; distance++) {
            int deltaY = -distance;
            for (int deltaX = -distance; deltaX <= 0; deltaX++) {
                int currentX = startx + deltaX * xx + deltaY * xy;
                int currentY = starty + deltaX * yx + deltaY * yy;
                double leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f);
                double rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f);

                if (!(currentX >= 0 && currentY >= 0 && currentX < width && currentY < height) || start < rightSlope) {
                    continue;
                } else if (end > leftSlope) {
                    break;
                }
                double at2 = Math.abs(angle - atan2_(currentY - starty, currentX - startx)) * 8.0,
                        deltaRadius = radius(deltaX, deltaY);
                int ia = (int)(at2), low = ia & 7, high = ia + 1 & 7;
                double a = at2 - ia, adjRadius = (1.0 - a) * directionRanges[low] + a * directionRanges[high];
                //check if it's within the lightable area and light if needed
                if (deltaRadius <= adjRadius) {
                    lightMap[currentX][currentY] = 1.0 - (deltaRadius / (adjRadius + 1.0)); // how bright the tile is
                }

                if (blocked) { //previous cell was a blocking one
                    if (map[currentX][currentY] >= 1) {//hit a wall
                        newStart = rightSlope;
                    } else {
                        blocked = false;
                        start = newStart;
                    }
                } else {
                    if (map[currentX][currentY] >= 1 && distance < adjRadius) {//hit a wall within sight line
                        blocked = true;
                        lightMap = shadowCastPersonalized(distance + 1, start, leftSlope, xx, xy, yx, yy, radius, startx, starty, lightMap, map, angle, directionRanges);
                        newStart = rightSlope;
                    }
                }
            }
        }
        return lightMap;
    }

    /**
     * Adds an FOV map to another in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Modifies the basis parameter in-place and makes no
     * allocations; this is different from {@link #addFOVs(double[][][])}, which creates a new 2D array.
     * @param basis a 2D double array, which can be empty or returned by calculateFOV() or reuseFOV(); modified!
     * @param addend another 2D double array that will be added into basis; this one will not be modified
     * @return the sum of the 2D double arrays passed, using the dimensions of basis if they don't match
     */
    public static double[][] addFOVsInto(double[][] basis, double[][] addend)
    {
        for (int x = 0; x < basis.length && x < addend.length; x++) {
                for (int y = 0; y < basis[x].length && y < addend[x].length; y++) {
                    basis[x][y] = Math.min(1.0, basis[x][y] + addend[x][y]);
                }
            }
        return basis;
    }
    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Allocates a new 2D double array and returns it.
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(double[][]... maps)
    {
        if(maps == null || maps.length == 0)
            return new double[0][0];
        double[][] map = new double[maps[0].length][maps[0][0].length];
        for (int x = 0; x < map.length; x++) {
            System.arraycopy(maps[0][x], 0, map[x], 0, map[0].length);
        }
        for(int i = 1; i < maps.length; i++)
        {
            for (int x = 0; x < map.length && x < maps[i].length; x++) {
                for (int y = 0; y < map[x].length && y < maps[i][x].length; y++) {
                    map[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }
    /**
     * Adds multiple FOV maps to basis cell-by-cell, modifying basis; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Returns basis without allocating new objects.
     * @param basis a 2D double array that will be modified by adding values in maps to it and clamping to 1.0 or less 
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return basis, with all elements in all of maps added to the corresponding cells and clamped
     */
    public static double[][] addFOVsInto(double[][] basis, double[][]... maps) {
        if (maps == null || maps.length == 0)
            return basis;
        for (int i = 1; i < maps.length; i++) {
            for (int x = 0; x < basis.length && x < maps[i].length; x++) {
                for (int y = 0; y < basis[x].length && y < maps[i][x].length; y++) {
                    basis[x][y] += maps[i][x][y];
                }
            }
        }
        for (int x = 0; x < basis.length; x++) {
            for (int y = 0; y < basis[x].length; y++) {
                if (basis[x][y] > 1.0) basis[x][y] = 1.0;
            }
        }
        return basis;
    }

    /**
     * Adds multiple FOV maps together in the simplest way possible; does not check line-of-sight between FOV maps.
     * Clamps the highest value for any single position at 1.0. Allocates a new 2D double array and returns it.
     * @param maps an Iterable of 2D double arrays (most collections implement Iterable),
     *             each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays passed, using the dimensions of the first if they don't all match
     */
    public static double[][] addFOVs(Iterable<double[][]> maps)
    {
        if(maps == null)
            return new double[0][0];
        Iterator<double[][]> it = maps.iterator();
        if(!it.hasNext())
            return new double[0][0];
        double[][] t = it.next();
        double[][] map = new double[t.length][t[0].length];
        for (int x = 0; x < map.length; x++) {
            System.arraycopy(t[x], 0, map[x], 0, map[0].length);
        }
        while (it.hasNext())
        {
            t = it.next();
            for (int x = 0; x < map.length && x < t.length; x++) {
                for (int y = 0; y < map[x].length && y < t[x].length; y++) {
                    map[x][y] += t[x][y];
                }
            }
        }
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVs(double[][] losMap, double[][]... maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }

        return map;
    }
    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param basis an existing 2D double array that should have matching width and height to losMap; will be modified
     * @param maps an array or vararg of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVsInto(double[][] losMap, double[][] basis, double[][]... maps)

    {
        if(losMap == null || losMap.length <= 0 || losMap[0].length <= 0)
            return addFOVsInto(basis, maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null || maps.length == 0)
            return map;
        for(int i = 0; i < maps.length; i++)
        {
            for (int x = 0; x < width && x < maps[i].length; x++) {
                for (int y = 0; y < height && y < maps[i][x].length; y++) {
                    if(losMap[x][y] > 0.0001) {
                        map[x][y] += maps[i][x][y];
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(map[x][y] > 1.0) map[x][y] = 1.0;
            }
        }
        return map;
    }

    /**
     * Adds together multiple FOV maps, but only adds to a position if it is visible in the given LOS map. Useful if
     * you want distant lighting to be visible only if the player has line-of-sight to a lit cell. Typically the LOS map
     * is calculated by {@link #reuseLOS(double[][], double[][], int, int)}, using the same resistance map used to
     * calculate the FOV maps. Clamps the highest value for any single position at 1.0.
     * @param losMap an LOS map such as one generated by {@link #reuseLOS(double[][], double[][], int, int)}
     * @param maps an Iterable of 2D double arrays, each usually returned by calculateFOV()
     * @return the sum of all the 2D double arrays in maps where a cell was visible in losMap
     */
    public static double[][] mixVisibleFOVs(double[][] losMap, Iterable<double[][]> maps)
    {
        if(losMap == null || losMap.length == 0)
            return addFOVs(maps);
        final int width = losMap.length, height = losMap[0].length;
        double[][] map = new double[width][height];
        if(maps == null)
            return map;
        for (double[][] map1 : maps) {
            for (int x = 0; x < width && x < map1.length; x++) {
                for (int y = 0; y < height && y < map1[x].length; y++) {
                    if (losMap[x][y] > 0.0001) {
                        map[x][y] += map1[x][y];
                        if (map[x][y] > 1.0) map[x][y] = 1.0;
                    }
                }
            }
        }
        return map;
    }
}
