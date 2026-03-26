package nl.jessedezwart.strongbuffs;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Persists rule definitions as versioned JSON in RuneLite config storage.
 *
 * <p>The store serializes persisted model types only. Live runtime objects are rebuilt from those
 * definitions at startup so saved data stays stable, diffable, and migration-friendly.</p>
 */
@Singleton
public class RuleDefinitionStore
{
	static final String CONFIG_GROUP = "strongbuffs";
	static final String CONFIG_KEY_RULES = "rules";
	static final int CURRENT_SCHEMA_VERSION = RuleJsonCodec.CURRENT_SCHEMA_VERSION;

	private final ConfigManager configManager;
	private final RuleJsonCodec ruleJsonCodec;

	public RuleDefinitionStore(ConfigManager configManager)
	{
		this(configManager, new RuleJsonCodec());
	}

	@Inject
	public RuleDefinitionStore(ConfigManager configManager, RuleJsonCodec ruleJsonCodec)
	{
		this.configManager = configManager;
		this.ruleJsonCodec = ruleJsonCodec;
	}

	public List<RuleDefinition> load()
	{
		return deserialize(configManager == null ? null : configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_RULES));
	}

	/**
	 * Replaces the stored rule list with the provided persisted definitions.
	 */
	public void save(List<RuleDefinition> rules)
	{
		if (configManager == null)
		{
			return;
		}

		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_RULES, serialize(rules));
	}

	String serialize(List<RuleDefinition> rules)
	{
		return ruleJsonCodec.serializeRules(rules);
	}

	List<RuleDefinition> deserialize(String serializedRules)
	{
		return ruleJsonCodec.deserializeRules(serializedRules);
	}
}
