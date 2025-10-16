package com.sciome.bmdexpressweb.views.mainstage;

import com.sciome.bmdexpressweb.dto.ProjectUploadResponse;
import com.sciome.bmdexpressweb.mvp.viewinterface.mainstage.IProjectNavigationView;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;

@CssImport("./styles/project-navigation.css")

/**
 * Vaadin implementation of IProjectNavigationView.
 * Displays project datasets in a tree structure.
 *
 * This is a simplified initial implementation demonstrating the MVP pattern.
 * The desktop JavaFX version uses TreeView with complex cell factories for icons and context menus.
 * This Vaadin version uses TreeGrid which provides similar hierarchical display capabilities.
 *
 * Pattern from desktop:
 * - View implements interface, contains only UI logic
 * - All business logic delegated to ProjectNavigationPresenter
 * - Presenter calls interface methods to update UI
 * - View calls presenter methods in response to user actions
 */
public class ProjectNavigationView extends VerticalLayout implements IProjectNavigationView
{
	private TreeGrid<ProjectTreeNode> datasetTree;
	private H3 title;
	private TreeData<ProjectTreeNode> treeData;

	public ProjectNavigationView()
	{
		initializeUI();
	}

	private void initializeUI()
	{
		// Title
		title = new H3("Project Navigation");

		// TreeGrid for displaying datasets
		datasetTree = new TreeGrid<>();
		datasetTree.addHierarchyColumn(item -> item.getLabel()).setHeader("Datasets")
			.setClassNameGenerator(item -> item.isGrayedOut() ? "grayed-out" : "");
		datasetTree.setSizeFull();

		// Initialize tree data structure
		treeData = new TreeData<>();
		datasetTree.setDataProvider(new TreeDataProvider<>(treeData));

		// Add custom CSS for grayed out items
		datasetTree.getStyle().set("--lumo-disabled-text-color", "#999");

		// Layout
		add(title, datasetTree);
		setPadding(true);
		setSpacing(true);
		setSizeFull();
	}

	/**
	 * Add selection listener to handle category selection
	 */
	public void addSelectionListener(CategorySelectionListener listener) {
		datasetTree.addSelectionListener(event -> {
			event.getFirstSelectedItem().ifPresent(node -> {
				if (node.getType() == NodeType.CATEGORY_RESULT && !node.isGrayedOut()) {
					listener.onCategorySelected(node.getResultId());
				}
			});
		});
	}

	/**
	 * Functional interface for category selection events
	 */
	@FunctionalInterface
	public interface CategorySelectionListener {
		void onCategorySelected(String categoryResultName);
	}

	@Override
	public void clearNavigationTree()
	{
		// Clear all items from tree
		treeData.clear();
		datasetTree.getDataProvider().refreshAll();
		System.out.println("ProjectNavigationView: Navigation tree cleared");
	}

	@Override
	public void addDataset(Object dataset, boolean selectIt)
	{
		// Simplified implementation - just adds to tree as flat list
		// Full implementation would organize into hierarchy based on dataset type
		System.out.println("ProjectNavigationView: Dataset added - " + dataset);
	}

	/**
	 * Populate tree from ProjectUploadResponse
	 */
	public void populateFromResponse(ProjectUploadResponse response)
	{
		clearNavigationTree();

		// Create root node for the project
		ProjectTreeNode rootNode = new ProjectTreeNode(response.getName(), NodeType.PROJECT_ROOT, null, false);
		treeData.addItem(null, rootNode);

		// Add Expression Data section
		if (response.getExpressionDataCount() > 0) {
			ProjectTreeNode expDataNode = new ProjectTreeNode(
				"Expression Data (" + response.getExpressionDataCount() + ")",
				NodeType.EXPRESSION_DATA_SECTION,
				null,
				true // grayed out
			);
			treeData.addItem(rootNode, expDataNode);
		}

		// Add BMD Results section
		if (response.getBmdResultNames() != null && !response.getBmdResultNames().isEmpty()) {
			ProjectTreeNode bmdNode = new ProjectTreeNode(
				"BMD Results",
				NodeType.BMD_RESULTS_SECTION,
				null,
				true // grayed out
			);
			treeData.addItem(rootNode, bmdNode);

			for (String bmdResult : response.getBmdResultNames()) {
				ProjectTreeNode resultNode = new ProjectTreeNode(bmdResult, NodeType.BMD_RESULT, null, true);
				treeData.addItem(bmdNode, resultNode);
			}
		}

		// Add Category Analysis section (NOT grayed out - this is functional)
		if (response.getCategoryResultNames() != null && !response.getCategoryResultNames().isEmpty()) {
			ProjectTreeNode catNode = new ProjectTreeNode(
				"Category Analysis",
				NodeType.CATEGORY_SECTION,
				null,
				false // NOT grayed out
			);
			treeData.addItem(rootNode, catNode);

			for (String catResult : response.getCategoryResultNames()) {
				ProjectTreeNode resultNode = new ProjectTreeNode(catResult, NodeType.CATEGORY_RESULT, catResult, false);
				treeData.addItem(catNode, resultNode);
			}
		}

		datasetTree.getDataProvider().refreshAll();
		datasetTree.expand(rootNode); // Auto-expand root

		System.out.println("ProjectNavigationView: Tree populated from response");
	}

