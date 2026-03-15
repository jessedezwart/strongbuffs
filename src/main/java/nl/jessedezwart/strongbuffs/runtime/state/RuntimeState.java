package nl.jessedezwart.strongbuffs.runtime.state;

import lombok.Getter;

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
