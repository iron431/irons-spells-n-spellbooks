package io.redspace.ironsspellbooks.util;

import io.redspace.ironsspellbooks.datafix.ParallelMatcher;

import java.io.DataInputStream;
import java.io.IOException;

public class ByteHelper {
    /**
     * Returns the start position of the first occurrence of the specified {@code target} within
     * {@code array}, or {@code -1} if there is no such occurrence.
     *
     * <p>More formally, returns the lowest index {@code i} such that {@code Arrays.copyOfRange(array,
     * i, i + target.length)} contains exactly the same elements as {@code target}.
     *
     * @param array  the array to search for the sequence {@code target}
     * @param target the array to search for as a sub-sequence of {@code array}
     */
    public static int indexOf(byte[] array, int length, byte[] target) {
        if (array == null || target == null || target.length == 0 || length == 0) {
            return -1;
        }

        outer:
        for (int i = 0; i < length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static int indexOf(DataInputStream dataInputStream, byte[] target) throws IOException {
        if (dataInputStream == null || target == null || target.length == 0) {
            return -1;
        }

        int index = -1;
        int data;

        outer:
        do {
            data = dataInputStream.read();
            index++;

            int addlReadCount = 0;

            for (int j = 0; j < target.length; j++) {
                if (data != target[j]) {
                    index += addlReadCount;
                    continue outer;
                } else if (j < target.length - 1) {
                    data = dataInputStream.read();
                    addlReadCount++;
                    if (data == -1) {
                        return -1;
                    }
                }
            }
            return index;
        } while (data != -1);
        return -1;
    }

    public static int indexOf(DataInputStream dataInputStream, ParallelMatcher parallelMatcher) throws IOException {
        if (dataInputStream == null || parallelMatcher == null) {
            return -1;
        }

        int data;
        int index = -1;

        do {
            data = dataInputStream.read();
            index++;
            if (parallelMatcher.pushValue(data)) {
                return index;
            }

        } while (data != -1);

        return -1;
    }
}
