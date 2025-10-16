package com.sciome.bmdexpressweb.mvp.presenter.presenterbases;

import com.google.common.eventbus.Subscribe;
import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBus;
import com.sciome.bmdexpressweb.shared.eventbus.project.CloseProjectRequestEvent;

/**
 * Base class for all presenters in the MVP pattern.
 * Handles EventBus registration and provides access to view and event bus.
 * Copied from desktop application (100% reusable - pure Java).
 */
@SuppressWarnings("restriction")
public abstract class PresenterBase<T>
{

	private T					view;
	private BMDExpressEventBus	eventBus;

	public PresenterBase(T view, BMDExpressEventBus eventBus)
	{

		this.view = view;
		this.eventBus = eventBus;

		eventBus.register(this);
	}

	/*
	 * Public Methods
	 */
	public void close()
	{
		destroy();
	}

	/*
	 * Protected Methods
	 */

	/*
	 * Get the current view
	 */
	protected T getView()
	{
		return view;
	}

	/*
	 * Get the event bus
	 */
	protected BMDExpressEventBus getEventBus()
	{
		return eventBus;
	}

	public void destroy()
	{
		eventBus.unregister(this);
	}

	@Subscribe
	public void onCloseApplicationRequest(CloseProjectRequestEvent event)
	{

	}

}
