package nl.jessedezwart.strongbuffs.panel.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.RuleRuntimeController;

@Singleton
public class RuleRepository
{
	private final RuleDefinitionStore store;
	private final RuleRuntimeController ruleRuntimeController;
	private final List<RuleDefinition> persistedRules = new ArrayList<>();

	public RuleRepository(RuleDefinitionStore store)
	{
		this(store, null);
	}

	@Inject
	public RuleRepository(RuleDefinitionStore store, RuleRuntimeController ruleRuntimeController)
	{
		this.store = store;
		this.ruleRuntimeController = ruleRuntimeController;
	}

	public void reload()
	{
		persistedRules.clear();
		persistedRules.addAll(store.load());
		synchronizeRuntime();
	}

	public List<RuleDefinition> getAll()
	{
		return Collections.unmodifiableList(new ArrayList<>(persistedRules));
	}

	public RuleDefinition findById(String ruleId)
	{
		if (ruleId == null)
		{
			return null;
		}

		for (RuleDefinition ruleDefinition : persistedRules)
		{
			if (Objects.equals(ruleDefinition.getId(), ruleId))
			{
				return ruleDefinition;
			}
		}

		return null;
	}

	public void saveRule(RuleDefinition ruleDefinition)
	{
		if (ruleDefinition == null)
		{
			return;
		}

		int existingIndex = indexOf(ruleDefinition.getId());

		if (existingIndex >= 0)
		{
			persistedRules.set(existingIndex, ruleDefinition);
		}
		else
		{
			persistedRules.add(ruleDefinition);
		}

		persist();
	}

	public void deleteRule(String ruleId)
	{
		int existingIndex = indexOf(ruleId);

		if (existingIndex < 0)
		{
			return;
		}

		persistedRules.remove(existingIndex);
		persist();
	}

	private void persist()
	{
		store.save(new ArrayList<>(persistedRules));
		synchronizeRuntime();
	}

	private void synchronizeRuntime()
	{
		if (ruleRuntimeController != null)
		{
			ruleRuntimeController.setRules(new ArrayList<>(persistedRules));
		}
	}

	private int indexOf(String ruleId)
	{
		for (int i = 0; i < persistedRules.size(); i++)
		{
			if (Objects.equals(persistedRules.get(i).getId(), ruleId))
			{
				return i;
			}
		}

		return -1;
	}
}
