package com.caresync.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic container for paginated data grids across CareSync administration modules.
 *
 * @param <T> Entity or DTO type displayed in the paginated table.
 */
public class PaginatedResult<T> implements Serializable {

    private final List<T> items;
    private final long totalCount;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PaginatedResult(List<T> items, long totalCount, int page, int pageSize) {
        this.items = items != null ? items : new ArrayList<>();
        this.totalCount = Math.max(0, totalCount);
        this.page = Math.max(1, page);
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = (int) Math.ceil((double) this.totalCount / this.pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return page < totalPages;
    }

    public boolean hasPrev() {
        return page > 1;
    }

    public int getStartIndex() {
        if (totalCount == 0) return 0;
        return (page - 1) * pageSize + 1;
    }

    public int getEndIndex() {
        return (int) Math.min((long) page * pageSize, totalCount);
    }
}

