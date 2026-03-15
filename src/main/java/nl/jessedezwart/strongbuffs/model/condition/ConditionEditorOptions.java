package nl.jessedezwart.strongbuffs.model.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Prayer;
import net.runelite.api.Skill;

public final class ConditionEditorOptions
{
	private static final List<Skill> SKILLS = Collections.unmodifiableList(Arrays.asList(Skill.values()));
	private static final List<Prayer> PRAYERS = Collections.unmodifiableList(Arrays.asList(Prayer.values()));

	private ConditionEditorOptions()
	{
	}

	public static List<Skill> getSkills()
	{
		return SKILLS;
	}

	public static List<Prayer> getPrayers()
	{
		return PRAYERS;
	}
}
