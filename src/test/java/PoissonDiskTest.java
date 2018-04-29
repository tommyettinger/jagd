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
        RNG rng = new RNG(7777777L); // try -411885208567508193L for a weird state
        Vector2 center = new Vector2(1, 1);
        long sz = 0L, state;
        for (int i = 0; i < 1000000; i++) {
            state = rng.state;
            center.set(rng.nextFloat(10), rng.nextFloat(15));
            try
            {
                sz += PoissonDisk.sampleCircle(center, 4f, 1.5f, 9, 10, rng).size();
            }catch (IndexOutOfBoundsException e)
            {
                System.out.println("\nException on iteration " + i + " with state " + state + " :\n");
                e.printStackTrace();
                break;
            }
        }
        System.out.println(sz);
    }
}
