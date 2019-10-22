package com.xxq.domain.common;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@ThriftStruct
@NoArgsConstructor
@Data
@AllArgsConstructor(onConstructor = @__({@ThriftConstructor}))
public class OptionalStruct<T> {
    @Getter(onMethod = @__(@ThriftField(1)))
    T reference;

    public boolean isPresent() {
        return reference != null;
    }

    public T get() {
        return reference;
    }

    public static <T> OptionalStruct<T> of(T reference) {
        return new OptionalStruct<T>(reference);
    }

    public static <T> OptionalStruct<T> empty() {
        return new OptionalStruct<T>(null);
    }

    public T checkPresent(String message) {
        PreChecks.checkArgument(this.isPresent(), message);
        return reference;
    }
}
