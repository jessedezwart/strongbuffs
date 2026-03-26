package nl.jessedezwart.strongbuffs.website;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import nl.jessedezwart.strongbuffs.model.registry.DefinitionCatalog;
import org.junit.Test;

public class DefinitionManifestGeneratorTest
{
	@Test
	public void generatedScriptMatchesCommittedWebsiteManifest() throws Exception
	{
		DefinitionManifestGenerator generator = new DefinitionManifestGenerator();
		String generatedScript = generator.generateScript();
		String committedScript = Files.readString(DefinitionManifestGenerator.DEFAULT_OUTPUT_PATH, StandardCharsets.UTF_8);

		assertEquals(committedScript, generatedScript);
	}

	@Test
	public void manifestIncludesAllRegisteredDefinitions() throws Exception
	{
		DefinitionCatalog definitionCatalog = new DefinitionCatalog();
		String generatedScript = new DefinitionManifestGenerator().generateScript();
		JsonObject manifest = new JsonParser().parse(extractJson(generatedScript)).getAsJsonObject();

		assertEquals(definitionCatalog.getConditionDefinitions().size(), manifest.getAsJsonArray("conditions").size());
		assertEquals(definitionCatalog.getActionDefinitions().size(), manifest.getAsJsonArray("actions").size());
		assertTrue(manifest.has("defaultConditionType"));
		assertTrue(manifest.has("defaultActionType"));
	}

	private static String extractJson(String script)
	{
		String prefix = "window.STRONGBUFFS_MANIFEST = ";
		return script.substring(prefix.length(), script.length() - 2);
	}
}
