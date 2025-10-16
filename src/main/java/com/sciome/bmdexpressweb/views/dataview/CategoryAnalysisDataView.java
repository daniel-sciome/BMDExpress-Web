package com.sciome.bmdexpressweb.views.dataview;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Vaadin view for displaying Category Analysis results
 * Shows both table data and BMD distribution charts
 */
public class CategoryAnalysisDataView extends VerticalLayout {

    private final H2 titleLabel;
    private final Grid<Map<String, Object>> dataGrid;
    private final Div chartsContainer;

    public CategoryAnalysisDataView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Title
        titleLabel = new H2("Category Analysis Results");
        add(titleLabel);

        // Data grid
        dataGrid = new Grid<>();
        dataGrid.setSizeFull();
        dataGrid.setPageSize(50);
        add(dataGrid);

        // Charts container (will hold ApexCharts)
        chartsContainer = new Div();
        chartsContainer.setWidthFull();
        chartsContainer.setHeight("400px");
        chartsContainer.getStyle()
                .set("border", "1px solid #ddd")
                .set("border-radius", "4px")
                .set("padding", "16px")
                .set("margin-top", "16px");
        add(chartsContainer);

        // Set flex grow so grid takes most space
        expand(dataGrid);
    }

    /**
     * Load category analysis data from the backend API response
     */
    public void loadCategoryData(Map<String, Object> categoryResult) {
        System.out.println("=== CategoryAnalysisDataView.loadCategoryData() CALLED ===");

        if (categoryResult == null) {
            System.out.println("ERROR: categoryResult is NULL");
            return;
        }

        System.out.println("DEBUG: categoryResult keys: " + categoryResult.keySet());
        System.out.println("DEBUG: categoryResult size: " + categoryResult.size());

        // Set title
        String name = (String) categoryResult.get("name");
        System.out.println("DEBUG: Category name: " + name);
        if (name != null) {
            titleLabel.setText("Category Analysis: " + name);
        }

        // Get column headers
        @SuppressWarnings("unchecked")
        List<String> columnHeaders = (List<String>) categoryResult.get("columnHeader");
        System.out.println("DEBUG: columnHeaders: " + columnHeaders);

        // Get the results list
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>)
                categoryResult.get("categoryAnalsyisResults"); // Note: typo in field name from backend
        System.out.println("DEBUG: results is null? " + (results == null));
        if (results != null) {
            System.out.println("DEBUG: results size: " + results.size());
            if (!results.isEmpty()) {
                System.out.println("DEBUG: First result keys: " + results.get(0).keySet());
            }
        }

        if (columnHeaders == null || results == null) {
            System.out.println("ERROR: columnHeaders or results is NULL - RETURNING");
            System.out.println("  columnHeaders is null? " + (columnHeaders == null));
            System.out.println("  results is null? " + (results == null));
            return;
        }

        // Clear existing columns
        dataGrid.removeAllColumns();
        System.out.println("DEBUG: Cleared existing columns");

        // Add columns dynamically based on column headers
        System.out.println("DEBUG: Adding " + columnHeaders.size() + " columns");
        for (int i = 0; i < columnHeaders.size(); i++) {
            final int colIndex = i;
            String header = columnHeaders.get(i);
            System.out.println("DEBUG: Adding column " + i + ": " + header);

            dataGrid.addColumn(rowData -> {
                @SuppressWarnings("unchecked")
                List<Object> row = (List<Object>) rowData.get("row");
                if (row != null && colIndex < row.size()) {
                    Object value = row.get(colIndex);
                    return value != null ? value.toString() : "";
                }
                return "";
            }).setHeader(header)
              .setResizable(true)
              .setSortable(true)
              .setAutoWidth(true);
        }
        System.out.println("DEBUG: Finished adding columns");

        // Set data provider
        System.out.println("DEBUG: Setting data provider with " + results.size() + " items");
        dataGrid.setDataProvider(new ListDataProvider<>(results));
        System.out.println("DEBUG: Data provider set successfully");

        System.out.println("=== CategoryAnalysisDataView.loadCategoryData() COMPLETE ===");

        // TODO: Extract BMD data and create charts
        extractAndDisplayCharts(results, columnHeaders);
    }

    /**
     * Extract BMD statistics and display distribution charts
     */
    private void extractAndDisplayCharts(List<Map<String, Object>> results, List<String> columnHeaders) {
        // Find BMD Mean column index
        int bmdMeanIndex = columnHeaders.indexOf("BMD Mean");
        int bmdMedianIndex = columnHeaders.indexOf("BMD Median");

        if (bmdMeanIndex == -1 && bmdMedianIndex == -1) {
            chartsContainer.removeAll();
            chartsContainer.setText("No BMD data available for charting");
            return;
        }

        // Extract BMD values
        List<Double> bmdMeanValues = new ArrayList<>();
        List<Double> bmdMedianValues = new ArrayList<>();

        for (Map<String, Object> result : results) {
            @SuppressWarnings("unchecked")
            List<Object> row = (List<Object>) result.get("row");
            if (row != null) {
                if (bmdMeanIndex >= 0 && bmdMeanIndex < row.size()) {
                    Object meanVal = row.get(bmdMeanIndex);
                    if (meanVal instanceof Number) {
                        bmdMeanValues.add(((Number) meanVal).doubleValue());
                    }
                }
                if (bmdMedianIndex >= 0 && bmdMedianIndex < row.size()) {
                    Object medianVal = row.get(bmdMedianIndex);
                    if (medianVal instanceof Number) {
                        bmdMedianValues.add(((Number) medianVal).doubleValue());
                    }
                }
            }
        }

        // Display summary statistics for now (ApexCharts integration in next step)
        chartsContainer.removeAll();
        Div statsDiv = new Div();
        statsDiv.getStyle().set("padding", "8px");

        if (!bmdMeanValues.isEmpty()) {
            double mean = bmdMeanValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double min = bmdMeanValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = bmdMeanValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

            Div meanStats = new Div();
            meanStats.setText(String.format("BMD Mean Statistics: Count=%d, Avg=%.3f, Min=%.3f, Max=%.3f",
                    bmdMeanValues.size(), mean, min, max));
            statsDiv.add(meanStats);
        }

        if (!bmdMedianValues.isEmpty()) {
            double mean = bmdMedianValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double min = bmdMedianValues.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
            double max = bmdMedianValues.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

            Div medianStats = new Div();
            medianStats.setText(String.format("BMD Median Statistics: Count=%d, Avg=%.3f, Min=%.3f, Max=%.3f",
                    bmdMedianValues.size(), mean, min, max));
            statsDiv.add(medianStats);
        }

        chartsContainer.add(statsDiv);
    }

    /**
     * Clear the view
     */
    public void clear() {
        titleLabel.setText("Category Analysis Results");
        dataGrid.setItems(new ArrayList<>());
        chartsContainer.removeAll();
    }
}
