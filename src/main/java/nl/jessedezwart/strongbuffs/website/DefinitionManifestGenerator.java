package nl.jessedezwart.strongbuffs.website;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import nl.jessedezwart.strongbuffs.RuleJsonCodec;
import nl.jessedezwart.strongbuffs.model.EditorField;
import nl.jessedezwart.strongbuffs.model.action.ActionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionDefinition;
import nl.jessedezwart.strongbuffs.model.condition.ConditionGroup;
import nl.jessedezwart.strongbuffs.model.condition.ConditionNode;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import nl.jessedezwart.strongbuffs.model.rule.ActivationMode;

/**
 * Generates the website manifest consumed by the static rule builder.
 */
public class DefinitionManifestGenerator
{
	public static final Path DEFAULT_OUTPUT_PATH = Path.of("website/generated/definition-manifest.js");

	private final DefinitionCatalog definitionCatalog;
	private final Gson gson;

	public DefinitionManifestGenerator()
	{
		this(new DefinitionCatalog());
	}

	DefinitionManifestGenerator(DefinitionCatalog definitionCatalog)
	{
		this.definitionCatalog = definitionCatalog;
		this.gson = RuleJsonCodec.createGson(definitionCatalog);
	}

	public static void main(String[] args) throws IOException
	{
		boolean checkOnly = false;
		Path outputPath = DEFAULT_OUTPUT_PATH;

		for (String arg : args)
		{
			if ("--check".equals(arg))
			{
				checkOnly = true;
			}
			else
			{
				outputPath = Path.of(arg);
			}
		}

		DefinitionManifestGenerator generator = new DefinitionManifestGenerator();
		String script = generator.generateScript();

		if (checkOnly)
		{
			String existing = Files.exists(outputPath)
				? Files.readString(outputPath, StandardCharsets.UTF_8)
				: "";

			if (!script.equals(existing))
			{
				throw new IllegalStateException(outputPath + " is stale. Run generateWebsiteManifest.");
			}

			return;
		}

		Files.createDirectories(outputPath.getParent());
		Files.writeString(outputPath, script, StandardCharsets.UTF_8);
	}

	public String generateScript()
	{
		return "window.STRONGBUFFS_MANIFEST = " + gson.toJson(buildManifest()) + ";\n";
	}

	private JsonObject buildManifest()
	{
		JsonObject manifest = new JsonObject();
		List<Class<? extends ConditionDefinition>> conditionClasses = sortedConditionClasses();
		List<Class<? extends ActionDefinition>> actionClasses = sortedActionClasses();

		manifest.addProperty("schemaVersion", RuleJsonCodec.CURRENT_SCHEMA_VERSION);
		manifest.add("activationModes", buildActivationModes());
		manifest.add("groupDefaults", serializeConditionNode(new ConditionGroup()));
		manifest.add("conditions", buildConditionDefinitions(conditionClasses));
		manifest.add("actions", buildActionDefinitions(actionClasses));
		manifest.addProperty("defaultConditionType", definitionCatalog.getConditionMetadata(conditionClasses.get(0)).getTypeId());
		manifest.addProperty("defaultActionType", definitionCatalog.getActionMetadata(actionClasses.get(0)).getTypeId());
		return manifest;
	}

	private JsonArray buildActivationModes()
	{
		JsonArray activationModes = new JsonArray();

		for (ActivationMode activationMode : ActivationMode.values())
		{
			JsonObject item = new JsonObject();
			item.addProperty("value", activationMode.name());
			item.addProperty("label", formatEnumLabel(activationMode.name()));
			activationModes.add(item);
		}

		return activationModes;
	}

	private JsonArray buildConditionDefinitions(List<Class<? extends ConditionDefinition>> conditionClasses)
	{
		JsonArray definitions = new JsonArray();

		for (Class<? extends ConditionDefinition> conditionClass : conditionClasses)
		{
			ConditionDefinition metadata = definitionCatalog.getConditionMetadata(conditionClass);
			ConditionDefinition defaultDefinition = definitionCatalog.createCondition(conditionClass);
			JsonObject item = new JsonObject();
			item.addProperty("typeId", metadata.getTypeId());
			item.addProperty("label", metadata.getEditorLabel());
			item.addProperty("description", metadata.getEditorDescription());
			item.add("defaults", serializeConditionNode(defaultDefinition));
			item.add("fields", buildFields(metadata.getEditorFields()));
			definitions.add(item);
		}

		return definitions;
	}

	private JsonArray buildActionDefinitions(List<Class<? extends ActionDefinition>> actionClasses)
	{
		JsonArray definitions = new JsonArray();

		for (Class<? extends ActionDefinition> actionClass : actionClasses)
		{
			ActionDefinition metadata = definitionCatalog.getActionMetadata(actionClass);
			ActionDefinition defaultDefinition = definitionCatalog.createAction(actionClass);
			JsonObject item = new JsonObject();
			item.addProperty("typeId", metadata.getTypeId());
			item.addProperty("label", metadata.getEditorLabel());
			item.addProperty("description", metadata.getEditorDescription());
			item.add("defaults", serializeAction(defaultDefinition));
			item.add("fields", buildFields(metadata.getEditorFields()));
			definitions.add(item);
		}

		return definitions;
	}

