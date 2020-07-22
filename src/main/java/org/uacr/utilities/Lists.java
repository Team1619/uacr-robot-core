package org.uacr.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Lists {

    public static <T> List<T> of() {
        return new ArrayList<>();
    }

    public static <T> List<T> of(T... objects) {
        return Arrays.asList(objects);
    }

    public static <T> List<T> of(Set<T> objects) {
        List<T> list = of();
        list.addAll(objects);
        return list;
    }
}
