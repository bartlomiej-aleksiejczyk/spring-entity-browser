package com.example.demo.pagecomponent;

import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@Component
public class PageComponentTableCreator {

    private final TemplateEngine templateEngine;

    public PageComponentTableCreator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String createTableFragment(List<Map<String, Object>> results, String entityName, String tableName,
            TableConfig config) {

        if (results == null || results.isEmpty()) {
            return "<p>No data available for " + entityName + "</p>";
        }

        Context context = new Context();
        context.setVariable("entityName", entityName);
        context.setVariable("tableName", tableName);
        context.setVariable("results", results);
        context.setVariable("headers", results.get(0).keySet());
        context.setVariable("config", config);

        return templateEngine.process("page-components/tableFragmentTemplate", context);
    }

    public static class TableConfig {
        private String entityName;
        private boolean pagination;
        private boolean ordering;
        private boolean showHeaders;
        private int pageSize;
        private int currentPage;
        private int totalPages;
        private String baseUrl;
        private String orderColumn;
        private boolean reverseOrder;

        public TableConfig(String entityName, boolean pagination, boolean ordering, boolean showHeaders, int pageSize,
                int currentPage,
                int totalPages, String baseUrl, String orderColumn, boolean reverseOrder) {
            this.entityName = entityName;
            this.pagination = pagination;
            this.ordering = ordering;
            this.showHeaders = showHeaders;
            this.pageSize = pageSize;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.baseUrl = baseUrl;
            this.orderColumn = orderColumn;
            this.reverseOrder = reverseOrder;
        }

        public String getEntityName() {
            return entityName;
        }

        public boolean isPagination() {
            return pagination;
        }

        public boolean isOrdering() {
            return ordering;
        }

        public boolean isShowHeaders() {
            return showHeaders;
        }

        public int getPageSize() {
            return pageSize;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String getOrderColumn() {
            return orderColumn;
        }

        public boolean isReverseOrder() {
            return reverseOrder;
        }

        public String getNextPageUrl() {
            int nextPage = currentPage + 1;
            return baseUrl + "?" + entityName + "_page=" + nextPage
                    + (orderColumn != null
                            ? "&" + entityName + "_order=" + orderColumn + (reverseOrder ? "_desc" : "_asc")
                            : "");
        }

        public String getPrevPageUrl() {
            int prevPage = currentPage - 1;
            return baseUrl + "?" + entityName + "_page=" + prevPage
                    + (orderColumn != null
                            ? "&" + entityName + "_order=" + orderColumn + (reverseOrder ? "_desc" : "_asc")
                            : "");
        }
    }
}
