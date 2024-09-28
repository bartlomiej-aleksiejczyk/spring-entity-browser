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

    // Need to know what is currently used order parameter
    public static class TableConfig {
        private boolean pagination;
        private boolean ordering;
        private boolean showHeaders;
        private int pageSize;
        private int currentPage;
        private int totalPages;

        public TableConfig(boolean pagination, boolean ordering, boolean showHeaders, int pageSize, int currentPage,
                int totalPages) {
            this.pagination = pagination;
            this.ordering = ordering;
            this.showHeaders = showHeaders;
            this.pageSize = pageSize;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
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
    }
}
