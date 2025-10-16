package com.sciome.bmdexpressweb.mvp.viewinterface.mainstage;

import java.io.File;
import java.util.List;

// TODO: These domain models need to be copied from desktop or provided by backend API
// import com.sciome.bmdexpress.ui.mvp.model.DoseResponseExperiment;
// import com.sciome.bmdexpress.ui.mvp.model.category.CategoryAnalysisResults;
// import com.sciome.bmdexpress.ui.mvp.model.chip.ChipInfo;
// import com.sciome.bmdexpress.ui.mvp.model.prefilter.CurveFitPrefilterResults;
// import com.sciome.bmdexpress.ui.mvp.model.prefilter.OneWayANOVAResults;
// import com.sciome.bmdexpress.ui.mvp.model.prefilter.OriogenResults;
// import com.sciome.bmdexpress.ui.mvp.model.prefilter.WilliamsTrendResults;
// import com.sciome.bmdexpress.ui.mvp.model.stat.BMDResult;
// import com.sciome.bmdexpress.ui.shared.CategoryAnalysisEnum;
// import com.sciome.bmdexpress.ui.util.MatrixData;
// import com.sciome.bmdexpress.ui.util.annotation.FileAnnotation;

/**
 * View interface for the project navigation tree.
 * Defines methods for managing the dataset tree and triggering analyses.
 * Copied from desktop application - 100% reusable interface.
 *
 * Note: Domain model references commented out until models are ported.
 * For initial implementation, we'll use simplified method signatures.
 */
public interface IProjectNavigationView
{
	public void clearNavigationTree();

	// Simplified versions without domain models for initial implementation
	public void addDataset(Object dataset, boolean selectIt);

	/*
	// Full versions - uncomment when domain models are available:

	public void addDoseResponseExperiement(DoseResponseExperiment doseResponseExperiment, boolean selectIt);

	public void addOneWayANOVAAnalysis(OneWayANOVAResults getPayload, boolean selectIt);

	public void addWilliamsTrendAnalysis(WilliamsTrendResults getPayload, boolean selectIt);

	public void addCurveFitPrefilterAnalysis(CurveFitPrefilterResults getPayload, boolean selectIt);

	public void addOriogenAnalysis(OriogenResults getPayload, boolean selectIt);

	public void addBMDAnalysis(BMDResult getPayload, boolean selectIt);

	public void addCategoryAnalysis(CategoryAnalysisResults getPayload, boolean selectIt);

	public void performOneWayANOVA();

	public void performWilliamsTrend();

	public void performCurveFitPreFilter();

	public void performOriogen();

	public void performBMDAnalysis();

	public void performCategoryAnalysis(CategoryAnalysisEnum categoryAnalysisEnum);

	public void expandTree();

	public int askToSaveBeforeClose();

	public File askForAProjectFile();

	public File askForAProjectFileToOpen();

	public void showMatrixPreview(String string, MatrixData matrixData);

	public void setWindowSizeProperties();

	public void getAChip(List<ChipInfo> choices, List<DoseResponseExperiment> doseResponseExperiment,
			FileAnnotation fileAnnotation);

	File askForABMDFileToImport();

	File askForAJSONFileToImport();

	public void performBMDAnalysisGCurveP();

	public void performBMDAnalysisToxicR();
	*/

}
