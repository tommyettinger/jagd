package jagd;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static jagd.NumberTools.doubleToMixedIntBits;
import static jagd.NumberTools.floatToIntBits;

/**
 * 32-bit and 64-bit hash code functions for arrays and some other types, with cross-platform equivalent results.
 * A fairly fast hashing algorithm in general, Water performs especially well on large arrays, and passes SMHasher's
 * newest and most stringent version of tests. The int-hashing {@link #hash(int[])} method is faster than
 * {@link Arrays#hashCode(int[])}, and all hashes are higher-quality than Arrays.hashCode(). Based on
 * <a href="https://github.com/wangyi-fudan/wyhash">wyhash</a>, specifically
 * <a href="https://github.com/tommyettinger/waterhash">the waterhash variant</a>. This version passes SMHasher for
 * both the 32-bit output hash() methods and the 64-bit output hash64() methods (which use the slightly tweaked
 * wheathash variant in the waterhash Git repo, or woothash for hashing long arrays). While an earlier version
 * passed rurban/smhasher, it failed demerphq/smhasher (Yves' more stringent fork), so some minor tweaks allowed the
 * latest code to pass Yves' test. Uses 64-bit math, so it won't be as fast on GWT. Currently, the methods that hash
 * types other than int arrays aren't as fast as the int array hash, but they are usually fast enough and pass SMHasher.
 * <br>
 * These hash functions are so fast because they operate in bulk on 4 items at a time, such as 4 ints (which is the
 * optimal case), 4 bytes, or 4 longs (which uses a different algorithm). This bulk operation usually entails 3
 * multiplications and some other, cheaper operations per 4 items hashed. For long arrays, it requires many more
 * multiplications, but modern CPUs can pipeline the operations on unrelated longs to run in parallel on one core.
 * If any items are left over after the bulk segment, Water uses the least effort possible to hash the remaining 1,
 * 2, or 3 items left. Most of these operations use the method {@link #mum(long, long)}, which helps take two inputs
 * and multiply them once, getting a more-random result after another small step. The long array code uses
 * {@link #wow(long, long)} (similar to mum upside-down), which mixes up its arguments with each other before
 * multplying. It finishes with either code similar to mum() for 32-bit output hash() methods, or a somewhat more
 * rigorous method for 64-bit output hash64() methods (still similar to mum).
 */
@SuppressWarnings("NumericOverflow")
public final class CrossHash {
    /**
     * Big constant 0.
     */
    public static final long b0 = 0xA0761D6478BD642FL;
    /**
     * Big constant 1.
     */
    public static final long b1 = 0xE7037ED1A0B428DBL;
    /**
     * Big constant 2.
     */
    public static final long b2 = 0x8EBC6AF09C88C6E3L;
    /**
     * Big constant 3.
     */
    public static final long b3 = 0x589965CC75374CC3L;
    /**
     * Big constant 4.
     */
    public static final long b4 = 0x1D8E4E27C47D124FL;
    /**
     * Big constant 5.
     */
    public static final long b5 = 0xEB44ACCAB455D165L;

    /**
     * Takes two arguments that are technically longs, and should be very different, and uses them to get a result
     * that is technically a long and mixes the bits of the inputs. The arguments and result are only technically
     * longs because their lower 32 bits matter much more than their upper 32, and giving just any long won't work.
     * <br>
     * This is very similar to wyhash's mum function, but doesn't use 128-bit math because it expects that its
     * arguments are only relevant in their lower 32 bits (allowing their product to fit in 64 bits).
     * @param a a long that should probably only hold an int's worth of data
     * @param b a long that should probably only hold an int's worth of data
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long mum(final long a, final long b) {
        final long n = a * b;
        return n - (n >>> 32);
    }

    /**
     * A slower but higher-quality variant on {@link #mum(long, long)} that can take two arbitrary longs (with any
     * of their 64 bits containing relevant data) instead of mum's 32-bit sections of its inputs, and outputs a
     * 64-bit result that can have any of its bits used.
     * <br>
     * This was changed so it distributes bits from both inputs a little better on July 6, 2019.
     * @param a any long
     * @param b any long
     * @return a sort-of randomized output dependent on both inputs
     */
    public static long wow(final long a, final long b) {
        final long n = (a ^ (b << 39 | b >>> 25)) * (b ^ (a << 39 | a >>> 25));
        return n ^ (n >>> 32);
    }

