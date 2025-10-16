package com.sciome.bmdexpressweb.shared.eventbus.project;

import com.sciome.bmdexpress2.mvp.model.BMDProject;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBase;

/**
 * Event fired when a BMD project is saved to file.
 * Copied from desktop application (100% reusable).
 */
public class BMDProjectSavedEvent extends BMDExpressEventBase<BMDProject>
{

	public BMDProjectSavedEvent(BMDProject payload)
	{
		super(payload);
	}
}
