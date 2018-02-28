package jagd;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

/**
 * This provides a Uniform Poisson Disk Sampling technique that can be used to generate random points that have a
 * uniform minimum distance between each other. This uses libGDX {@link Vector2} items with floats for x and y, and the
 * static methods in this class return {@link IndexedSet} collections of those Vector2. Because the order of the Vector2
 * items can be important (each item is much more likely to be followed by a nearby Vector2 than a far-away one), the
 * order of the IndexedSet may also be important; its iteration order will start at the first point placed and iterating
 * or using {@link IndexedSet#getAt(int)} to get a higher index will likely find nearby Vector2 positions.
 * <br>
 * The algorithm is from the "Fast Poisson Disk Sampling in Arbitrary Dimensions" paper by Robert Bridson
 * http://www.cs.ubc.ca/~rbridson/docs/bridson-siggraph07-poissondisk.pdf
 * <br>
 * <a href="http://theinstructionlimit.com/fast-uniform-poisson-disk-sampling-in-c">From C# by Renaud Bedard</a>,
 * which was adapted from Java source by Herman Tulleken</a>.
 * <br>
 * Created by Tommy Ettinger on 10/20/2015.
 */
public class PoissonDisk {
    private static final float rootTwo = (float) Math.sqrt(2),
            pi2 = (float) (Math.PI * 2.0);

    private static final int defaultPointsPlaced = 10;

    private PoissonDisk() {
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static IndexedSet<Vector2> sampleCircle(Vector2 center, float radius, float minimumDistance)
    {
        return sampleCircle(center, radius, minimumDistance, defaultPointsPlaced, new RNG());
    }

    /**
     * Get a list of Coords, each randomly positioned around the given center out to the given radius (measured with
     * Euclidean distance, so a true circle), but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param center the center of the circle to spray Coords into
     * @param radius the radius of the circle to spray Coords into
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param pointsPerIteration with small radii, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static IndexedSet<Vector2> sampleCircle(Vector2 center, float radius, float minimumDistance,
                                                   int pointsPerIteration, RNG rng)
    {
        return sample(center.x -radius, center.y -radius, center.x + radius, center.y + radius,
                radius * radius, minimumDistance, pointsPerIteration, rng);
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static IndexedSet<Vector2> sampleRectangle(Vector2 minPosition, Vector2 maxPosition, float minimumDistance)
    {
        return sampleRectangle(minPosition, maxPosition, minimumDistance, defaultPointsPlaced, new RNG());
    }

    /**
     * Get a list of Coords, each randomly positioned within the rectangle between the given minPosition and
     * maxPosition, but with the given minimum distance from any other Coord in the list.
     * The parameters maxX and maxY should typically correspond to the width and height of the map; no points will have
     * positions with x equal to or greater than maxX and the same for y and maxY; similarly, no points will have
     * negative x or y.
     * @param minPosition the Coord with the lowest x and lowest y to be used as a corner for the bounding box
     * @param maxPosition the Coord with the highest x and highest y to be used as a corner for the bounding box
     * @param minimumDistance the minimum distance between Coords, in Euclidean distance as a float.
     * @param pointsPerIteration with small areas, this can be around 5; with larger ones, 30 is reasonable
     * @param rng an RNG to use for all random sampling.
     * @return an ArrayList of Coord that satisfy the minimum distance; the length of the array can vary
     */
    public static IndexedSet<Vector2> sampleRectangle(Vector2 minPosition, Vector2 maxPosition, float minimumDistance,
                                                      int pointsPerIteration, RNG rng)
    {
        return sample(minPosition.x, minPosition.y, maxPosition.x, maxPosition.y, 0f, minimumDistance, pointsPerIteration, rng);
    }

    private static IndexedSet<Vector2> sample(float minX, float minY, float maxX, float maxY, float rejectionDistance,
                                              float minimumDistance, int pointsPerIteration, RNG rng)
    {

        Vector2 center = new Vector2((minX + maxX) * 0.5f, (minY + maxY) * 0.5f);
        Vector2 dimensions = new Vector2(maxX - minX, maxY - minY);
        float cellSize = Math.max(minimumDistance / rootTwo, 0.25f);
        int gridWidth = (int)(dimensions.x / cellSize) + 1;
        int gridHeight = (int)(dimensions.y / cellSize) + 1;
        Vector2[][] grid = new Vector2[gridWidth][gridHeight];
        ArrayList<Vector2> activePoints = new ArrayList<Vector2>();
        IndexedSet<Vector2> points = new IndexedSet<Vector2>(128);

        //add first point
        boolean added = false;
        while (!added)
        {
            float xr = minX + dimensions.x * rng.nextFloat();
            float yr = Math.round(minY + dimensions.y * rng.nextFloat());

            if (rejectionDistance > 0 && Vector2.dst2(center.x, center.y, xr, yr) > rejectionDistance)
                continue;
            added = true;
            Vector2 p = new Vector2(Math.min(xr, maxX), Math.min(yr, maxY));
            
            grid[MathUtils.roundPositive((p.x - minX) / cellSize)][MathUtils.roundPositive((p.y - minY) / cellSize)] = p;

            activePoints.add(p);
            points.add(p);
        }
        //end add first point

        while (activePoints.size() != 0)
        {
            int listIndex = rng.nextInt(activePoints.size());

            Vector2 point = activePoints.get(listIndex);
            boolean found = false;

            for (int k = 0; k < pointsPerIteration; k++)
            {
                //add next point
                //get random point around
                float d = rng.nextFloat();
                float radius = minimumDistance + minimumDistance * d;
                float angle = pi2 * rng.nextFloat();

                Vector2 q = new Vector2(point.x + radius * MathUtils.sin(angle), point.y + radius * MathUtils.cos(angle));
                //end get random point around

                if (q.x >= minX && q.x <= maxX &&
                        q.y >= minY && q.y <= maxY &&
                        (rejectionDistance <= 0 || center.dst2(q) <= rejectionDistance))
                {
                    int qx = MathUtils.floorPositive((q.x - minX) / cellSize);
                    int qy = MathUtils.floorPositive((q.y - minY) / cellSize);
                    boolean tooClose = false;

                    for (int i = Math.max(0, qx - 2); i < Math.min(gridWidth, qx + 3) && !tooClose; i++) {
                        for (int j = Math.max(0, qy - 2); j < Math.min(gridHeight, qy + 3); j++) {
                            if (grid[i][j] != null && q.dst(grid[i][j]) < minimumDistance) {
                                tooClose = true;
                                break;
                            }
                        }
                    }
                    if (!tooClose)
                    {
                        found = true;
                        activePoints.add(q);
                        points.add(q);
                        grid[qx][qy] = q;
                    }
                }
                //end add next point
            }

            if (!found)
            {
                activePoints.remove(listIndex);
            }
        }
        return points;
    }
}