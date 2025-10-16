package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when a category analysis dataset is selected in the navigation tree.
 * Copied from desktop application (100% reusable).
 */
public class CategoryAnalysisDataSelectedEvent extends BMDExpressEventBase<CategoryAnalysisResults>
{

	public CategoryAnalysisDataSelectedEvent(CategoryAnalysisResults payload)
	{
		super(payload);
	}
}