    public static long hash64(final boolean[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;//seed = b1 ^ b1 >>> 29 ^ b1 >>> 43 ^ b1 << 7 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public static long hash64(final byte[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b2, b1 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ b3, data[len-2] ^ data[len-1] << 8 ^ b4); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 8, b2 ^ data[len-1]); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final short[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final char[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final CharSequence data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final int[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    
    public static long hash64(final int[] data, final int length) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        seed = (seed ^ seed << 16) * (length ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final long[] data) {
        if (data == null) return 0;
        long seed = 0x1E98AE18CA351B28L,// seed = b0 ^ b0 >>> 23 ^ b0 >>> 48 ^ b0 << 7 ^ b0 << 53, 
                a = seed ^ b4, b = (seed << 17 | seed >>> 47) ^ b3,
                c = (seed << 31 | seed >>> 33) ^ b2, d = (seed << 47 | seed >>> 17) ^ b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a = (data[i-3] ^ a) * b1; a = (a << 23 | a >>> 41) * b3;
            b = (data[i-2] ^ b) * b2; b = (b << 25 | b >>> 39) * b4;
            c = (data[i-1] ^ c) * b3; c = (c << 29 | c >>> 35) * b5;
            d = (data[i  ] ^ d) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 + data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) ^ wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0 ^ seed >>> 32);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public static long hash64(final float[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToIntBits(data[i-3]) ^ b1, floatToIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToIntBits(data[i-1]) ^ b3, floatToIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToIntBits(data[len-1]) >>> 16), b3 ^ (floatToIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToIntBits(data[len-2]), b0 ^ floatToIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToIntBits(data[len-3]), b2 ^ floatToIntBits(data[len-2])) ^ mum(seed ^ floatToIntBits(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }
    public static long hash64(final double[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(doubleToMixedIntBits(data[i-3]) ^ b1, doubleToMixedIntBits(data[i-2]) ^ b2) + seed,
                    mum(doubleToMixedIntBits(data[i-1]) ^ b3, doubleToMixedIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (doubleToMixedIntBits(data[len-1]) >>> 16), b3 ^ (doubleToMixedIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ doubleToMixedIntBits(data[len-2]), b0 ^ doubleToMixedIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ doubleToMixedIntBits(data[len-3]), b2 ^ doubleToMixedIntBits(data[len-2])) ^ mum(seed ^ doubleToMixedIntBits(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
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
            return 0;
        long seed = 9069147967908697017L;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
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
            return 0;
        long seed = 9069147967908697017L;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
    }


    public static long hash64(final char[][] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final int[][] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final long[][] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final CharSequence[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final CharSequence[]... data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data.get(i-3)) ^ b1, hash(data.get(i-2)) ^ b2) + seed,
                    mum(hash(data.get(i-1)) ^ b3, hash(data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data.get(len-2)), b0 ^ hash(data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(data.get(len-3)), b2 ^ hash(data.get(len-2))) ^ mum(seed ^ hash(data.get(len-1)), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);

    }

    public static long hash64(final Object[] data) {
        if (data == null) return 0;
        long seed = 9069147967908697017L;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0);
        return seed - (seed >>> 31) + (seed << 33);
    }

    public static long hash64(final Object data) {
        if (data == null)
            return 0;
        final long h = data.hashCode() * 0x9E3779B97F4A7C15L;
        return h - (h >>> 31) + (h << 33);
    }


    public static int hash(final boolean[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum((data[i-3] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b1, (data[i-2] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b2) + seed,
                    mum((data[i-1] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b3, (data[i] ? 0x9E3779B9L : 0x7F4A7C15L) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] ? 0x9E37L : 0x7F4AL), b3 ^ (data[len-1]  ? 0x79B9L : 0x7C15L)); break;
            case 2: seed = mum(seed ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L), b0 ^ (data[len-1] ? 0x9E3779B9L : 0x7F4A7C15L)); break;
            case 3: seed = mum(seed ^ (data[len-3] ? 0x9E3779B9L : 0x7F4A7C15L), b2 ^ (data[len-2] ? 0x9E3779B9L : 0x7F4A7C15L)) ^ mum(seed ^ (data[len-1] ? 0x9E3779B9 : 0x7F4A7C15), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    public static int hash(final byte[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b2, b1 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ b3, data[len-2] ^ data[len-1] << 8 ^ b4); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 8, b2 ^ data[len-1]); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final short[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final char[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final CharSequence data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i  ) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    public static int hash(final int[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[len-1] >>> 16), b3 ^ (data[len-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[len-2], b0 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3], b2 ^ data[len-2]) ^ mum(seed ^ data[len-1], b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    public static int hash(final int[] data, final int length) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        for (int i = 3; i < length; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (length & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (data[length-1] >>> 16), b3 ^ (data[length-1] & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ data[length-2], b0 ^ data[length-1]); break;
            case 3: seed = mum(seed ^ data[length-3], b2 ^ data[length-2]) ^ mum(seed ^ data[length-1], b4); break;
        }
        return (int) mum(seed ^ seed << 16, length ^ b0);
    }
    
    public static int hash(final long[] data) {
        if (data == null) return 0;
        long seed = 0x1E98AE18CA351B28L,// seed = b0 ^ b0 >>> 23 ^ b0 >>> 48 ^ b0 << 7 ^ b0 << 53, 
                a = seed ^ b4, b = (seed << 17 | seed >>> 47) ^ b3,
                c = (seed << 31 | seed >>> 33) ^ b2, d = (seed << 47 | seed >>> 17) ^ b1;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            a = (data[i-3] ^ a) * b1; a = (a << 23 | a >>> 41) * b3;
            b = (data[i-2] ^ b) * b2; b = (b << 25 | b >>> 39) * b4;
            c = (data[i-1] ^ c) * b3; c = (c << 29 | c >>> 35) * b5;
            d = (data[i  ] ^ d) * b4; d = (d << 31 | d >>> 33) * b1;
            seed += a + b + c + d;
        }
        seed += b5;
        switch (len & 3) {
            case 1: seed = wow(seed, b1 ^ data[len-1]); break;
            case 2: seed = wow(seed + data[len-2], b2 + data[len-1]); break;
            case 3: seed = wow(seed + data[len-3], b2 + data[len-2]) ^ wow(seed + data[len-1], seed ^ b3); break;
        }
        seed = (seed ^ seed << 16) * (len ^ b0 ^ seed >>> 32);
        return (int)(seed - (seed >>> 32));
    }

    public static int hash(final float[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(floatToIntBits(data[i-3]) ^ b1, floatToIntBits(data[i-2]) ^ b2) + seed,
                    mum(floatToIntBits(data[i-1]) ^ b3, floatToIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ (floatToIntBits(data[len-1]) >>> 16), b3 ^ (floatToIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ floatToIntBits(data[len-2]), b0 ^ floatToIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ floatToIntBits(data[len-3]), b2 ^ floatToIntBits(data[len-2])) ^ mum(seed ^ floatToIntBits(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    public static int hash(final double[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(doubleToMixedIntBits(data[i-3]) ^ b1, doubleToMixedIntBits(data[i-2]) ^ b2) + seed,
                    mum(doubleToMixedIntBits(data[i-1]) ^ b3, doubleToMixedIntBits(data[i]) ^ b4));
        }
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^(doubleToMixedIntBits(data[len-1]) >>> 16), b3 ^ (doubleToMixedIntBits(data[len-1]) & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ doubleToMixedIntBits(data[len-2]), b0 ^ doubleToMixedIntBits(data[len-1])); break;
            case 3: seed = mum(seed ^ doubleToMixedIntBits(data[len-3]), b2 ^ doubleToMixedIntBits(data[len-2])) ^ mum(seed ^ doubleToMixedIntBits(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
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
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = Math.min(end, data.length);
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data[i-3] ^ b1, data[i-2] ^ b2) + seed,
                    mum(data[i-1] ^ b3, data[i] ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data[len-1]); break;
            case 2: seed = mum(seed ^ data[len-2], b3 ^ data[len-1]); break;
            case 3: seed = mum(seed ^ data[len-3] ^ data[len-2] << 16, b1 ^ data[len-1]); break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
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
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = Math.min(end, data.length());
        for (int i = start + 3; i < len; i+=4) {
            seed = mum(
                    mum(data.charAt(i-3) ^ b1, data.charAt(i-2) ^ b2) + seed,
                    mum(data.charAt(i-1) ^ b3, data.charAt(i) ^ b4));
        }
        switch (len - start & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^ b3, b4 ^ data.charAt(len-1)); break;
            case 2: seed = mum(seed ^ data.charAt(len-2), b3 ^ data.charAt(len-1)); break;
            case 3: seed = mum(seed ^ data.charAt(len-3) ^ data.charAt(len-2) << 16, b1 ^ data.charAt(len-1)); break;
        }
        return (int) mum(seed ^ seed << 16, len - start ^ b0);
    }
    
    
    public static int hash(final char[][] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final int[][] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final long[][] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final CharSequence[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final CharSequence[]... data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    public static int hash(final Iterable<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final Iterator<? extends CharSequence> it = data.iterator();
        int len = 0;
        while (it.hasNext())
        {
            ++len;
            seed = mum(
                    mum(hash(it.next()) ^ b1, (it.hasNext() ? hash(it.next()) ^ b2 ^ ++len : b2)) + seed,
                    mum((it.hasNext() ? hash(it.next()) ^ b3 ^ ++len : b3), (it.hasNext() ? hash(it.next()) ^ b4 ^ ++len : b4)));
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }

    public static int hash(final List<? extends CharSequence> data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.size();
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data.get(i-3)) ^ b1, hash(data.get(i-2)) ^ b2) + seed,
                    mum(hash(data.get(i-1)) ^ b3, hash(data.get(i  )) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data.get(len-1))) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data.get(len-2)), b0 ^ hash(data.get(len-1))); break;
            case 3: seed = mum(seed ^ hash(data.get(len-3)), b2 ^ hash(data.get(len-2))) ^ mum(seed ^ hash(data.get(len-1)), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final Object[] data) {
        if (data == null) return 0;
        long seed = -260224914646652572L;//b1 ^ b1 >>> 41 ^ b1 << 53;
        final int len = data.length;
        for (int i = 3; i < len; i+=4) {
            seed = mum(
                    mum(hash(data[i-3]) ^ b1, hash(data[i-2]) ^ b2) + seed,
                    mum(hash(data[i-1]) ^ b3, hash(data[i  ]) ^ b4));
        }
        int t;
        switch (len & 3) {
            case 0: seed = mum(b1 ^ seed, b4 + seed); break;
            case 1: seed = mum(seed ^((t = hash(data[len-1])) >>> 16), b3 ^ (t & 0xFFFFL)); break;
            case 2: seed = mum(seed ^ hash(data[len-2]), b0 ^ hash(data[len-1])); break;
            case 3: seed = mum(seed ^ hash(data[len-3]), b2 ^ hash(data[len-2])) ^ mum(seed ^ hash(data[len-1]), b4); break;
        }
        return (int) mum(seed ^ seed << 16, len ^ b0);
    }
    
    public static int hash(final Object data) {
        if (data == null)
            return 0;
        final int h = data.hashCode() * 0x9E375;
        return h ^ (h >>> 16);
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
         * : (left != null && left.equals(right)));} , but for multidimensional arrays you should use the
         * {@link #equalityHelper(Object[], Object[], IHasher)} method with an IHasher for the inner arrays that are 1D
         * or otherwise already-hash-able, as can be seen in the body of the implementation for 2D char arrays, where
         * charHasher is an existing IHasher that handles 1D arrays:
         * {@code return left == right
         * || ((left instanceof char[][] && right instanceof char[][])
         * ? equalityHelper((char[][]) left, (char[][]) right, charHasher)
         * : (left != null && left.equals(right)));}
         *
         * @param left  allowed to be null; most implementations will have special behavior for one type
         * @param right allowed to be null; most implementations will have special behavior for one type
         * @return true if left is equal to right (preferably by value, but reference equality may sometimes be needed)
         */
        boolean areEqual(final Object left, final Object right);
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
     * : (left == right) || (left != null && left.equals(right)));}
     *
     * @param left
     * @param right
     * @param inner
     * @return
     */
    public static boolean equalityHelper(Object[] left, Object[] right, IHasher inner) {
        if (left == right)
            return true;
        if ((left == null) ^ (right == null))
            return false;
        for (int i = 0; i < left.length && i < right.length; i++) {
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
            return left == right || ((left instanceof boolean[] && right instanceof boolean[]) ? Arrays.equals((boolean[]) left, (boolean[]) right) : (left == right) || (left != null && left.equals(right)));
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
                    : (left == right) || (left != null && left.equals(right)));
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
            return left == right || ((left instanceof short[] && right instanceof short[]) ? Arrays.equals((short[]) left, (short[]) right) : (left == right) || (left != null && left.equals(right)));
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
            return left == right || ((left instanceof char[] && right instanceof char[]) ? Arrays.equals((char[]) left, (char[]) right) : (left == right) || (left != null && left.equals(right)));
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
            return (left instanceof int[] && right instanceof int[]) ? Arrays.equals((int[]) left, (int[]) right) : (left == right) || (left != null && left.equals(right));
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
            return (left instanceof long[] && right instanceof long[]) ? Arrays.equals((long[]) left, (long[]) right) : (left == right) || (left != null && left.equals(right));
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
            return left == right || ((left instanceof float[] && right instanceof float[]) ? Arrays.equals((float[]) left, (float[]) right) : (left == right) || (left != null && left.equals(right)));
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
            return left == right || ((left instanceof double[] && right instanceof double[]) ? Arrays.equals((double[]) left, (double[]) right) : (left == right) || (left != null && left.equals(right)));
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
                    : (left == right) || (left != null && left.equals(right)));
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
                    : (left == right) || (left != null && left.equals(right)));
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
                    : (left == right) || (left != null && left.equals(right)));
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
            return (left == right) || (left != null && left.equals(right));
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
            return left == right || ((left instanceof CharSequence[] && right instanceof CharSequence[]) ? equalityHelper((CharSequence[]) left, (CharSequence[]) right, stringHasher) : (left == right) || (left != null && left.equals(right)));
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
            return left == right || ((left instanceof Object[] && right instanceof Object[]) && Arrays.equals((Object[]) left, (Object[]) right) || (left == right) || (left != null && left.equals(right)));
        }
    }
    public static final IHasher objectArrayHasher = new ObjectArrayHasher();

    private static class DefaultHasher implements IHasher, Serializable {
        private static final long serialVersionUID = 3L;

        DefaultHasher() {
        }

        @Override
        public int hash(final Object data) {
            return data.hashCode();
        }

        @Override
        public boolean areEqual(Object left, Object right) {
            return (left == right) || (left != null && left.equals(right));
        }
    }

    public static final IHasher defaultHasher = new DefaultHasher();

    private static class IdentityHasher implements IHasher, Serializable
    {
        private static final long serialVersionUID = 3L;
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
            if(left == null || right == null) return false;
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
                return left.equals(right);
            }
            return false;
        }
    }

    /**
     * This IHasher is the one you should use if you aren't totally certain what types will go in an OrderedMap's keys
     * or an OrderedSet's items, since it can handle mixes of elements.
     */
    public static final IHasher generalHasher = new GeneralHasher();

}
