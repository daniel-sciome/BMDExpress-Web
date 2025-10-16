package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.stat.BMDResult;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when BMD analysis completes and data is loaded.
 * Copied from desktop application (100% reusable).
 */
public class BMDAnalysisDataLoadedEvent extends BMDExpressEventBase<BMDResult>
{

	public BMDAnalysisDataLoadedEvent(BMDResult payload)
	{
		super(payload);
	}
}
