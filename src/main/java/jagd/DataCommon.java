package jagd;

import com.badlogic.gdx.utils.IntArray;

/**
 * Code used internally for dealing with the hashing and ordering for IndexedMap, IndexedSet, and other data structures.
 * Has some methods and constants that may be useful in other kinds of code.
 * Created by Tommy Ettinger on 7/28/2017.
 */
public class DataCommon {
    // EnumHasher may come in handy at some point.
//    public static class EnumHasher implements CrossHash.IHasher
//    {
//        @Override
//        public int hash(Object data) {
//            return (data instanceof Enum) ? ((Enum)data).ordinal() + 1 : 0;
//        }
//
//        @Override
//        public boolean areEqual(Object left, Object right) {
//            return (left == right) || (left != null && left.equals(right));
//        }
//    }
//    public static final EnumHasher enumHasher = new EnumHasher();

    private DataCommon() {
    }

    /**
     * 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    public static final int INT_PHI = 0x9E3779B9;
    /**
     * The reciprocal of {@link #INT_PHI} modulo 2<sup>32</sup>.
     */
    public static final int INV_INT_PHI = 0x144cbc89;
    /**
     * 2<sup>64</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2.
     */
    public static final long LONG_PHI = 0x9E3779B97F4A7C15L;
    /**
     * The reciprocal of {@link #LONG_PHI} modulo 2<sup>64</sup>.
     */
    public static final long INV_LONG_PHI = 0xf1de83e19937733dL;
    
    /**
     * Thoroughly mixes the bits of an integer; GWT-compatible and will not lose precision as long as int inputs are
     * between {@link Integer#MIN_VALUE} and {@link Integer#MAX_VALUE}, inclusive (the inverse is only possible on GWT).
     *
     * @param n an integer.
     * @return a hash value obtained by mixing the bits of {@code n}.
     */
    public static int mix(final int n){
        final int h = n * 0x9E375;
        return h ^ (h >>> 16);
    }
    /**
     * Quickly mixes the bits of a long integer.
     * <br>This method mixes the bits of the argument by multiplying by the golden ratio and
     * xorshifting twice the result. It is borrowed from <a href="https://github.com/OpenHFT/Koloboke">Koloboke</a>, and
     * it has slightly worse behaviour than MurmurHash3 (in open-addressing hash tables the average number of probes
     * is slightly larger), but it's much faster.
     *
     * @param x a long integer.
     * @return a hash value obtained by mixing the bits of {@code x}.
     * @see #invMix(long)
     */
    public static long mix(final long x) {
        long h = x * LONG_PHI;
        h ^= h >>> 32;
        return h ^ (h >>> 16);
    }

    /**
     * The inverse of {@link #mix(long)}. This method is mainly useful to create unit tests.
     *
     * @param x a long integer.
     * @return a value that passed through {@link #mix(long)} would give {@code x}.
     */
    static long invMix(long x) {
        x ^= x >>> 32;
        x ^= x >>> 16;
        return (x ^ x >>> 32) * INV_LONG_PHI;
    }

    /**
     * Return the least power of two greater than or equal to the specified value.
     * <br>Note that this function will return 1 when the argument is 0.
     *
     * @param x an integer smaller than or equal to 2<sup>30</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    public static int nextPowerOfTwo(int x) {
        if (x == 0) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        return (x | x >> 16) + 1;
    }

    /**
     * Return the least power of two greater than or equal to the specified value.
     * <br>Note that this function will return 1 when the argument is 0.
     *
     * @param x a long integer smaller than or equal to 2<sup>62</sup>.
     * @return the least power of two greater than or equal to the specified value.
     */
    public static long nextPowerOfTwo(long x) {
        if (x == 0) return 1;
        x--;
        x |= x >> 1;
        x |= x >> 2;
        x |= x >> 4;
        x |= x >> 8;
        x |= x >> 16;
        return (x | x >> 32) + 1;
    }
    /** Moves the item at the specified index to the first index. */
    public static void moveToFirst (final IntArray array, final int index) {
        if (index >= array.size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + array.size);
        if(index == 0) return;
        int[] items = array.items;
        int value = items[index];
        System.arraycopy(items, 0, items, 1, index);
        items[0] = value;
    }

    /** Moves the item at the specified index to the last index. */
    public static void moveToLast (final IntArray array, final int index) {
        if (index >= array.size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + array.size);
        int sz = array.size - 1;
        if(index == sz) return;
        int[] items = array.items;
        int value = items[index];
        System.arraycopy(items, index + 1, items, index, sz - index);
        items[sz] = value;
    }

    /**
     * Exactly like {@link IntArray#removeValue(int)}, but returns the index that was removed or -1 if none was found.
     * @param array an IntArray that will be modified
     * @param value a value to search for and remove the first occurrence of
     * @return the index of the first found/removed value, or -1 if none was found
     */
    public static int removeValue(final IntArray array, final int value)
    {
        final int[] items = array.items;
        for (int i = 0, n = items.length; i < n; i++) {
            if (items[i] == value) {
                array.removeIndex(i);
                return i;
            }
        }
        return -1;

    }
    /**
     * Given an array or varargs of replacement indices for the values of this IntArray, reorders this so the first item
     * in the returned version is the same as {@code get(ordering[0])} (with some care taken for negative or too-large
     * indices), the second item in the returned version is the same as {@code get(ordering[1])}, etc.
     * <br>
     * Negative indices are considered reversed distances from the end of ordering, so -1 refers to the same index as
     * {@code ordering[ordering.length - 1]}. If ordering is smaller than this IntArray, only the indices up to the
     * length of ordering will be modified. If ordering is larger than this IntArray, only as many indices will be
     * affected as this IntArray's size, and reversed distances are measured from the end of this IntArray instead of
     * the end of ordering. Duplicate values in ordering will produce duplicate values in the returned IntArray.
     * <br>
     * This method modifies the given IntArray in-place and also returns it for chaining.
     *
     * @param array an IntArray to reorder
     * @param ordering an array or varargs of int indices, where the nth item in ordering changes the nth item in this
     *                 IntVLA to have the value currently in this IntVLA at the index specified by the value in ordering
     * @return this for chaining, after modifying it in-place
     */
    public static void reorder (IntArray array, int... ordering) {
        int ol;
        if (ordering == null || (ol = Math.min(array.size, ordering.length)) == 0)
            return;
        int[] items = array.items, alt = new int[ol];
        for (int i = 0; i < ol; i++) {
            alt[i] = items[(ordering[i] % ol + ol) % ol];
        }
        System.arraycopy(alt, 0, items, 0, ol);
    }
    


}
