package com.sciome.bmdexpressweb.shared.eventbus.project;

import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event requesting the current project to be exported as JSON.
 * Copied from desktop application (100% reusable).
 */
public class SaveProjectAsJSONRequestEvent extends BMDExpressEventBase<Object>
{

	public SaveProjectAsJSONRequestEvent()
	{
		super(null);
	}
}
