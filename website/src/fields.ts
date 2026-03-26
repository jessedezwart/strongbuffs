import { createFieldGrid, createFieldShell, assignFocusId } from "./dom";
import { normalizeNumber, parseOptionValue, prettyLabel } from "./format";
import { isManifestFieldValid } from "./validation";
import type { DefinitionNode, ManifestField, SelectOption } from "./types";

type ValueValidator = (value: unknown) => boolean;

interface TextFieldOptions
{
	type?: string;
	invalid?: boolean;
	placeholder?: string;
	validate?: ValueValidator;
}

interface NumberFieldOptions
{
	min?: number;
	max?: number;
	step?: number;
	suffix?: string;
	invalid?: boolean;
	validate?: ValueValidator;
}

interface RangeFieldOptions
{
	min?: number;
	max?: number;
	step?: number;
	invalid?: boolean;
	validate?: ValueValidator;
}

export function createTextField(
	label: string,
	value: unknown,
	onChange: (value: string) => void,
	options: TextFieldOptions = {}
): HTMLLabelElement
{
	const field = createFieldShell(label);
	const input = document.createElement("input");
	const hasValidator = typeof options.validate === "function";
	assignFocusId(input);
	input.type = options.type || "text";
	input.value = value == null ? "" : String(value);

	if (options.invalid)
	{
		input.classList.add("input--invalid");
	}

	if (options.placeholder)
	{
		input.placeholder = options.placeholder;
	}

	input.addEventListener("input", function ()
	{
		if (hasValidator)
		{
			input.classList.toggle("input--invalid", !options.validate?.(input.value));
		}

		onChange(input.value);
	});

	field.appendChild(input);
	return field;
}

export function createCheckboxField(
	label: string,
	value: unknown,
	onChange: (value: boolean) => void
): HTMLLabelElement
{
	const field = document.createElement("label");
	field.className = "field field--inline-checkbox";
	const input = document.createElement("input");
	assignFocusId(input);
	input.type = "checkbox";
	input.checked = Boolean(value);
	input.addEventListener("change", function ()
	{
		onChange(input.checked);
	});
	field.appendChild(input);

	const caption = document.createElement("span");
	caption.textContent = label;
	field.appendChild(caption);
	return field;
}

export function createSelect<T>(
	options: SelectOption<T>[],
	value: unknown,
	onChange: (value: T | string) => void,
	invalid?: boolean | ValueValidator
): HTMLSelectElement
{
	const select = document.createElement("select");
	const validate = typeof invalid === "function" ? invalid : null;
	assignFocusId(select);

	if (invalid && !validate)
	{
		select.classList.add("input--invalid");
	}

	options.forEach(function (option)
	{
		const element = document.createElement("option");
		element.value = String(option.value);
		element.textContent = option.label;
		element.selected = String(option.value) === String(value);
		select.appendChild(element);
	});

	select.addEventListener("change", function ()
	{
		if (validate)
		{
			select.classList.toggle("input--invalid", !validate(select.value));
		}

		onChange(parseOptionValue(options, select.value));
	});

	return select;
}

export function createSelectField<T>(
	label: string,
	options: SelectOption<T>[],
	value: unknown,
	onChange: (value: T | string) => void,
	invalid?: boolean | ValueValidator
): HTMLLabelElement
{
	const field = createFieldShell(label);
	field.appendChild(createSelect(options, value, onChange, invalid));
	return field;
}

export function createNumberField(
	label: string,
	value: number,
	onChange: (value: number) => void,
	options: NumberFieldOptions = {}
): HTMLLabelElement
{
	const field = createFieldShell(label);
	const wrapper = document.createElement("div");
	wrapper.className = "field__row";
	const input = document.createElement("input");
	const hasValidator = typeof options.validate === "function";
	assignFocusId(input);
	input.type = "number";
	input.value = String(value);

	if (options.invalid)
	{
		input.classList.add("input--invalid");
	}

	if (options.min !== undefined)
	{
		input.min = String(options.min);
	}

	if (options.max !== undefined)
	{
		input.max = String(options.max);
	}

	if (options.step !== undefined)
	{
		input.step = String(options.step);
	}

	input.addEventListener("input", function ()
	{
		if (hasValidator)
		{
			input.classList.toggle(
				"input--invalid",
				!options.validate?.(Number(input.value))
			);
		}

		onChange(normalizeNumber(input.value, value));
	});

	wrapper.appendChild(input);

	if (options.suffix)
	{
		const suffix = document.createElement("span");
		suffix.className = "field__hint";
		suffix.textContent = options.suffix;
		wrapper.appendChild(suffix);
	}

	field.appendChild(wrapper);
	return field;
}

export function createRangeField(
	label: string,
	value: number,
	onChange: (value: number) => void,
	options: RangeFieldOptions
): HTMLLabelElement
{
	const field = createFieldShell(label);
	const wrapper = document.createElement("div");
	wrapper.className = "field";
	const input = document.createElement("input");
	const hasValidator = typeof options.validate === "function";
	assignFocusId(input);
	input.type = "range";
	input.min = String(options.min ?? 0);
	input.max = String(options.max ?? 0);
	input.step = String(options.step ?? 1);
	input.value = String(value);

	if (options.invalid)
	{
		input.classList.add("input--invalid");
	}

	const hint = document.createElement("span");
	hint.className = "field__hint";
	hint.textContent = String(value);

	input.addEventListener("input", function ()
	{
		const nextValue = normalizeNumber(input.value, value);
		hint.textContent = String(nextValue);

		if (hasValidator)
		{
			input.classList.toggle("input--invalid", !options.validate?.(nextValue));
		}

		onChange(nextValue);
	});

	wrapper.appendChild(input);
	wrapper.appendChild(hint);
	field.appendChild(wrapper);
	return field;
}

export function createManifestField(
	field: ManifestField,
	currentValue: unknown,
	onChange: (value: unknown) => void
): HTMLElement
{
	const label = field.label || prettyLabel(field.key);
	const validate = function (value: unknown): boolean
	{
		return isManifestFieldValid(field, value);
	};

	if (field.kind === "text" || field.kind === "color")
	{
		return createTextField(label, currentValue, onChange, {
			type: field.kind === "color" ? "color" : "text",
			validate: validate
		});
	}

	if (field.kind === "checkbox")
	{
		return createCheckboxField(label, Boolean(currentValue), onChange);
	}

	if (field.kind === "choice")
	{
		return createSelectField(
			label,
			field.options || [],
			currentValue,
			onChange,
			validate
		);
	}

	if (field.kind === "spinner")
	{
		return createNumberField(label, Number(currentValue), onChange, {
			min: field.minimumValue,
			max: field.maximumValue,
			step: field.stepSize,
			suffix: field.suffix,
			validate: validate
		});
	}

	if (field.kind === "slider")
	{
		return createRangeField(label, Number(currentValue), onChange, {
			min: field.minimumValue,
			max: field.maximumValue,
			step: 1,
			validate: validate
		});
	}

	return document.createElement("div");
}

export function createFieldsBlock(
	fields: ManifestField[],
	target: DefinitionNode,
	onChange: (key: string, value: unknown) => void
): HTMLDivElement
{
	const grid = createFieldGrid();

	fields.forEach(function (field)
	{
		grid.appendChild(
			createManifestField(field, target[field.key], function (value)
			{
				onChange(field.key, value);
			})
		);
	});

	return grid;
}
