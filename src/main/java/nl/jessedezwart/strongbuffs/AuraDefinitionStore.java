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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import nl.jessedezwart.strongbuffs.model.AuraDefinition;
import nl.jessedezwart.strongbuffs.model.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.ConditionNode;
import nl.jessedezwart.strongbuffs.model.condition.HpCondition;
import nl.jessedezwart.strongbuffs.model.condition.PrayerPointsCondition;
import nl.jessedezwart.strongbuffs.model.condition.SpecCondition;
import nl.jessedezwart.strongbuffs.model.display.DisplayDefinition;
import nl.jessedezwart.strongbuffs.model.display.OverlayTextDisplay;
import nl.jessedezwart.strongbuffs.model.display.ScreenFlashDisplay;
import nl.jessedezwart.strongbuffs.model.display.SoundAlertDisplay;

@Slf4j
@Singleton
public class AuraDefinitionStore
{
	static final String CONFIG_GROUP = "strongbuffs";
	static final String CONFIG_KEY_AURAS = "auras";
	static final int CURRENT_SCHEMA_VERSION = 1;

	private final ConfigManager configManager;
	private final Gson gson;

	@Inject
	public AuraDefinitionStore(ConfigManager configManager)
	{
		this(configManager, createGson());
	}

	AuraDefinitionStore(ConfigManager configManager, Gson gson)
	{
		this.configManager = configManager;
		this.gson = gson;
	}

	public List<AuraDefinition> load()
	{
		return deserialize(configManager.getConfiguration(CONFIG_GROUP, CONFIG_KEY_AURAS));
	}

	public void save(List<AuraDefinition> auras)
	{
		configManager.setConfiguration(CONFIG_GROUP, CONFIG_KEY_AURAS, serialize(auras));
	}

	String serialize(List<AuraDefinition> auras)
	{
		List<AuraDefinition> sanitizedAuras = new ArrayList<>();

		if (auras != null)
		{
			for (AuraDefinition aura : auras)
			{
				if (aura == null)
				{
					continue;
				}

				aura.setSchemaVersion(CURRENT_SCHEMA_VERSION);
				sanitizedAuras.add(aura);
			}
		}

		return gson.toJson(sanitizedAuras);
	}

	List<AuraDefinition> deserialize(String serializedAuras)
	{
		if (serializedAuras == null || serializedAuras.isBlank())
		{
			return new ArrayList<>();
		}

		JsonElement root;

		try
		{
			root = new JsonParser().parse(serializedAuras);
		}
		catch (RuntimeException ex)
		{
			log.warn("Failed to parse stored auras JSON", ex);
			return new ArrayList<>();
		}

		if (!root.isJsonArray())
		{
			log.warn("Ignoring stored auras because the root JSON element was not an array");
			return new ArrayList<>();
		}

		List<AuraDefinition> auras = new ArrayList<>();
		JsonArray array = root.getAsJsonArray();

		for (JsonElement element : array)
		{
			if (!element.isJsonObject())
			{
				log.warn("Ignoring stored aura because the JSON element was not an object");
				continue;
			}

			JsonObject jsonObject = element.getAsJsonObject();

			if (!hasRequiredFields(jsonObject))
			{
				log.warn("Ignoring stored aura because required JSON fields were missing");
				continue;
			}

			try
			{
				AuraDefinition aura = gson.fromJson(jsonObject, AuraDefinition.class);
				AuraDefinition migratedAura = migrate(aura);

				if (migratedAura != null)
				{
					auras.add(migratedAura);
				}
			}
			catch (RuntimeException ex)
			{
				log.warn("Ignoring invalid stored aura definition", ex);
			}
		}

		return auras;
	}

	private AuraDefinition migrate(AuraDefinition aura)
	{
		if (aura == null)
		{
			return null;
		}

		if (aura.getSchemaVersion() > CURRENT_SCHEMA_VERSION)
		{
			log.warn("Ignoring aura {} because schema version {} is newer than supported version {}", aura.getId(),
				aura.getSchemaVersion(), CURRENT_SCHEMA_VERSION);
			return null;
		}

		if (aura.getSchemaVersion() <= 0)
		{
			aura.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		}

		if (aura.getRootGroup() == null || aura.getDisplay() == null)
		{
			log.warn("Ignoring aura {} because required fields were missing", aura.getId());
			return null;
		}

		aura.setSchemaVersion(CURRENT_SCHEMA_VERSION);
		return aura;
	}

