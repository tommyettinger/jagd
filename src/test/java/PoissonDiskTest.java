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
        IndexedSet<Vector2> points = PoissonDisk.sampleRectangle(0f, 0f, 63.5f, 63.5f, 3f, 25, 500, new RNG("Fish"));
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
}
