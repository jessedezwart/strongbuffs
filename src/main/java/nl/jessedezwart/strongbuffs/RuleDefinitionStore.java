package nl.jessedezwart.strongbuffs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.tree.ConditionNode;
import nl.jessedezwart.strongbuffs.model.registry.DefaultDefinitionCatalog;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Persists rule definitions as versioned JSON in RuneLite config storage.
 *
 * <p>The store serializes persisted model types only. Live runtime objects are rebuilt from those
 * definitions at startup so saved data stays stable, diffable, and migration-friendly.</p>
 */
@Slf4j
@Singleton
public class RuleDefinitionStore
{
	static final String CONFIG_GROUP = "strongbuffs";
	static final String CONFIG_KEY_RULES = "rules";
	static final int CURRENT_SCHEMA_VERSION = 1;

	private final ConfigManager configManager;
	private final Gson gson;

	public RuleDefinitionStore(ConfigManager configManager)
	{
		this(configManager, new DefaultDefinitionCatalog());
	}

	@Inject
	public RuleDefinitionStore(ConfigManager configManager, DefinitionCatalog definitionCatalog)
	{
		this(configManager, definitionCatalog, createGson(definitionCatalog));
	}

	RuleDefinitionStore(ConfigManager configManager, DefinitionCatalog definitionCatalog, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public List<RuleDefinition> load()
	{
		return deserialize(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_RULES));
	}

