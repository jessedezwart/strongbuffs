package nl.jessedezwart.strongbuffs.model.registry;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class DefinitionRegistryConsistencyTest
{
	private static final Path PROJECT_ROOT = Path.of("").toAbsolutePath();
	private static final Path ACTION_IMPL_DIR = PROJECT_ROOT.resolve(
		"src/main/java/nl/jessedezwart/strongbuffs/model/action/impl");
	private static final Path CONDITION_IMPL_DIR = PROJECT_ROOT.resolve(
		"src/main/java/nl/jessedezwart/strongbuffs/model/condition/impl");

	@Test
	public void actionImplsMatchDefinitionRegistry() throws IOException
	{
		assertEquals(readJavaClassNames(ACTION_IMPL_DIR), DefinitionRegistry.getActionDefinitions().stream()
			.map(Class::getName)
			.sorted()
			.collect(Collectors.toList()));
	}

	@Test
	public void conditionImplsMatchDefinitionRegistry() throws IOException
	{
		assertEquals(readJavaClassNames(CONDITION_IMPL_DIR), DefinitionRegistry.getConditionDefinitions().stream()
			.map(Class::getName)
			.sorted()
			.collect(Collectors.toList()));
	}

	private static List<String> readJavaClassNames(Path directory) throws IOException
	{
		try (Stream<Path> stream = Files.list(directory))
		{
			return stream
				.filter(path -> path.getFileName().toString().endsWith(".java"))
				.map(DefinitionRegistryConsistencyTest::toClassName)
				.sorted()
				.collect(Collectors.toList());
		}
	}

	private static String toClassName(Path path)
	{
		String normalized = PROJECT_ROOT.relativize(path).toString().replace('\\', '/');
		String withoutPrefix = normalized.replaceFirst("^src/main/java/", "");
		return withoutPrefix.substring(0, withoutPrefix.length() - ".java".length()).replace('/', '.');
	}
}
