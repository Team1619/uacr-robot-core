package org.uacr.shared.concretions;

import org.uacr.shared.abstractions.HardwareFactory;
import org.uacr.utilities.MultiObjectKey;
import org.uacr.utilities.injection.Inject;
import org.uacr.utilities.injection.Singleton;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;

@Singleton
public class SharedHardwareFactory implements HardwareFactory {

    private final HashMap<MultiObjectKey, Object> fObjectMap;

    @Inject
    public SharedHardwareFactory() {
        fObjectMap = new HashMap<>();
    }

    public <T> T get(Class<T> tClass, Object... parameters) {
        MultiObjectKey key = createKey(tClass, parameters);

        if (fObjectMap.containsKey(key)) {
            @SuppressWarnings("unchecked")
            T tObject = (T) fObjectMap.get(key);
            return tObject;
        }

        T tObject = construct(tClass, parameters);
        fObjectMap.put(key, tObject);
        return tObject;
    }

    private <T> T construct(Class<T> tClass, Object... parameters) {
        Class<?>[] parameterTypes = getParameterTypes(parameters);
        Constructor<?> tConstructor = getConstructor(tClass, parameterTypes);
        try {
            @SuppressWarnings("unchecked")
            T tObject = (T) tConstructor.newInstance(parameters);
            return tObject;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(Arrays.toString(tConstructor.getParameterTypes()));
            throw new RuntimeException("Invalid Configuration when creating " + tClass.getName() + " with parameters " + Arrays.toString(parameters));
        }
    }

    private Constructor<?> getConstructor(Class<?> tClass, Class<?>[] parameterTypes) {
        Constructor<?>[] constructors = tClass.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (parameterTypesMatch(constructor.getParameterTypes(), parameterTypes)) {
                return constructor;
            }
        }

        throw new RuntimeException("No constructor found for " + tClass);
    }

    private boolean parameterTypesMatch(Class<?>[] parameterTypes1, Class<?>[] parameterTypes2) {
        if (parameterTypes1.length != parameterTypes2.length) {
            return false;
        }
        for (int p = 0; p < parameterTypes1.length; p++) {
            if (!convertToNonPrimitiveClass(parameterTypes1[p]).isAssignableFrom(convertToNonPrimitiveClass(parameterTypes2[p]))) {
                return false;
            }
        }
        return true;
    }

    private Class<?> convertToNonPrimitiveClass(Class<?> tClass) {
        if(tClass == int.class) {
            return Integer.class;
        }

        if(tClass == short.class) {
            return Short.class;
        }

        if(tClass == long.class) {
            return Long.class;
        }

        if(tClass == float.class) {
            return Float.class;
        }

        if(tClass == double.class) {
            return Double.class;
        }

        if(tClass == byte.class) {
            return Byte.class;
        }

        if(tClass == char.class) {
            return Character.class;
        }

        if(tClass == boolean.class) {
            return Boolean.class;
        }

        return tClass;
    }

    private Class<?>[] getParameterTypes(Object[] parameters) {
        Class<?>[] parameterClasses = new Class[parameters.length];

        for (int p = 0; p < parameters.length; p++) {
            parameterClasses[p] = parameters[p].getClass();
        }

        return parameterClasses;
    }

    private MultiObjectKey createKey(Class<?> tClass, Object[] parameters) {
        return new MultiObjectKey(tClass, parameters);
    }
}
