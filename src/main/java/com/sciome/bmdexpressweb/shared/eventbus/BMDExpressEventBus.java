package com.sciome.bmdexpressweb.shared.eventbus;

import com.google.common.eventbus.EventBus;
import org.springframework.stereotype.Component;

/**
 * EventBus for decoupled communication between UI components.
 * Adapted from desktop application - now managed by Spring as a singleton bean.
 *
 * In the web version, we use Spring's @Component to create a singleton instance
 * instead of the manual singleton pattern used in the desktop version.
 */
@Component
public class BMDExpressEventBus extends EventBus
{
	// Spring automatically creates a singleton instance
	public BMDExpressEventBus()
	{
		super();
	}
}