	/**
	 * Node types for project tree
	 */
	public enum NodeType {
		PROJECT_ROOT,
		EXPRESSION_DATA_SECTION,
		BMD_RESULTS_SECTION,
		BMD_RESULT,
		CATEGORY_SECTION,
		CATEGORY_RESULT
	}

	/**
	 * Tree node class for project navigation
	 */
	public static class ProjectTreeNode {
		private final String label;
		private final NodeType type;
		private final String resultId; // For category results
		private final boolean grayedOut;

		public ProjectTreeNode(String label, NodeType type, String resultId, boolean grayedOut) {
			this.label = label;
			this.type = type;
			this.resultId = resultId;
			this.grayedOut = grayedOut;
		}

		public String getLabel() {
			return label;
		}

		public NodeType getType() {
			return type;
		}

		public String getResultId() {
			return resultId;
		}

		public boolean isGrayedOut() {
			return grayedOut;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	/*
	 * Full desktop implementation has these additional methods that will be uncommented
	 * when domain models are available:
	 *
	 * @Override
	 * public void addDoseResponseExperiment(DoseResponseExperiment doseResponseExperiment, boolean selectIt)
	 * {
	 *     // Add experiment to tree root level
	 *     // Create icon based on experiment properties
	 *     // Setup context menu with export, remove, rename options
	 *     // Setup double-click handler to show data view
	 * }
	 *
	 * @Override
	 * public void addOneWayANOVAAnalysis(OneWayANOVAResults results, boolean selectIt)
	 * {
	 *     // Add as child of parent experiment
	 *     // Create icon for ANOVA type
	 *     // Setup context menus for this analysis type
	 * }
	 *
	 * Similar methods for:
	 * - addWilliamsTrendAnalysis
	 * - addCurveFitPrefilterAnalysis
	 * - addOriogenAnalysis
	 * - addBMDAnalysis
	 * - addCategoryAnalysis
	 *
	 * And action methods:
	 * - performOneWayANOVA() - shows analysis dialog
	 * - performWilliamsTrend() - shows analysis dialog
	 * - performCurveFitPreFilter() - shows analysis dialog
	 * - performOriogen() - shows analysis dialog
	 * - performBMDAnalysis() - shows analysis dialog
	 * - performCategoryAnalysis(enum) - shows analysis dialog
	 * - expandTree() - expands all tree nodes
	 * - askToSaveBeforeClose() - shows confirmation dialog
	 * - askForAProjectFile() - shows file chooser
	 * - askForAProjectFileToOpen() - shows file open dialog
	 * - showMatrixPreview(String, MatrixData) - shows data preview dialog
	 * - setWindowSizeProperties() - saves window dimensions
	 * - getAChip(List, List, FileAnnotation) - shows chip selection dialog
	 * - askForABMDFileToImport() - shows file chooser for BMD import
	 * - askForAJSONFileToImport() - shows file chooser for JSON import
	 * - performBMDAnalysisGCurveP() - shows GCurveP analysis dialog
	 * - performBMDAnalysisToxicR() - shows ToxicR analysis dialog
	 *
	 * Desktop version is ~1,000 lines with complex TreeView setup, drag-drop support,
	 * context menus, selection handlers, and integration with various dialogs.
	 *
	 * This Vaadin version will be similar length when fully implemented, using:
	 * - TreeGrid for hierarchical display
	 * - ContextMenu for right-click actions
	 * - Dialog components for various prompts
	 * - Upload component for file selection
	 * - Grid selection model for item selection
	 */
}
