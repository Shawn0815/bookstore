package com.shawnyu.springbootmall.dto;

public enum BookSortColumn {

    PRICE("price"),
    PUBLISHED_DATE("published_date"),
    SALES_COUNT("sales_count");

    private final String columnName;

    BookSortColumn(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    // 白名單轉換：找不到對應欄位時，一律 fallback 成預設排序，避免把使用者輸入直接拼進 SQL
    public static BookSortColumn fromParam(String raw) {
        for (BookSortColumn column : values()) {
            if (column.columnName.equalsIgnoreCase(raw)) {
                return column;
            }
        }
        return PUBLISHED_DATE;
    }
}
