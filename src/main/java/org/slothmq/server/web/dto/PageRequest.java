package org.slothmq.server.web.dto;

public record PageRequest(Integer page, Integer pageSize, Sort sort, String search) {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final Sort DEFAULT_SORT = null;

    public static final PageRequest DEFAULT_PAGING = new PageRequest(DEFAULT_PAGE, DEFAULT_PAGE_SIZE,
            DEFAULT_SORT, null);

    public static PageRequest parseOrGetDefaults(Integer page, String pageSize, String sortField, String sortOrder,
                                                 String search) {
        Integer intPageSize = pageSize != null ? Integer.parseInt(pageSize) : DEFAULT_PAGE_SIZE;
        Sort.SortOrder order = Sort.SortOrder.ASC;
        if (sortOrder != null && !sortOrder.isEmpty()) {
            order = Sort.SortOrder.valueOf(sortOrder);
        }
        Sort sort = null;
        if (sortField != null && !sortField.isEmpty()) {
            sort = new Sort(sortField, order);
        }
        return new PageRequest(page, intPageSize, sort, search);
    }

    public record Sort(String field, SortOrder order) {
        public enum SortOrder {
            ASC, DESC
        }
    }
}
