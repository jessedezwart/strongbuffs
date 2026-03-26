package nl.jessedezwart.strongbuffs.runtime.tracker;

import java.util.Set;
import nl.jessedezwart.strongbuffs.runtime.state.RuntimeState;

public interface RuntimeStateListener
{
	void onRuntimeStateChanged(Set<RuntimeTrigger> triggers, RuntimeState runtimeState);
}
