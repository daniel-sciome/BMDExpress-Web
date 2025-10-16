package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when a BMD analysis dataset is selected in the navigation tree.
 * Copied from desktop application (100% reusable).
 */
public class BMDAnalysisDataSelectedEvent extends BMDExpressEventBase<BMDResult>
{

	public BMDAnalysisDataSelectedEvent(BMDResult payload)
	{
		super(payload);
	}
}
