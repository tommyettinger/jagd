import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import jagd.IndexedSet;
import jagd.PoissonDisk;
import jagd.RNG;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by Tommy Ettinger on 2/28/2018.
 */
public class PoissonDiskTest {
    @Test
    public void testPoisson()
    {
        IndexedSet<Vector2> points = PoissonDisk.sampleRectangle(0f, 0f, 63.5f, 63.5f, 3f, 25, -1, new RNG("Fish"));
        final int HEIGHT = 64, WIDTH = 64;
        char[][] grid = new char[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            Arrays.fill(grid[y], '.');
        }
        int sz = points.size();
        Vector2 current;
        for (int i = 0; i < sz; i++) {
            current = points.getAt(i);
            grid[MathUtils.roundPositive(current.x)][MathUtils.roundPositive(current.y)] = '#';
        }
        for (int y = 0; y < HEIGHT; y++) {
            System.out.println(grid[y]);
        }
    }
    @Test
    public void stressTest() {
        RNG rng = new RNG(0L); // try -411885208567508193L for a weird state
        IndexedSet<Vector2> points;
        long sz = 0L, state;
        float xx = 0f, yy = 0f;
        for (int i = 0; i < 100000; i++) {
            state = rng.state;
            //center.set(rng.nextFloat(10), rng.nextFloat(15));
            try
            {
                points = PoissonDisk.sampleCircle(4.5f, 8f, 3.6f, 1.5f, 12, 12, rng);
                sz += points.size();
                for(Vector2 v : points)
                {
                    xx += v.x;
                    yy += v.y;
                }
            }catch (Exception e)
            {
                System.out.println("\nException on iteration " + i + " with state " + state + " :\n");
                e.printStackTrace();
                break;
            }
        }
        System.out.println(sz);
        System.out.println(xx);
        System.out.println(yy);
    }
}
