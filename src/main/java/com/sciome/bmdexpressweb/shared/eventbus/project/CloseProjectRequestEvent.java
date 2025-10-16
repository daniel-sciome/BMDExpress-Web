package com.sciome.bmdexpressweb.shared.eventbus.project;

import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event requesting the current project to be closed.
 * Copied from desktop application (100% reusable).
 */
public class CloseProjectRequestEvent extends BMDExpressEventBase<Object>
{

	public CloseProjectRequestEvent()
	{
		super(null);
	}
}
