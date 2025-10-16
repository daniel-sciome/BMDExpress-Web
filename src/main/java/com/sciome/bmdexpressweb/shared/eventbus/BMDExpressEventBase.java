package com.sciome.bmdexpressweb.shared.eventbus;

/**
 * Base class for all EventBus events.
 * Provides typed payload using generics.
 * Copied from desktop application (100% reusable).
 */
public abstract class BMDExpressEventBase<T>
{

	private T payload;

	public BMDExpressEventBase(T payload)
	{
		this.payload = payload;
	}

	public T GetPayload()
	{
		return payload;
	}
}
