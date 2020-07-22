package org.uacr.utilities;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Sets {

    public static <T> HashSet<T> of(T... objects) {
        return new HashSet<>(Arrays.asList(objects));
    }

    public static <T> HashSet<T> of(List<T> objects) {
        return new HashSet<>(objects);
    }
}
