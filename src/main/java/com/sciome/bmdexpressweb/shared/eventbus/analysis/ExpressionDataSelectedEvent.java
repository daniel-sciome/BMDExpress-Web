package com.sciome.bmdexpressweb.shared.eventbus.analysis;

import com.sciome.bmdexpress2.mvp.model.DoseResponseExperiment;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when an expression dataset is selected in the navigation tree.
 * Copied from desktop application (100% reusable).
 */
public class ExpressionDataSelectedEvent extends BMDExpressEventBase<DoseResponseExperiment>
{

	public ExpressionDataSelectedEvent(DoseResponseExperiment payload)
	{
		super(payload);
	}
}
