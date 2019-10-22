package com.xxq.domain.common;


public class PreChecks {
    public static void checkArgument(boolean expression, Object errorMessage) {
        checkState(expression, errorMessage);
    }

    public static void checkState(boolean expression, Object errorMessage) {
        if (!expression) {
            throw new ApplicationException(String.valueOf(errorMessage));
        }
    }

    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new ApplicationException(String.valueOf(errorMessage));
        }
        return reference;
    }

    public static void checkNotEmpty(String reference, Object errorMessage) {
        if (reference == null || reference.trim().length() == 0) {
            throw new ApplicationException(String.valueOf(errorMessage));
        }
    }
}
