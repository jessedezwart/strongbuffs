package nl.jessedezwart.strongbuffs.panel.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;
import nl.jessedezwart.strongbuffs.RuleDefinitionStore;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;
import nl.jessedezwart.strongbuffs.runtime.engine.RuleRuntimeController;

/**
 * In-memory repository for persisted rules plus runtime synchronization.
 *
 * <p>The panel talks to this repository instead of touching the store or runtime controller
 * directly. Saving and deleting rules therefore updates persistence and the live compiled rule set
 * together.</p>
 */
@Singleton
public class RuleRepository
{
	private final RuleDefinitionStore store;
	private final RuleRuntimeController ruleRuntimeController;
	private final RuleImportService ruleImportService;
	private final List<RuleDefinition> persistedRules = new ArrayList<>();

	public RuleRepository(RuleDefinitionStore store)
	{
		this(store, null, null);
	}

	@Inject
	public RuleRepository(RuleDefinitionStore store, RuleRuntimeController ruleRuntimeController,
		RuleImportService ruleImportService)
	{
		this.store = store;
		this.ruleRuntimeController = ruleRuntimeController;
		this.ruleImportService = ruleImportService;
	}

	/**
	 * Reloads persisted rules from config storage and republishes them to the runtime layer.
	 */
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

	/**
	 * Saves or replaces one rule, then synchronizes the compiled runtime rule set.
	 */
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

	public RuleDefinition duplicateRule(String ruleId)
	{
		RuleDefinition existingRule = findById(ruleId);

		if (existingRule == null)
		{
			return null;
		}

		RuleDefinition copy = new RuleDefinition();
		copy.setSchemaVersion(existingRule.getSchemaVersion());
		copy.setId(UUID.randomUUID().toString());
		copy.setName(buildDuplicateName(existingRule.getName()));
		copy.setEnabled(existingRule.isEnabled());
		copy.setRootGroup(RuleDraft.copyGroup(existingRule.getRootGroup()));
		copy.setActivationMode(existingRule.getActivationMode());
		copy.setCooldownTicks(existingRule.getCooldownTicks());
		copy.setAction(RuleDraft.copyAction(existingRule.getAction()));
		persistedRules.add(copy);
		persist();
		return copy;
	}

	public RuleImportResult importRuleJson(String serializedRule)
	{
		if (ruleImportService == null)
		{
			return RuleImportResult.failure("Rule import is unavailable.");
		}

		RuleImportResult result = ruleImportService.importRule(serializedRule);

		if (!result.isSuccess())
		{
			return result;
		}

		persistedRules.add(result.getImportedRule());
		persist();
		return result;
	}

	private void persist()
	{
		store.save(new ArrayList<>(persistedRules));
		synchronizeRuntime();
	}

	private void synchronizeRuntime()
	{
		// The repository is the single bridge from persisted edits to the live runtime pipeline.
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

	private static String buildDuplicateName(String name)
	{
		String baseName = name == null || name.trim().isEmpty() ? "New Rule" : name.trim();
		return baseName + " Copy";
	}
}
