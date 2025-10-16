package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when expression data is loaded from file.
 * Copied from desktop application (100% reusable).
 */
public class ExpressionDataLoadedEvent extends BMDExpressEventBase<DoseResponseExperiment>
{

	public ExpressionDataLoadedEvent(DoseResponseExperiment payload)
	{
		super(payload);
	}
}
