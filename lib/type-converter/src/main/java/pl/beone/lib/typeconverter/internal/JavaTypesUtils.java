package pl.beone.lib.typeconverter.internal;

import java.net.URI;

public class JavaTypesUtils {

    public static Boolean isString(Class<?> clazz) {
        return clazz.equals(String.class);
    }

    public static Boolean isBoolean(Class<?> clazz) {
        return clazz.equals(Boolean.class);
    }

    public static Boolean isLong(Class<?> clazz) {
        return clazz.equals(Long.class);
    }

    public static Boolean isInteger(Class<?> clazz) {
        return clazz.equals(Integer.class);
    }

    public static Boolean isDouble(Class<?> clazz) {
        return clazz.equals(Double.class);
    }

    public static Boolean isFloat(Class<?> clazz) {
        return clazz.equals(Float.class);
    }

    public static Boolean isURI(Class<?> clazz) {
        return clazz.equals(URI.class);
    }

}

