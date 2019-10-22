package com.xxq.domain.common.pagination;

import com.facebook.swift.codec.ThriftConstructor;
import com.facebook.swift.codec.ThriftField;
import com.facebook.swift.codec.ThriftStruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ThriftStruct
@NoArgsConstructor
@Setter
@AllArgsConstructor(onConstructor = @__({@ThriftConstructor}))
public class Paging implements Pageable {

    @Getter(onMethod = @__(@ThriftField(1)))
    private int page;

    @Getter(onMethod = @__(@ThriftField(2)))
    private int size;

    @Getter(onMethod = @__(@ThriftField(4)))
    private OrderBy order;


    public Paging(int page, int size) {
        this(page, size, null);
    }

    @Override
    public int getPageNumber() {
        return page;
    }

    @Override
    public Pageable next() {
        return new Paging(getPage(), getPageSize(), this.order);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    public Pageable previous() {
        return page == 1 ? this : new Paging(getPage() - 1, getPageSize(), this.order);
    }

    @Override
    public Pageable first() {
        return new Paging(1, getPageSize(), this.order);
    }

    @Override
    public boolean hasPrevious() {
        return page > 1;
    }

    @Override
    public Sort getSort() {
        return order==null?null:order.toSpringDataSort();
    }

    @Override
    public int getPageSize() {
        return size;
    }

    @Override
    public long getOffset() {
        return (page - 1) * size;
    }
}
