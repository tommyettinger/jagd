import jagd.MimicWFC;
import jagd.RNG;

/**
 * Created by Tommy Ettinger on 3/28/2018.
 */
public class WFCTest {
    public static void main(String[] args)
    {
        RNG random = new RNG(1337);
        int[][] grid = new int[32][32];
        char[][] dungeon = new char[][]{
                "                                  ".toCharArray(),
                "    ┌───────┐ ┌─────┐ ┌────────┐  ".toCharArray(),
                "  ┌─┤.......│ │.....└─┤........│  ".toCharArray(),
                "  │.└┐......│┌┴───....│........│  ".toCharArray(),
                "  │..├───┐..││.................│  ".toCharArray(),
                "  │..│   │..││.................├─┐".toCharArray(),
                "  │..└┐┌─┘..││....┌┐.....──────┘.│".toCharArray(),
                "  │...└┘....││..──┤│.............│".toCharArray(),
                "  │.........││....└┼─┐..........┌┘".toCharArray(),
                "  └┐.....┌──┘│.....└┐└┬────────┬┘ ".toCharArray(),
                "   │.....│   │......│ │........│  ".toCharArray(),
                "   ├─...┌┘  ┌┴─..┌──┴─┘........│  ".toCharArray(),
                "   │....│   │....│.............└─┐".toCharArray(),
                "  ┌┘...┌┘   │....│...............│".toCharArray(),
                "  │....└─┐  │..┌─┴────...........│".toCharArray(),
                "  │......└┐ │..│...............─.│".toCharArray(),
                "  │.......└─┘..│.................│".toCharArray(),
                "  │..┌┐...........┌───...........│".toCharArray(),
                "  └──┘└─┐.........│............┌─┘".toCharArray(),
                "        └───┐..│..│............│  ".toCharArray(),
                "      ┌────┐└┬─┘..└┬───┐......┌┘  ".toCharArray(),
                "   ┌──┘....│┌┘.....└─┐┌┘..─┬──┘   ".toCharArray(),
                "  ┌┘.......││........├┘....└┐     ".toCharArray(),
                "  │........├┘........│......└┐    ".toCharArray(),
                "  │........│...─┐....│.......└┐   ".toCharArray(),
                "  └┐....│..│....│....│........│   ".toCharArray(),
                "   └─┬──┘.......│..──┘..┌┐....│   ".toCharArray(),
                "     │..........│.......││....│   ".toCharArray(),
                "    ┌┘.....│....│......┌┘│...┌┘   ".toCharArray(),
                "    │......├────┤..──┬─┘ │...│    ".toCharArray(),
                "    │.....┌┘    │....│ ┌─┘..─┤    ".toCharArray(),
                "    └──┐..│     │....│ │.....│    ".toCharArray(),
                "       └──┘     └────┘ └─────┘    ".toCharArray(),
        };
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                grid[y][x] = dungeon[x][y];
            }
        }
        // this uses order 2, which has slightly weaker quality but will finish quickly and more reliably.
        // you can try order 3 with some inputs, but it is much less likely to finish at all.
        // this specifies non-periodic input (meaning it doesn't wrap at edges), but periodic output, so it tiles.
        MimicWFC wfc = new MimicWFC(grid, 2, 80, 80, false, false, 1, Integer.valueOf(' '));
        int i = 0;
        while (!wfc.run(random, 1000000)) System.out.println((++i) + " attempts failed.");
        int[][] grid2 = wfc.result();
        for (int y = 0; y < 80; y++) { 
            for (int x = 0; x < 80; x++) {
                System.out.print((char) grid2[x][y]);
            }
            System.out.println();
        }
        System.out.println();
        wfc = new MimicWFC(grid, 2, 80, 80, false, false, 1, Integer.valueOf('.'));
        i = 0;
        while (!wfc.run(random, 1000000))
            System.out.println((++i) + " attempts failed.");
        grid2 = wfc.result();
        for (int y = 0; y < 80; y++) {
            for (int x = 0; x < 80; x++) {
                System.out.print((char) grid2[x][y]);
            }
            System.out.println();
        }
        
    }
}
