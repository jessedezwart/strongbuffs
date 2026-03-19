package nl.jessedezwart.strongbuffs.runtime.state;

import lombok.Getter;
import nl.jessedezwart.strongbuffs.runtime.state.impl.GroundItemRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.InventoryRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.LocationRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.SkillRuntimeState;
import nl.jessedezwart.strongbuffs.runtime.state.impl.VarRuntimeState;

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
}
