package com.xxq.domain.common.pagination;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Setter
@AllArgsConstructor(onConstructor = @__({@ThriftConstructor}))
public class Paged<T> {
    @Getter(onMethod = @__(@ThriftField(1)))
    private long total;

    @Getter(onMethod = @__(@ThriftField(2)))
    private List<T> data;

    public Paged(Page<T> page) {
        this.total = page.getTotalElements();
        this.data = page.getContent();
    }
}
