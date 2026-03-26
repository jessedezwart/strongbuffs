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
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import nl.jessedezwart.strongbuffs.model.rule.RuleDefinition;

/**
 * Shared JSON codec for persisted rules and single-rule imports.
 */
@Slf4j
@Singleton
public class RuleJsonCodec
{
	public static final int CURRENT_SCHEMA_VERSION = 1;
	private static final String TYPE_FIELD = "type";
	private static final String GROUP_TYPE = "group";
	private final Gson gson;

	public RuleJsonCodec()
	{
		this(new DefinitionCatalog());
	}

	@Inject
	public RuleJsonCodec(DefinitionCatalog definitionCatalog)
	{
		this.gson = createGson(definitionCatalog);
	}

	public String serializeRules(List<RuleDefinition> rules)
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

	public String serializeRule(RuleDefinition rule)
	{
		if (rule == null)
		{
			throw new IllegalArgumentException("Rule is required.");
		}

		return gson.toJson(copyForStorage(rule));
	}

	public List<RuleDefinition> deserializeRules(String serializedRules)
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

			try
			{
				rules.add(parseRuleObject(element.getAsJsonObject()));
			}
			catch (IllegalArgumentException ex)
			{
				log.warn("Ignoring invalid stored rule definition", ex);
			}
		}

		return rules;
	}

	public RuleDefinition deserializeRule(String serializedRule)
	{
		if (serializedRule == null || serializedRule.isBlank())
		{
			throw new IllegalArgumentException("Paste a rule JSON object.");
		}

		JsonElement root;

		try
		{
			root = new JsonParser().parse(serializedRule);
		}
		catch (RuntimeException ex)
		{
			throw new IllegalArgumentException("Rule JSON is not valid JSON.", ex);
		}

		if (!root.isJsonObject())
		{
			throw new IllegalArgumentException("Rule JSON must be a single object.");
		}

		return parseRuleObject(root.getAsJsonObject());
	}

	public static Gson createGson(DefinitionCatalog definitionCatalog)
	{
		return new GsonBuilder()
			.registerTypeAdapter(ConditionNode.class, new ConditionNodeAdapter(definitionCatalog))
			.registerTypeAdapter(ActionDefinition.class, new ActionDefinitionAdapter(definitionCatalog))
			.create();
	}

	private RuleDefinition parseRuleObject(JsonObject jsonObject)
	{
		if (!hasRequiredFields(jsonObject))
		{
			throw new IllegalArgumentException("Rule JSON must include rootGroup and action.");
		}

		try
		{
			RuleDefinition rule = gson.fromJson(jsonObject, RuleDefinition.class);
			return migrateOrThrow(rule);
		}
		catch (IllegalArgumentException ex)
		{
			throw ex;
		}
		catch (RuntimeException ex)
		{
			throw new IllegalArgumentException("Rule JSON contains unsupported or invalid values.", ex);
		}
	}

	private RuleDefinition migrateOrThrow(RuleDefinition rule)
	{
		if (rule == null)
		{
			throw new IllegalArgumentException("Rule JSON could not be read.");
		}

		if (rule.getSchemaVersion() > CURRENT_SCHEMA_VERSION)
		{
			throw new IllegalArgumentException(
				"Rule schema version " + rule.getSchemaVersion() + " is newer than this plugin supports.");
		}

		if (rule.getSchemaVersion() <= 0)
		{
			rule.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		}

		if (rule.getRootGroup() == null || rule.getAction() == null)
		{
			throw new IllegalArgumentException("Rule JSON must include rootGroup and action.");
		}

		rule.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		return rule;
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

		if (GROUP_TYPE.equals(type))
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

	private static String requireType(JsonObject jsonObject)
	{
		JsonElement typeElement = jsonObject.get(TYPE_FIELD);

		if (typeElement == null || !typeElement.isJsonPrimitive())
		{
			throw new JsonParseException("Missing required type field: " + TYPE_FIELD);
		}

		return typeElement.getAsString();
	}

	private static final class ConditionNodeAdapter implements JsonSerializer<ConditionNode>, JsonDeserializer<ConditionNode>
	{
		private final DefinitionCatalog definitionCatalog;

		private ConditionNodeAdapter(DefinitionCatalog definitionCatalog)
		{
			this.definitionCatalog = definitionCatalog;
		}

		@Override
		public JsonElement serialize(ConditionNode src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
			String type;

			if (src instanceof ConditionGroup)
			{
				type = GROUP_TYPE;
			}
			else if (src instanceof ConditionDefinition)
			{
				type = ((ConditionDefinition) src).getTypeId();
			}
			else
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
			String type = requireType(jsonObject);
			Class<? extends ConditionNode> targetClass = resolveConditionNodeClass(type, definitionCatalog);

			if (targetClass == null)
			{
				throw new JsonParseException("Unsupported condition node type: " + type);
			}

			JsonObject payload = jsonObject.deepCopy();
			payload.remove(TYPE_FIELD);
			return context.deserialize(payload, targetClass);
		}
	}

	private static final class ActionDefinitionAdapter
		implements JsonSerializer<ActionDefinition>, JsonDeserializer<ActionDefinition>
	{
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
			String type = requireType(jsonObject);
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
}