	/**
	 * Replaces the stored rule list with the provided persisted definitions.
	 */
	public void save(List<RuleDefinition> rules)
	{
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_RULES, serialize(rules));
	}

	String serialize(List<RuleDefinition> rules)
	{
		List<RuleDefinition> sanitizedRules = new ArrayList<>();

		if (rules != null)
		{
			for (RuleDefinition rule : rules)
			{
				if (rule == null)
				{
					continue;
				}

				sanitizedRules.add(copyForStorage(rule));
			}
		}

		return gson.toJson(sanitizedRules);
	}

	private static RuleDefinition copyForStorage(RuleDefinition rule)
	{
		RuleDefinition copy = new RuleDefinition();
		copy.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		copy.setId(rule.getId());
		copy.setName(rule.getName());
		copy.setEnabled(rule.isEnabled());
		copy.setRootGroup(rule.getRootGroup());
		copy.setActivationMode(rule.getActivationMode());
		copy.setCooldownTicks(rule.getCooldownTicks());
		copy.setAction(rule.getAction());
		return copy;
	}

	List<RuleDefinition> deserialize(String serializedRules)
	{
		if (serializedRules == null || serializedRules.isBlank())
		{
			return new ArrayList<>();
		}

		JsonElement root;

		try
		{
			root = new JsonParser().parse(serializedRules);
		}
		catch (RuntimeException ex)
		{
			log.warn("Failed to parse stored rules JSON", ex);
			return new ArrayList<>();
		}

		if (!root.isJsonArray())
		{
			log.warn("Ignoring stored rules because the root JSON element was not an array");
			return new ArrayList<>();
		}

		List<RuleDefinition> rules = new ArrayList<>();
		JsonArray array = root.getAsJsonArray();

		for (JsonElement element : array)
		{
			if (!element.isJsonObject())
			{
				log.warn("Ignoring stored rule because the JSON element was not an object");
				continue;
			}

			JsonObject jsonObject = element.getAsJsonObject();

			if (!hasRequiredFields(jsonObject))
			{
				log.warn("Ignoring stored rule because required JSON fields were missing");
				continue;
			}

			try
			{
				// Invalid or unknown rules are dropped wholesale so the runtime never executes a
				// partially recovered rule with ambiguous semantics.
				RuleDefinition rule = gson.fromJson(jsonObject, RuleDefinition.class);
				RuleDefinition migratedRule = migrate(rule);

				if (migratedRule != null)
				{
					rules.add(migratedRule);
				}
			}
			catch (RuntimeException ex)
			{
				log.warn("Ignoring invalid stored rule definition", ex);
			}
		}

		return rules;
	}

	private RuleDefinition migrate(RuleDefinition rule)
	{
		if (rule == null)
		{
			return null;
		}

		if (rule.getSchemaVersion() > CURRENT_SCHEMA_VERSION)
		{
			log.warn("Ignoring rule {} because schema version {} is newer than supported version {}", rule.getId(),
					rule.getSchemaVersion(), CURRENT_SCHEMA_VERSION);
			return null;
		}

		if (rule.getSchemaVersion() <= 0)
		{
			rule.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		}

		// Fail closed when required structural pieces are missing. The editor can recreate a rule
		// safely, but the runtime cannot infer intent from partial persisted data.
		if (rule.getRootGroup() == null || rule.getAction() == null)
		{
			log.warn("Ignoring rule {} because required fields were missing", rule.getId());
			return null;
		}

		rule.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		return rule;
	}

	private static Gson createGson(DefinitionCatalog definitionCatalog)
	{
		return new GsonBuilder().registerTypeAdapter(ConditionNode.class, new ConditionNodeAdapter(definitionCatalog))
			.registerTypeAdapter(ActionDefinition.class, new ActionDefinitionAdapter(definitionCatalog))
			.create();
	}

	private static final class ConditionNodeAdapter
			implements JsonSerializer<ConditionNode>, JsonDeserializer<ConditionNode>
	{
		private static final String TYPE_FIELD = "type";
		private final DefinitionCatalog definitionCatalog;

		private ConditionNodeAdapter(DefinitionCatalog definitionCatalog)
		{
			this.definitionCatalog = definitionCatalog;
		}

		@Override
		public JsonElement serialize(ConditionNode src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
			String type = src.getTypeId();

			if (type == null)
			{
				throw new JsonParseException("Unsupported condition node type: " + src.getClass().getName());
			}

			jsonObject.addProperty(TYPE_FIELD, type);
			return jsonObject;
		}

		@Override
		public ConditionNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		{
			JsonObject jsonObject = json.getAsJsonObject();
			String type = requireType(jsonObject, TYPE_FIELD);
			Class<? extends ConditionNode> targetClass = resolveConditionNodeClass(type, definitionCatalog);

			if (targetClass == null)
			{
				throw new JsonParseException("Unsupported condition node type: " + type);
			}

			// The synthetic type discriminator is used only for adapter dispatch and should not leak
			// into the target persisted model object.
			JsonObject payload = jsonObject.deepCopy();
			payload.remove(TYPE_FIELD);
			return context.deserialize(payload, targetClass);
		}
	}

	private static final class ActionDefinitionAdapter
			implements JsonSerializer<ActionDefinition>, JsonDeserializer<ActionDefinition>
	{
		private static final String TYPE_FIELD = "type";
		private final DefinitionCatalog definitionCatalog;

		private ActionDefinitionAdapter(DefinitionCatalog definitionCatalog)
		{
			this.definitionCatalog = definitionCatalog;
		}

		@Override
		public JsonElement serialize(ActionDefinition src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
			String type = src.getTypeId();

			if (type == null)
			{
				throw new JsonParseException("Unsupported action definition type: " + src.getClass().getName());
			}

			jsonObject.addProperty(TYPE_FIELD, type);
			return jsonObject;
		}

		@Override
		public ActionDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		{
			JsonObject jsonObject = json.getAsJsonObject();
			String type = requireType(jsonObject, TYPE_FIELD);
			Class<? extends ActionDefinition> targetClass = resolveActionDefinitionClass(type, definitionCatalog);

			if (targetClass == null)
			{
				throw new JsonParseException("Unsupported action definition type: " + type);
			}

			JsonObject payload = jsonObject.deepCopy();
			payload.remove(TYPE_FIELD);
			return context.deserialize(payload, targetClass);
		}
	}

	private static String requireType(JsonObject jsonObject, String fieldName)
	{
		JsonElement typeElement = jsonObject.get(fieldName);

		if (typeElement == null || !typeElement.isJsonPrimitive())
		{
			throw new JsonParseException("Missing required type field: " + fieldName);
		}

		return typeElement.getAsString();
	}

	private static boolean hasRequiredFields(JsonObject jsonObject)
	{
		return jsonObject.has("rootGroup") && jsonObject.has("action");
	}

	private static Class<? extends ConditionNode> resolveConditionNodeClass(String type,
		DefinitionCatalog definitionCatalog)
	{
		if (type == null || type.isEmpty())
		{
			return null;
		}

		ConditionGroup group = new ConditionGroup();

		if (group.getTypeId().equals(type))
		{
			return ConditionGroup.class;
		}

		try
		{
			return definitionCatalog.getConditionDefinitionClass(type);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}

	private static Class<? extends ActionDefinition> resolveActionDefinitionClass(String type,
		DefinitionCatalog definitionCatalog)
	{
		if (type == null || type.isEmpty())
		{
			return null;
		}

		try
		{
			return definitionCatalog.getActionDefinitionClass(type);
		}
		catch (IllegalArgumentException ex)
		{
			return null;
		}
	}
}
