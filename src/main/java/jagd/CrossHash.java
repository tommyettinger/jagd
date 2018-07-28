package jagd;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Simple hashing functions that we can rely on staying the same cross-platform.
 * The static methods of this class use a custom algorithm designed for speed and 
 * general-purpose usability, but not cryptographic security; this algorithm is
 * sometimes referred to as Wisp. The hashes this returns are always 0 when given null
 * to hash. Arrays with identical elements of identical types will hash identically.
 * Arrays with identical numerical values but different types will sometimes hash
 * differently. This class always provides 64-bit hashes via hash64() and 32-bit hashes
 * via hash(). The hash64() and hash() methods use 64-bit math even when producing 32-bit
 * hashes, for GWT reasons. GWT doesn't have the same behavior as desktop and Android
 * applications when using ints because it treats doubles mostly like ints, sometimes, due
 * to it using JavaScript. If we use mainly longs, though, GWT emulates the longs with a
 * more complex technique behind-the-scenes, that behaves the same on the web as it does on
 * desktop or on a phone. Since CrossHash is supposed to be stable cross-platform,
 * this is the way we need to go, despite it being slightly slower.
 * <br>
 * Created by Tommy Ettinger on 1/16/2016.
 * @author Tommy Ettinger
 */
public class CrossHash {
    public static long hash64(final CharSequence data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result ^= (z += (data.charAt(i) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final boolean[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += ((data[i] ? 0xFF51AFD7ED558CCDL : 0xC4CEB9FE1A85EC53L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }

    public static long hash64(final byte[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }

    public static long hash64(final short[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final int[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final long[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final char[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final float[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (NumberTools.floatToIntBits(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final double[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (NumberTools.doubleToLongBits(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length() ? end : data.length();
        for (int i = 0; i < len; i++) {
            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(final char[] data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i += step) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static long hash64(final CharSequence data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i += step) {
            result ^= (z += (data.charAt(i) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final char[][] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final int[][] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final long[][] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final CharSequence[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final CharSequence[]... data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        for (CharSequence datum : data) {
            result ^= (z += (hash64(datum) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);

    }

    public static long hash64(final List<? extends CharSequence> data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data.get(i)) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);

    }

    public static long hash64(final Object[] data) {
        if (data == null)
            return 0L;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result ^= (z += (((o = data[i]) == null ? -1L : o.hashCode()) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return ((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static long hash64(final Object data) {
        if (data == null)
            return 0L;
        long result = data.hashCode() * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (result ^ result >>> 33);
    }


    public static int hash(final CharSequence data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length();
        for (int i = 0; i < len; i++) {
            result ^= (z += (data.charAt(i) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final boolean[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += ((data[i] ? 0xFF51AFD7ED558CCDL : 0xC4CEB9FE1A85EC53L) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }

    public static int hash(final byte[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }

    public static int hash(final short[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final int[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final long[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final char[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final float[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (NumberTools.floatToIntBits(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final double[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (NumberTools.doubleToLongBits(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final char[] data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i++) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive) and ending before end (exclusive).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final CharSequence data, final int start, final int end) {
        if (data == null || start >= end)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length() ? end : data.length();
        for (int i = 0; i < len; i++) {
            result ^= (z += (data.charAt(i) ^ 0x9E3779B97F4A7C15L) * 0xC6BC279692B5CC83L);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result ^= result >>> 25 ^ z ^ z >>> 29;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the char array to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final char[] data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length ? end : data.length;
        for (int i = start; i < len; i += step) {
            result ^= (z += (data[i] ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    /**
     * Hashes only a subsection of the given data, starting at start (inclusive), ending before end (exclusive), and
     * moving between chars in increments of step (which is always greater than 0).
     *
     * @param data  the String or other CharSequence to hash
     * @param start the start of the section to hash (inclusive)
     * @param end   the end of the section to hash (exclusive)
     * @param step  how many elements to advance after using one element from data; must be greater than 0
     * @return a 32-bit hash code for the requested section of data
     */
    public static int hash(final CharSequence data, final int start, final int end, final int step) {
        if (data == null || start >= end || step <= 0)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = end < data.length() ? end : data.length();
        for (int i = start; i < len; i += step) {
            result ^= (z += (data.charAt(i) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final char[][] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final int[][] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final long[][] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final CharSequence[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final CharSequence[]... data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data[i]) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final Iterable<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        for (CharSequence datum : data) {
            result ^= (z += (hash64(datum) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);

    }

    public static int hash(final List<? extends CharSequence> data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.size();
        for (int i = 0; i < len; i++) {
            result ^= (z += (hash64(data.get(i)) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);

    }

    public static int hash(final Object[] data) {
        if (data == null)
            return 0;
        long result = 0x1A976FDF6BF60B8EL, z = 0x60642E2A34326F15L;
        final int len = data.length;
        Object o;
        for (int i = 0; i < len; i++) {
            result ^= (z += (((o = data[i]) == null ? -1L : o.hashCode()) ^ 0xC6BC279692B5CC85L) * 0x6C8E9CF570932BABL);
            result = (result << 54 | result >>> 10);
        }
        result += (z ^ z >>> 26) * 0x632BE59BD9B4E019L;
        result = (result ^ result >>> 33) * 0xFF51AFD7ED558CCDL;
        return (int)((result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L);
    }

    public static int hash(final Object data) {
        if (data == null)
            return 0;
        long result = data.hashCode() * 0xFF51AFD7ED558CCDL;
        result = (result ^ result >>> 33) * 0xC4CEB9FE1A85EC53L;
        return (int)(result ^ result >>> 33);
    }

    /**
     * An interface that can be used to move the logic for the hashCode() and equals() methods from a class' methods to
     * an implementation of IHasher that certain collections in SquidLib can use. Primarily useful when the key type is
     * an array, which normally doesn't work as expected in Java hash-based collections, but can if the right collection
     * and IHasher are used.
     */
    public interface IHasher extends Serializable {
        /**
         * If data is a type that this IHasher can specifically hash, this method should use that specific hash; in
         * other situations, it should simply delegate to calling {@link Object#hashCode()} on data. The body of an
         * implementation of this method can be very small; for an IHasher that is meant for byte arrays, the body could
         * be: {@code return (data instanceof byte[]) ? CrossHash.Lightning.hash((byte[]) data) : data.hashCode();}
         *
         * @param data the Object to hash; this method should take any type but often has special behavior for one type
         * @return a 32-bit int hash code of data
         */
        int hash(final Object data);

        /**
         * Not all types you might want to use an IHasher on meaningfully implement .equals(), such as array types; in
         * these situations the areEqual method helps quickly check for equality by potentially having special logic for
         * the type this is meant to check. The body of implementations for this method can be fairly small; for byte
         * arrays, it looks like: {@code return left == right
         * || ((left instanceof byte[] && right instanceof byte[])
         * ? Arrays.equals((byte[]) left, (byte[]) right)
         * : CrossHash.objectEquals(left, right));} , but for multidimensional arrays you should use the
         * {@link #equalityHelper(Object[], Object[], IHasher)} method with an IHasher for the inner arrays that are 1D
         * or otherwise already-hash-able, as can be seen in the body of the implementation for 2D char arrays, where
         * charHasher is an existing IHasher that handles 1D arrays:
         * {@code return left == right
         * || ((left instanceof char[][] && right instanceof char[][])
         * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
         * : CrossHash.objectEquals(left, right));}
         *
         * @param left  allowed to be null; most implementations will have special behavior for one type
         * @param right allowed to be null; most implementations will have special behavior for one type
         * @return true if left is equal to right (preferably by value, but reference equality may sometimes be needed)
         */
        boolean areEqual(final Object left, final Object right);
    }

    /**
     * Replacement for the missing Objects.equals() method on Java 6/Android; if both arguments are equal via reference
     * equality or (failing that) by their equals method, then this returns true, or false otherwise.
     * @param a an Object to compare; may be null
     * @param b an Object to compare; may be null
     * @return true if a and b are equal, false otherwise
     */
    public static boolean objectEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Not a general-purpose method; meant to ease implementation of {@link IHasher#areEqual(Object, Object)}
     * methods when the type being compared is a multi-dimensional array (which normally requires the heavyweight method
     * {@link Arrays#deepEquals(Object[], Object[])} or doing more work yourself; this reduces the work needed to
     * implement fixed-depth equality). As mentioned in the docs for {@link IHasher#areEqual(Object, Object)}, example
     * code that hashes 2D char arrays can be done using an IHasher for 1D char arrays called charHasher:
     * {@code return left == right
     * || ((left instanceof char[][] && right instanceof char[][])
     * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
     * : Objects.equals(left, right));}
     *
     * @param left an array of some kind of Object, usually an array, that the given IHasher can compare
     * @param right an array of some kind of Object, usually an array, that the given IHasher can compare
     * @param inner an IHasher to compare items in left with items in right
     * @return true if the contents of left and right are equal by the given IHasher, otherwise false
     */
    public static boolean equalityHelper(Object[] left, Object[] right, IHasher inner) {
        if (left == right)
            return true;
        if (left == null || right == null || left.length != right.length)
            return false;
        for (int i = 0; i < left.length; i++) {
            if (!inner.areEqual(left[i], right[i]))
                return false;
        }
        return true;
    }

    private static class BooleanHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        BooleanHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof boolean[]) ? CrossHash.hash((boolean[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof boolean[] && right instanceof boolean[]) ? Arrays.equals((boolean[]) left, (boolean[]) right) : objectEquals(left, right));
        }
    }

    public static final IHasher booleanHasher = new BooleanHasher();

    private static class ByteHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ByteHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof byte[]) ? CrossHash.hash((byte[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof byte[] && right instanceof byte[])
                    ? Arrays.equals((byte[]) left, (byte[]) right)
                    : objectEquals(left, right));
        }
    }

    public static final IHasher byteHasher = new ByteHasher();

    private static class ShortHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ShortHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof short[]) ? CrossHash.hash((short[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof short[] && right instanceof short[]) ? Arrays.equals((short[]) left, (short[]) right) : objectEquals(left, right));
        }
    }

    public static final IHasher shortHasher = new ShortHasher();

    private static class CharHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        CharHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[]) ? CrossHash.hash((char[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof char[] && right instanceof char[]) ? Arrays.equals((char[]) left, (char[]) right) : objectEquals(left, right));
        }
    }

    public static final IHasher charHasher = new CharHasher();

    private static class IntHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        IntHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[]) ? CrossHash.hash((int[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof int[] && right instanceof int[]) ? Arrays.equals((int[]) left, (int[]) right) : objectEquals(left, right);
        }
    }

    public static final IHasher intHasher = new IntHasher();

    private static class LongHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        LongHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[]) ? CrossHash.hash((long[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left instanceof long[] && right instanceof long[]) ? Arrays.equals((long[]) left, (long[]) right) : objectEquals(left, right);
        }
    }

    public static final IHasher longHasher = new LongHasher();

    private static class FloatHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        FloatHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof float[]) ? CrossHash.hash((float[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof float[] && right instanceof float[]) ? Arrays.equals((float[]) left, (float[]) right) : objectEquals(left, right));
        }
    }

    public static final IHasher floatHasher = new FloatHasher();

    private static class DoubleHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        DoubleHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof double[]) ? CrossHash.hash((double[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof double[] && right instanceof double[]) ? Arrays.equals((double[]) left, (double[]) right) : objectEquals(left, right));
        }
    }

    public static final IHasher doubleHasher = new DoubleHasher();

    private static class Char2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Char2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof char[][]) ? CrossHash.hash((char[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof char[][] && right instanceof char[][])
                    ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
                    : objectEquals(left, right));
        }
    }

    public static final IHasher char2DHasher = new Char2DHasher();

    private static class Int2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Int2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof int[][]) ? CrossHash.hash((int[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof int[][] && right instanceof int[][])
                    ? equalityHelper((int[][]) left, (int[][]) right, intHasher)
                    : objectEquals(left, right));
        }
    }

    public static final IHasher int2DHasher = new Int2DHasher();

    private static class Long2DHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        Long2DHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof long[][]) ? CrossHash.hash((long[][]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right
                    || ((left instanceof long[][] && right instanceof long[][])
                    ? equalityHelper((long[][]) left, (long[][]) right, longHasher)
                    : objectEquals(left, right));
        }
    }

    public static final IHasher long2DHasher = new Long2DHasher();

    private static class StringHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence) ? CrossHash.hash((CharSequence) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return objectEquals(left, right);
        }
    }

    public static final IHasher stringHasher = new StringHasher();

    private static class StringArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        StringArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof CharSequence[]) ? CrossHash.hash((CharSequence[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof CharSequence[] && right instanceof CharSequence[]) ? equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher) : objectEquals(left, right));
        }
    }

    /**
     * Though the name suggests this only hashes String arrays, it can actually hash any CharSequence array as well.
     */
    public static final IHasher stringArrayHasher = new StringArrayHasher();

    private static class ObjectArrayHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        ObjectArrayHasher() {
        }

        @Override
        public int hash(final Object data) {
            return (data instanceof Object[]) ? CrossHash.hash((Object[]) data) : data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right || ((left instanceof Object[] && right instanceof Object[]) && Arrays.equals((Object[]) left, (Object[]) right) || objectEquals(left, right));
        }
    }
    public static final IHasher objectArrayHasher = new ObjectArrayHasher();

    private static class DefaultHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 5L;

        DefaultHasher() {
        }

        @Override
        public int hash(final Object data) {
            if(data == null) return 0;
            final int x = data.hashCode() * 0x9E375;
            return x ^ x >>> 16;
        }

        @Override
        public boolean areEqual(final Object left, final Object right) {
            return (left == right) || (left != null && left.equals(right));
        }
    }

    /**
     * The go-to IHasher for object types that implement hashCode() and equals().
     * Scrambles the hashCode() result slightly (effectively using {@link DataCommon#mix(int)}). Checks for equality
     * first by reference, and if the arguments aren't reference-equivalent, it calls equals() on the first parameter if
     * it is non-null. This shouldn't be used for arrays (unless you want to use their default of hashing by identity
     * instead of by value); there are lots of different array-based IHashers here.
     */
    public static final IHasher defaultHasher = new DefaultHasher();

    private static class MildHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 4L;

        MildHasher() {
        }

        @Override
        public int hash(final Object data) {
            return data != null ? data.hashCode() : 0;
        }

        @Override
        public boolean areEqual(final Object left, final Object right) {
            return (left == right) || (left != null && left.equals(right));
        }
    }

    /**
     * The most basic IHasher type; does the same thing as Objects.hashCode(Object) and Objects.equals(Object, Object). Might not scramble the bits of a hash well enough to have good
     * performance in a hash table lke {@link IndexedMap} or {@link IndexedSet}, unless the objects being hashed have
     * good hashCode() implementations already.
     */
    public static final IHasher mildHasher = new MildHasher();

    private static class IdentityHasher implements IHasher, Serializable
    {
        private static final long serialVersionUID = 4L;
        IdentityHasher() { }

        @Override
        public int hash(Object data) {
            return System.identityHashCode(data);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return left == right;
        }
    }
    public static final IHasher identityHasher = new IdentityHasher();

    private static class GeneralHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        GeneralHasher() {
        }

        @Override
        public int hash(final Object data) {
            return CrossHash.hash(data);
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            if(left == right) return true;
            Class l = left.getClass(), r = right.getClass();
            if(l == r)
            {
                if(l.isArray())
                {
                    if(left instanceof int[]) return Arrays.equals((int[]) left, (int[]) right);
                    else if(left instanceof long[]) return Arrays.equals((long[]) left, (long[]) right);
                    else if(left instanceof char[]) return Arrays.equals((char[]) left, (char[]) right);
                    else if(left instanceof double[]) return Arrays.equals((double[]) left, (double[]) right);
                    else if(left instanceof boolean[]) return Arrays.equals((boolean[]) left, (boolean[]) right);
                    else if(left instanceof byte[]) return Arrays.equals((byte[]) left, (byte[]) right);
                    else if(left instanceof float[]) return Arrays.equals((float[]) left, (float[]) right);
                    else if(left instanceof short[]) return Arrays.equals((short[]) left, (short[]) right);
                    else if(left instanceof char[][]) return equalityHelper((char[][]) left, (char[][]) right, charHasher);
                    else if(left instanceof int[][]) return equalityHelper((int[][]) left, (int[][]) right, intHasher);
                    else if(left instanceof long[][]) return equalityHelper((long[][]) left, (long[][]) right, longHasher);
                    else if(left instanceof CharSequence[]) return equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher);
                    else if(left instanceof Object[]) return Arrays.equals((Object[]) left, (Object[]) right);
                }
                return objectEquals(left, right);
            }
            return false;
        }
    }

    /**
     * This IHasher is the one you should use if you aren't totally certain what types will go in an IndexedMap's keys
     * or an IndexedSet's items, since it can handle mixes of elements.
     */
    public static final IHasher generalHasher = new GeneralHasher();

}
