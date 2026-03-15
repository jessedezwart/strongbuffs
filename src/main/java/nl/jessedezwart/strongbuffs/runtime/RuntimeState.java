package nl.jessedezwart.strongbuffs.runtime;

import lombok.Getter;
import nl.jessedezwart.strongbuffs.runtime.state.GroundItemRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.InventoryRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.LocationRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.SkillRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.VarRuntimeState;

@Getter
public class RuntimeState
{
	private final SkillRuntimeState skills = new SkillRuntimeState();
	private final VarRuntimeState vars = new VarRuntimeState();
	private final InventoryRuntimeState inventory = new InventoryRuntimeState();
	private final GroundItemRuntimeState groundItems = new GroundItemRuntimeState();
	private final LocationRuntimeState location = new LocationRuntimeState();

	public void clear()
	{
		skills.clear();
		vars.clear();
		inventory.clear();
		groundItems.clear();
		location.clear();
	}

	public enum PoisonState
	{
		NONE,
		POISON,
		VENOM
	}
}