	private JsonArray buildFields(List<EditorField> fields)
	{
		JsonArray items = new JsonArray();

		for (EditorField field : fields)
		{
			JsonObject item = new JsonObject();
			item.addProperty("key", field.getKey());
			item.addProperty("label", field.getLabel());

			if (field instanceof EditorField.TextEditorField)
			{
				EditorField.TextEditorField textField = (EditorField.TextEditorField) field;
				item.addProperty("kind", textField.getKind().name().toLowerCase());
				item.addProperty("columns", textField.getColumns());
				item.add("defaultValue", toSerializedValue(textField.getGetter().get()));
			}
			else if (field instanceof EditorField.BooleanEditorField)
			{
				EditorField.BooleanEditorField booleanField = (EditorField.BooleanEditorField) field;
				item.addProperty("kind", "checkbox");
				item.add("defaultValue", toSerializedValue(booleanField.getGetter().get()));
			}
			else if (field instanceof EditorField.IntegerSliderEditorField)
			{
				EditorField.IntegerSliderEditorField sliderField = (EditorField.IntegerSliderEditorField) field;
				item.addProperty("kind", "slider");
				item.add("defaultValue", toSerializedValue(sliderField.getGetter().get()));
				item.addProperty("minimumValue", sliderField.getMinimumValue());
				item.addProperty("maximumValue", sliderField.getMaximumValue());
				item.addProperty("majorTickSpacing", sliderField.getMajorTickSpacing());
				item.addProperty("paintTicks", sliderField.isPaintTicks());
				item.addProperty("paintLabels", sliderField.isPaintLabels());
			}
			else if (field instanceof EditorField.IntegerSpinnerEditorField)
			{
				EditorField.IntegerSpinnerEditorField spinnerField = (EditorField.IntegerSpinnerEditorField) field;
				item.addProperty("kind", "spinner");
				item.add("defaultValue", toSerializedValue(spinnerField.getGetter().get()));
				item.addProperty("minimumValue", spinnerField.getMinimumValue());
				item.addProperty("maximumValue", spinnerField.getMaximumValue());
				item.addProperty("stepSize", spinnerField.getStepSize());
				item.addProperty("suffix", spinnerField.getSuffix());
			}
			else if (field instanceof EditorField.ChoiceEditorField)
			{
				appendChoiceField(item, field);
			}
			else
			{
				throw new IllegalArgumentException("Unsupported editor field: " + field.getClass().getName());
			}

			items.add(item);
		}

		return items;
	}

	@SuppressWarnings("unchecked")
	private void appendChoiceField(JsonObject item, EditorField field)
	{
		appendChoiceFieldTyped(item, (EditorField.ChoiceEditorField<Object>) field);
	}

	private void appendChoiceFieldTyped(JsonObject item, EditorField.ChoiceEditorField<Object> choiceField)
	{
		item.addProperty("kind", "choice");
		item.add("defaultValue", toSerializedValue(choiceField.getGetter().get()));

		JsonArray options = new JsonArray();

		for (Object option : choiceField.getOptions())
		{
			JsonObject optionJson = new JsonObject();
			optionJson.add("value", toSerializedValue(option));
			optionJson.addProperty("label", choiceField.getOptionLabeler().apply(option));
			options.add(optionJson);
		}

		item.add("options", options);
	}

	private JsonObject serializeConditionNode(ConditionNode conditionNode)
	{
		return gson.toJsonTree(conditionNode, ConditionNode.class).getAsJsonObject();
	}

	private JsonObject serializeAction(ActionDefinition actionDefinition)
	{
		return gson.toJsonTree(actionDefinition, ActionDefinition.class).getAsJsonObject();
	}

	private List<Class<? extends ConditionDefinition>> sortedConditionClasses()
	{
		List<Class<? extends ConditionDefinition>> conditionClasses =
			new ArrayList<>(definitionCatalog.getConditionDefinitions());
		conditionClasses.sort(Comparator.comparing(
			conditionClass -> definitionCatalog.getConditionMetadata(conditionClass).getEditorLabel()));
		return conditionClasses;
	}

	private List<Class<? extends ActionDefinition>> sortedActionClasses()
	{
		List<Class<? extends ActionDefinition>> actionClasses =
			new ArrayList<>(definitionCatalog.getActionDefinitions());
		actionClasses.sort(Comparator.comparing(
			actionClass -> definitionCatalog.getActionMetadata(actionClass).getEditorLabel()));
		return actionClasses;
	}

	private static JsonElement toSerializedValue(Object value)
	{
		if (value == null)
		{
			return JsonNull.INSTANCE;
		}

		if (value instanceof Enum<?>)
		{
			return new JsonPrimitive(((Enum<?>) value).name());
		}

		if (value instanceof Number)
		{
			return new JsonPrimitive((Number) value);
		}

		if (value instanceof Boolean)
		{
			return new JsonPrimitive((Boolean) value);
		}

		if (value instanceof Character)
		{
			return new JsonPrimitive((Character) value);
		}

		return new JsonPrimitive(String.valueOf(value));
	}

	private static String formatEnumLabel(String value)
	{
		String normalized = value.toLowerCase().replace('_', ' ');
		String[] words = normalized.split(" ");
		StringBuilder builder = new StringBuilder();

		for (String word : words)
		{
			if (builder.length() > 0)
			{
				builder.append(' ');
			}

			builder.append(Character.toUpperCase(word.charAt(0)));
			builder.append(word.substring(1));
		}

		return builder.toString();
	}
}
