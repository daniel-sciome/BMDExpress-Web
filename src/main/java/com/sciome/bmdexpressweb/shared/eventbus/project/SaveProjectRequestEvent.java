package com.sciome.bmdexpressweb.shared.eventbus.project;

import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event requesting the current project to be saved.
 * Copied from desktop application (100% reusable).
 */
public class SaveProjectRequestEvent extends BMDExpressEventBase<Object>
{

	public SaveProjectRequestEvent()
	{
		super(null);
	}
}
