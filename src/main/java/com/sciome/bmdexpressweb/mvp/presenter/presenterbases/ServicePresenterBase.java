package com.sciome.bmdexpressweb.mvp.presenter.presenterbases;

import com.sciome.bmdexpressweb.shared.eventbus.BMDExpressEventBus;

/**
 * Base class for presenters that require a service layer.
 * Extends PresenterBase and adds service injection.
 * Copied from desktop application (100% reusable - pure Java).
 */
public abstract class ServicePresenterBase<S, T> extends PresenterBase<S> {
	private T service;

	public ServicePresenterBase(S view, T service, BMDExpressEventBus eventBus) {
		super(view, eventBus);
		this.service = service;
	}

	public T getService() {
		return service;
	}
}
