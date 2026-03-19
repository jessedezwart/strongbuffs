package nl.jessedezwart.strongbuffs.model.editor;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.Getter;

/**
 * Declarative description of one editable field in the Swing rule editor.
 *
 * <p>Persisted definitions emit these descriptors so the panel can stay generic instead of
 * hardcoding a custom form for every action and condition implementation.</p>
 */
@Getter
public abstract class EditorField
{
	private final String key;
	private final String label;

	protected EditorField(String key, String label)
	{
		this.key = key;
		this.label = label;
	}

	public static TextEditorField text(String key, String label, int columns, Supplier<String> getter,
		Consumer<String> setter)
	{
		return new TextEditorField(key, label, TextFieldKind.TEXT, columns, getter, setter);
	}

	public static TextEditorField color(String key, String label, int columns, Supplier<String> getter,
		Consumer<String> setter)
	{
		return new TextEditorField(key, label, TextFieldKind.COLOR, columns, getter, setter);
	}

	public static BooleanEditorField checkbox(String key, String label, Supplier<Boolean> getter,
		Consumer<Boolean> setter)
	{
		return new BooleanEditorField(key, label, getter, setter);
	}

	public static IntegerSliderEditorField slider(String key, String label, Supplier<Integer> getter,
		Consumer<Integer> setter, int minimumValue, int maximumValue, int majorTickSpacing, boolean paintTicks,
		boolean paintLabels)
	{
		return new IntegerSliderEditorField(key, label, getter, setter, minimumValue, maximumValue, majorTickSpacing,
			paintTicks, paintLabels);
	}

	public static IntegerSpinnerEditorField spinner(String key, String label, Supplier<Integer> getter,
		Consumer<Integer> setter, int minimumValue, int maximumValue, int stepSize, String suffix)
	{
		return new IntegerSpinnerEditorField(key, label, getter, setter, minimumValue, maximumValue, stepSize, suffix);
	}

	public static <T> ChoiceEditorField<T> choice(String key, String label, Supplier<T> getter, Consumer<T> setter,
		List<T> options, Function<T, String> optionLabeler)
	{
		return new ChoiceEditorField<>(key, label, getter, setter, options, optionLabeler);
	}

	public enum TextFieldKind
	{
		TEXT,
		COLOR
	}

	@Getter
	/**
	 * Text-based editor field that binds a document back to a persisted string property.
	 */
	public static final class TextEditorField extends EditorField
	{
		private final TextFieldKind kind;
		private final int columns;
		private final Supplier<String> getter;
		private final Consumer<String> setter;

		private TextEditorField(String key, String label, TextFieldKind kind, int columns, Supplier<String> getter,
			Consumer<String> setter)
		{
			super(key, label);
			this.kind = kind;
			this.columns = columns;
			this.getter = getter;
			this.setter = setter;
		}
	}

	@Getter
	/**
	 * Checkbox field for persisted boolean properties.
	 */
	public static final class BooleanEditorField extends EditorField
	{
		private final Supplier<Boolean> getter;
		private final Consumer<Boolean> setter;

		private BooleanEditorField(String key, String label, Supplier<Boolean> getter, Consumer<Boolean> setter)
		{
			super(key, label);
			this.getter = getter;
			this.setter = setter;
		}
	}

	@Getter
	/**
	 * Slider field for bounded integer values where immediate visual feedback is useful.
	 */
	public static final class IntegerSliderEditorField extends EditorField
	{
		private final Supplier<Integer> getter;
		private final Consumer<Integer> setter;
		private final int minimumValue;
		private final int maximumValue;
		private final int majorTickSpacing;
		private final boolean paintTicks;
		private final boolean paintLabels;

		private IntegerSliderEditorField(String key, String label, Supplier<Integer> getter, Consumer<Integer> setter,
			int minimumValue, int maximumValue, int majorTickSpacing, boolean paintTicks, boolean paintLabels)
		{
			super(key, label);
			this.getter = getter;
			this.setter = setter;
			this.minimumValue = minimumValue;
			this.maximumValue = maximumValue;
			this.majorTickSpacing = majorTickSpacing;
			this.paintTicks = paintTicks;
			this.paintLabels = paintLabels;
		}
	}

	@Getter
	/**
	 * Spinner field for bounded integer values that are better edited precisely.
	 */
	public static final class IntegerSpinnerEditorField extends EditorField
	{
		private final Supplier<Integer> getter;
		private final Consumer<Integer> setter;
		private final int minimumValue;
		private final int maximumValue;
		private final int stepSize;
		private final String suffix;

		private IntegerSpinnerEditorField(String key, String label, Supplier<Integer> getter, Consumer<Integer> setter,
			int minimumValue, int maximumValue, int stepSize, String suffix)
		{
			super(key, label);
			this.getter = getter;
			this.setter = setter;
			this.minimumValue = minimumValue;
			this.maximumValue = maximumValue;
			this.stepSize = stepSize;
			this.suffix = suffix;
		}
	}

	@Getter
	/**
	 * Choice field backed by a fixed option list and labeler.
	 */
	public static final class ChoiceEditorField<T> extends EditorField
	{
		private final Supplier<T> getter;
		private final Consumer<T> setter;
		private final List<T> options;
		private final Function<T, String> optionLabeler;

		private ChoiceEditorField(String key, String label, Supplier<T> getter, Consumer<T> setter, List<T> options,
			Function<T, String> optionLabeler)
		{
			super(key, label);
			this.getter = getter;
			this.setter = setter;
			this.options = Collections.unmodifiableList(options);
			this.optionLabeler = optionLabeler;
		}
	}
}
