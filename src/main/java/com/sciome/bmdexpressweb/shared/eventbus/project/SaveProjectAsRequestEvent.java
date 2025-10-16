package com.sciome.bmdexpressweb.shared.eventbus.project;

import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event requesting the current project to be saved with a new name.
 * Copied from desktop application (100% reusable).
 */
public class SaveProjectAsRequestEvent extends BMDExpressEventBase<Object>
{

	public SaveProjectAsRequestEvent()
	{
		super(null);
	}
}
