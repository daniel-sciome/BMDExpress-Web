package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.category.CategoryAnalysisResults;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when category analysis completes and data is loaded.
 * Copied from desktop application (100% reusable).
 */
public class CategoryAnalysisDataLoadedEvent extends BMDExpressEventBase<CategoryAnalysisResults>
{

	public CategoryAnalysisDataLoadedEvent(CategoryAnalysisResults payload)
	{
		super(payload);
	}
}
