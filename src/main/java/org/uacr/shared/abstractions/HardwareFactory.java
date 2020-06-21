package org.uacr.shared.abstractions;

public interface HardwareFactory {

    <T> T get(Class<T> tClass, Object... parameters);
}
