package nl.jessedezwart.strongbuffs.model.registry;

import java.util.function.Supplier;

/**
 * Groups the three pieces needed per definition type: the class for type resolution,
 * a shared metadata instance for the editor, and a factory for creating new drafts.
 */
final class DefinitionRegistration<T>
{
	private final Class<? extends T> definitionClass;
	private final T metadata;
	private final Supplier<? extends T> factory;

	DefinitionRegistration(Class<? extends T> definitionClass, T metadata, Supplier<? extends T> factory)
	{
		this.definitionClass = definitionClass;
		this.metadata = metadata;
		this.factory = factory;
	}

	Class<? extends T> getDefinitionClass()
	{
		return definitionClass;
	}

	T getMetadata()
	{
		return metadata;
	}

	T create()
	{
		return factory.get();
	}
}
