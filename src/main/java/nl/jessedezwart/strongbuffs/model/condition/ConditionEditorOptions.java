package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.experimental.UtilityClass;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;

/**
 * Shared option lists used by condition editors.
 *
 * The lists are precomputed once so editor code can reuse a stable option ordering without
 * repeating RuneLite enum storing in each condition definition.
 */
@UtilityClass
public class ConditionEditorOptions
{
	private final List<Skill> SKILLS = Collections.unmodifiableList(Arrays.asList(Skill.values()));
	private final List<Prayer> PRAYERS = Collections.unmodifiableList(Arrays.asList(Prayer.values()));

	public List<Skill> getSkills()
	{
		return SKILLS;
	}

	public List<Prayer> getPrayers()
	{
		return PRAYERS;
	}
}
