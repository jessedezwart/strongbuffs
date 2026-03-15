package nl.jessedezwart.strongbuffs.runtime;

import java.util.Set;

public interface RuntimeStateListener
{
	void onRuntimeStateChanged(Set<RuntimeTrigger> triggers, RuntimeState runtimeState);
}
