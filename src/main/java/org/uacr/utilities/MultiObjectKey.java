package org.uacr.utilities;

import java.util.Arrays;

public class MultiObjectKey {

    private final Object[] fKeys;

    public MultiObjectKey(Object... keys) {
        if(keys == null) {
            keys = new Object[0];
        }
        fKeys = keys;
    }

    public boolean equals(Object object) {
        if(object instanceof MultiObjectKey) {
            return Arrays.deepEquals(fKeys, ((MultiObjectKey) object).fKeys);
        }
        return false;
    }

    public int hashCode() {
        return Arrays.deepHashCode(fKeys);
    }
}
