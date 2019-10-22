package com.xxq.domain.common.pagination;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@ThriftStruct
@NoArgsConstructor
@Setter
@AllArgsConstructor(onConstructor = @__({@ThriftConstructor}))
public class OrderBy {

    @Getter(onMethod = @__(@ThriftField(1)))
    private Sort.Direction direction;

    @Getter(onMethod = @__(@ThriftField(2)))
    private String property;

    public Sort toSpringDataSort() {
        return new Sort(new Sort.Order(direction, property));
    }
}