	private static Gson createGson()
	{
		return new GsonBuilder()
			.registerTypeAdapter(ConditionNode.class, new ConditionNodeAdapter())
			.registerTypeAdapter(DisplayDefinition.class, new DisplayDefinitionAdapter())
			.create();
	}

	private static final class ConditionNodeAdapter implements JsonSerializer<ConditionNode>, JsonDeserializer<ConditionNode>
	{
		private static final String TYPE_FIELD = "type";
		private static final Map<String, Class<? extends ConditionNode>> TYPE_TO_CLASS;
		private static final Map<Class<? extends ConditionNode>, String> CLASS_TO_TYPE;

		static
		{
			Map<String, Class<? extends ConditionNode>> typeToClass = new LinkedHashMap<>();
			typeToClass.put("group", ConditionGroup.class);
			typeToClass.put("hp", HpCondition.class);
			typeToClass.put("prayer_points", PrayerPointsCondition.class);
			typeToClass.put("spec", SpecCondition.class);
			TYPE_TO_CLASS = Collections.unmodifiableMap(typeToClass);

			Map<Class<? extends ConditionNode>, String> classToType = new LinkedHashMap<>();

			for (Map.Entry<String, Class<? extends ConditionNode>> entry : TYPE_TO_CLASS.entrySet())
			{
				classToType.put(entry.getValue(), entry.getKey());
			}

			CLASS_TO_TYPE = Collections.unmodifiableMap(classToType);
		}

		@Override
		public JsonElement serialize(ConditionNode src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
			String type = CLASS_TO_TYPE.get(src.getClass());

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
			Class<? extends ConditionNode> targetClass = TYPE_TO_CLASS.get(type);

			if (targetClass == null)
			{
				throw new JsonParseException("Unsupported condition node type: " + type);
			}

			JsonObject payload = jsonObject.deepCopy();
			payload.remove(TYPE_FIELD);
			return context.deserialize(payload, targetClass);
		}
	}

	private static final class DisplayDefinitionAdapter
		implements JsonSerializer<DisplayDefinition>, JsonDeserializer<DisplayDefinition>
	{
		private static final String TYPE_FIELD = "type";
		private static final Map<String, Class<? extends DisplayDefinition>> TYPE_TO_CLASS;
		private static final Map<Class<? extends DisplayDefinition>, String> CLASS_TO_TYPE;

		static
		{
			Map<String, Class<? extends DisplayDefinition>> typeToClass = new LinkedHashMap<>();
			typeToClass.put("overlay_text", OverlayTextDisplay.class);
			typeToClass.put("screen_flash", ScreenFlashDisplay.class);
			typeToClass.put("sound_alert", SoundAlertDisplay.class);
			TYPE_TO_CLASS = Collections.unmodifiableMap(typeToClass);

			Map<Class<? extends DisplayDefinition>, String> classToType = new LinkedHashMap<>();

			for (Map.Entry<String, Class<? extends DisplayDefinition>> entry : TYPE_TO_CLASS.entrySet())
			{
				classToType.put(entry.getValue(), entry.getKey());
			}

			CLASS_TO_TYPE = Collections.unmodifiableMap(classToType);
		}

		@Override
		public JsonElement serialize(DisplayDefinition src, Type typeOfSrc, JsonSerializationContext context)
		{
			JsonObject jsonObject = context.serialize(src, src.getClass()).getAsJsonObject();
			String type = CLASS_TO_TYPE.get(src.getClass());

			if (type == null)
			{
				throw new JsonParseException("Unsupported display definition type: " + src.getClass().getName());
			}

			jsonObject.addProperty(TYPE_FIELD, type);
			return jsonObject;
		}

		@Override
		public DisplayDefinition deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		{
			JsonObject jsonObject = json.getAsJsonObject();
			String type = requireType(jsonObject, TYPE_FIELD);
			Class<? extends DisplayDefinition> targetClass = TYPE_TO_CLASS.get(type);

			if (targetClass == null)
			{
				throw new JsonParseException("Unsupported display definition type: " + type);
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
		return jsonObject.has("rootGroup") && jsonObject.has("display");
	}
}
