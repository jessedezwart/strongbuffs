(function ()
{
	"use strict";

	const manifest = window.STRONGBUFFS_MANIFEST;

	if (!manifest)
	{
		return;
	}

	const conditionMap = toMap(manifest.conditions, "typeId");
	const actionMap = toMap(manifest.actions, "typeId");
	const dom = {
		basics: document.getElementById("basics-section"),
		conditions: document.getElementById("conditions-section"),
		activation: document.getElementById("activation-section"),
		action: document.getElementById("action-section"),
		status: document.getElementById("status-box"),
		json: document.getElementById("json-output"),
		copy: document.getElementById("copy-json"),
		download: document.getElementById("download-json"),
		reset: document.getElementById("reset-rule")
	};

	let state = createRuleState();
	let validation = validateRule(state);

	dom.copy.addEventListener("click", copyJson);
	dom.download.addEventListener("click", downloadJson);
	dom.reset.addEventListener("click", function ()
	{
		state = createRuleState();
		render();
	});

	render();

	function render()
	{
		validation = validateRule(state);
		renderBasics();
		renderConditions();
		renderActivation();
		renderAction();
		renderOutput();
	}

	function renderBasics()
	{
		const section = createSection("Basics", "Rule name and enabled state.");
		const grid = createFieldGrid();

		grid.appendChild(createTextField("Rule name", state.name, function (value)
		{
			state.name = value;
			render();
		}, { placeholder: "Low HP Warning" }));

		grid.appendChild(createCheckboxField("Enabled", state.enabled, function (value)
		{
			state.enabled = value;
			render();
		}));

		section.appendChild(grid);
		replaceChildren(dom.basics, section);
	}

	function renderConditions()
	{
		const section = createSection("Conditions", "Build nested AND / OR groups.");
		const root = document.createElement("div");
		root.className = "conditions-root";
		root.appendChild(renderGroup(state.rootGroup, []));
		section.appendChild(root);
		replaceChildren(dom.conditions, section);
	}

	function renderActivation()
	{
		const section = createSection("Activation", "Choose when the rule should fire.");
		const grid = createFieldGrid();

		grid.appendChild(createSelectField("Mode", manifest.activationModes, state.activationMode, function (value)
		{
			state.activationMode = value;
			render();
		}));

		grid.appendChild(createNumberField("Cooldown ticks", state.cooldownTicks, function (value)
		{
			state.cooldownTicks = value;
			render();
		}, { min: 0, max: 10000, step: 1 }));

		section.appendChild(grid);
		replaceChildren(dom.activation, section);
	}

	function renderAction()
	{
		const section = createSection("Action", "Select one action payload.");
		const actionMeta = actionMap[state.action.type];

		section.appendChild(createSelectField("Action type", manifest.actions.map(function (item)
		{
			return { value: item.typeId, label: item.label };
		}), state.action.type, function (value)
		{
			state.action = createDefinitionFromManifest(actionMap[value]);
			render();
		}));

		const card = document.createElement("div");
		card.className = "leaf-card";
		card.appendChild(createMetaHeader(actionMeta.label, actionMeta.description));
		card.appendChild(createFieldsBlock(actionMeta.fields, state.action, function (key, value)
		{
			state.action[key] = value;
			render();
		}));
		section.appendChild(card);
		replaceChildren(dom.action, section);
	}

	function renderOutput()
	{
		const exportedRule = createExportedRule();
		const json = JSON.stringify(exportedRule, null, 2);
		dom.json.textContent = json;
		dom.copy.disabled = !validation.isValid;
		dom.download.disabled = !validation.isValid;

		dom.status.className = validation.isValid ? "status" : "status error";
		dom.status.innerHTML = "";

		const title = document.createElement("strong");
		title.textContent = validation.isValid ? "Ready to import" : "Fix these issues first";
		dom.status.appendChild(title);

		if (validation.isValid)
		{
			const description = document.createElement("p");
			description.textContent = "Copy the JSON and paste it into Strong Buffs with Import JSON.";
			dom.status.appendChild(description);
			return;
		}

		const list = document.createElement("ul");

		validation.errors.forEach(function (error)
		{
			const item = document.createElement("li");
			item.textContent = error;
			list.appendChild(item);
		});

		dom.status.appendChild(list);
	}

	function renderGroup(group, path)
	{
		const card = document.createElement("div");
		card.className = "group-card";

		const header = document.createElement("div");
		header.className = "group-header";
		header.appendChild(createMetaHeader(path.length === 0 ? "Root group" : "Condition group",
			"Each group matches either every child or at least one child."));

		const logicChip = document.createElement("div");
		logicChip.className = "logic-chip";
		logicChip.appendChild(createInlineLabel("Logic"));
		logicChip.appendChild(createSelect(manifestValueOptions(["AND", "OR"]), group.logic, function (value)
		{
			group.logic = value;
			render();
		}));
		header.appendChild(logicChip);
		card.appendChild(header);

		const toolbar = document.createElement("div");
		toolbar.className = "toolbar";
		toolbar.appendChild(createAddConditionControl(group));

		const addGroupButton = document.createElement("button");
		addGroupButton.type = "button";
		addGroupButton.className = "secondary";
		addGroupButton.textContent = "Add group";
		addGroupButton.addEventListener("click", function ()
		{
			group.children.push(clone(manifest.groupDefaults));
			render();
		});
		toolbar.appendChild(addGroupButton);

		if (path.length > 0)
		{
			const removeButton = document.createElement("button");
			removeButton.type = "button";
			removeButton.className = "secondary";
			removeButton.textContent = "Remove group";
			removeButton.addEventListener("click", function ()
			{
				removeNode(path);
				render();
			});
			toolbar.appendChild(removeButton);
		}

		card.appendChild(toolbar);

		if (group.children.length === 0)
		{
			const empty = document.createElement("p");
			empty.className = "field-hint";
			empty.textContent = "Add at least one condition or nested group.";
			card.appendChild(empty);
		}

		group.children.forEach(function (child, index)
		{
			const childPath = path.concat(index);

			if (child.type === "group")
			{
				card.appendChild(renderGroup(child, childPath));
			}
			else
			{
				card.appendChild(renderCondition(child, childPath));
			}
		});

		return card;
	}

	function renderCondition(condition, path)
	{
		const meta = conditionMap[condition.type];
		const card = document.createElement("div");
		card.className = "leaf-card";
		card.appendChild(createMetaHeader(meta.label, meta.description));
		card.appendChild(createFieldsBlock(meta.fields, condition, function (key, value)
		{
			condition[key] = value;
			render();
		}));

		const toolbar = document.createElement("div");
		toolbar.className = "leaf-toolbar";
		const removeButton = document.createElement("button");
		removeButton.type = "button";
		removeButton.className = "secondary";
		removeButton.textContent = "Remove condition";
		removeButton.addEventListener("click", function ()
		{
			removeNode(path);
			render();
		});
		toolbar.appendChild(removeButton);
		card.appendChild(toolbar);
		return card;
	}

	function createFieldsBlock(fields, target, onChange)
	{
		const grid = createFieldGrid();

		fields.forEach(function (field)
		{
			grid.appendChild(createManifestField(field, target[field.key], function (value)
			{
				onChange(field.key, value);
			}));
		});

		return grid;
	}

	function createManifestField(field, currentValue, onChange)
	{
		const label = field.label || prettyLabel(field.key);

		if (field.kind === "text" || field.kind === "color")
		{
			return createTextField(label, currentValue, onChange, {
				type: field.kind === "color" ? "color" : "text"
			});
		}

		if (field.kind === "checkbox")
		{
			return createCheckboxField(label, Boolean(currentValue), onChange);
		}

		if (field.kind === "choice")
		{
			return createSelectField(label, field.options, currentValue, onChange);
		}

		if (field.kind === "spinner")
		{
			return createNumberField(label, currentValue, onChange, {
				min: field.minimumValue,
				max: field.maximumValue,
				step: field.stepSize,
				suffix: field.suffix
			});
		}

		if (field.kind === "slider")
		{
			return createRangeField(label, currentValue, onChange, {
				min: field.minimumValue,
				max: field.maximumValue,
				step: 1
			});
		}

		return document.createElement("div");
	}

	function createAddConditionControl(group)
	{
		const wrapper = document.createElement("div");
		wrapper.className = "toolbar";
		const select = createSelect(manifest.conditions.map(function (item)
		{
			return { value: item.typeId, label: item.label };
		}), manifest.defaultConditionType, function ()
		{
		});

		const addButton = document.createElement("button");
		addButton.type = "button";
		addButton.textContent = "Add condition";
		addButton.addEventListener("click", function ()
		{
			group.children.push(createDefinitionFromManifest(conditionMap[select.value]));
			render();
		});

		wrapper.appendChild(select);
		wrapper.appendChild(addButton);
		return wrapper;
	}

	function createSection(title, description)
	{
		const section = document.createElement("div");
		const head = document.createElement("div");
		head.className = "section-head";

		const heading = document.createElement("h2");
		heading.textContent = title;
		head.appendChild(heading);

		if (description)
		{
			const copy = document.createElement("p");
			copy.textContent = description;
			head.appendChild(copy);
		}

		section.appendChild(head);
		return section;
	}

	function createMetaHeader(title, description)
	{
		const wrapper = document.createElement("div");
		wrapper.className = "leaf-header";
		const heading = document.createElement("h3");
		heading.textContent = title;
		wrapper.appendChild(heading);

		if (description)
		{
			const copy = document.createElement("p");
			copy.textContent = description;
			wrapper.appendChild(copy);
		}

		return wrapper;
	}

	function createFieldGrid()
	{
		const grid = document.createElement("div");
		grid.className = "field-grid";
		return grid;
	}

	function createTextField(label, value, onChange, options)
	{
		options = options || {};
		const field = createFieldShell(label);
		const input = document.createElement("input");
		input.type = options.type || "text";
		input.value = value == null ? "" : value;

		if (options.placeholder)
		{
			input.placeholder = options.placeholder;
		}

		input.addEventListener("input", function ()
		{
			onChange(input.value);
		});

		field.appendChild(input);
		return field;
	}

	function createCheckboxField(label, value, onChange)
	{
		const field = document.createElement("label");
		field.className = "field inline-checkbox";
		const input = document.createElement("input");
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

	function createSelectField(label, options, value, onChange)
	{
		const field = createFieldShell(label);
		field.appendChild(createSelect(options, value, onChange));
		return field;
	}

	function createSelect(options, value, onChange)
	{
		const select = document.createElement("select");

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
			onChange(parseOptionValue(options, select.value));
		});

		return select;
	}

	function createNumberField(label, value, onChange, options)
	{
		options = options || {};
		const field = createFieldShell(label);
		const wrapper = document.createElement("div");
		wrapper.className = "range-row";
		const input = document.createElement("input");
		input.type = "number";
		input.value = value;

		if (options.min !== undefined)
		{
			input.min = options.min;
		}

		if (options.max !== undefined)
		{
			input.max = options.max;
		}

		if (options.step !== undefined)
		{
			input.step = options.step;
		}

		input.addEventListener("input", function ()
		{
			onChange(normalizeNumber(input.value, value));
		});

		wrapper.appendChild(input);

		if (options.suffix)
		{
			const suffix = document.createElement("span");
			suffix.className = "field-hint";
			suffix.textContent = options.suffix;
			wrapper.appendChild(suffix);
		}

		field.appendChild(wrapper);
		return field;
	}

	function createRangeField(label, value, onChange, options)
	{
		const field = createFieldShell(label);
		const wrapper = document.createElement("div");
		wrapper.className = "field";
		const input = document.createElement("input");
		input.type = "range";
		input.min = options.min;
		input.max = options.max;
		input.step = options.step;
		input.value = value;

		const hint = document.createElement("span");
		hint.className = "field-hint";
		hint.textContent = String(value);

		input.addEventListener("input", function ()
		{
			const nextValue = normalizeNumber(input.value, value);
			hint.textContent = String(nextValue);
			onChange(nextValue);
		});

		wrapper.appendChild(input);
		wrapper.appendChild(hint);
		field.appendChild(wrapper);
		return field;
	}

	function createFieldShell(label)
	{
		const field = document.createElement("label");
		field.className = "field";
		const caption = document.createElement("span");
		caption.textContent = label;
		field.appendChild(caption);
		return field;
	}

	function createInlineLabel(text)
	{
		const label = document.createElement("span");
		label.textContent = text;
		return label;
	}

	function createRuleState()
	{
		return {
			schemaVersion: manifest.schemaVersion,
			name: "",
			enabled: true,
			rootGroup: clone(manifest.groupDefaults),
			activationMode: manifest.activationModes[0].value,
			cooldownTicks: 0,
			action: createDefinitionFromManifest(actionMap[manifest.defaultActionType])
		};
	}

	function createDefinitionFromManifest(meta)
	{
		const definition = clone(meta.defaults);

		meta.fields.forEach(function (field)
		{
			if (definition[field.key] !== undefined)
			{
				return;
			}

			if (field.defaultValue !== undefined)
			{
				definition[field.key] = clone(field.defaultValue);
				return;
			}

			if (field.kind === "spinner" || field.kind === "slider")
			{
				definition[field.key] = field.minimumValue;
				return;
			}

			if (field.kind === "checkbox")
			{
				definition[field.key] = false;
				return;
			}

			definition[field.key] = "";
		});

		return definition;
	}

	function createExportedRule()
	{
		return {
			schemaVersion: manifest.schemaVersion,
			name: state.name.trim(),
			enabled: state.enabled,
			rootGroup: clone(state.rootGroup),
			activationMode: state.activationMode,
			cooldownTicks: state.cooldownTicks,
			action: clone(state.action)
		};
	}

	function validateRule(rule)
	{
		const errors = [];

		if (!rule.name || !rule.name.trim())
		{
			errors.push("Rule name is required.");
		}

		if (!hasLeafCondition(rule.rootGroup))
		{
			errors.push("Add at least one condition.");
		}

		if (!Number.isInteger(rule.cooldownTicks) || rule.cooldownTicks < 0)
		{
			errors.push("Cooldown must be zero or higher.");
		}

		validateNode(rule.rootGroup, errors);
		validateDefinition(rule.action, actionMap[rule.action.type], errors);
		return { isValid: errors.length === 0, errors: errors };
	}

	function validateNode(node, errors)
	{
		if (!node)
		{
			errors.push("Condition tree is missing.");
			return;
		}

		if (node.type === "group")
		{
			node.children.forEach(function (child)
			{
				validateNode(child, errors);
			});
			return;
		}

		validateDefinition(node, conditionMap[node.type], errors);
	}

	function validateDefinition(definition, meta, errors)
	{
		if (!definition || !meta)
		{
			errors.push("Definition metadata is missing.");
			return;
		}

		meta.fields.forEach(function (field)
		{
			const value = definition[field.key];
			const label = field.label || prettyLabel(field.key);

			if (field.kind === "text")
			{
				if (!String(value || "").trim())
				{
					errors.push(label + " is required.");
				}
			}
			else if (field.kind === "color")
			{
				if (!/^#[0-9A-Fa-f]{6}$/.test(String(value || "")))
				{
					errors.push(label + " must be in #RRGGBB format.");
				}
			}
			else if (field.kind === "choice")
			{
				const allowed = field.options.map(function (option)
				{
					return String(option.value);
				});

				if (allowed.indexOf(String(value)) === -1)
				{
					errors.push("Choose a valid " + label.toLowerCase() + ".");
				}
			}
			else if (field.kind === "spinner" || field.kind === "slider")
			{
				if (!Number.isFinite(value) || value < field.minimumValue || value > field.maximumValue)
				{
					errors.push(label + " must be between " + field.minimumValue + " and " + field.maximumValue + ".");
				}
			}
		});
	}

	function hasLeafCondition(group)
	{
		if (!group)
		{
			return false;
		}

		return group.children.some(function (child)
		{
			return child.type !== "group" || hasLeafCondition(child);
		});
	}

	function removeNode(path)
	{
		const parentPath = path.slice(0, -1);
		const index = path[path.length - 1];
		const parent = parentPath.length === 0 ? state.rootGroup : getNode(parentPath);
		parent.children.splice(index, 1);
	}

	function getNode(path)
	{
		let current = state.rootGroup;

		path.forEach(function (index)
		{
			current = current.children[index];
		});

		return current;
	}

	function copyJson()
	{
		const json = dom.json.textContent;

		if (!validation.isValid)
		{
			return;
		}

		if (navigator.clipboard && navigator.clipboard.writeText)
		{
			navigator.clipboard.writeText(json);
			return;
		}

		const range = document.createRange();
		range.selectNodeContents(dom.json);
		const selection = window.getSelection();
		selection.removeAllRanges();
		selection.addRange(range);
		document.execCommand("copy");
		selection.removeAllRanges();
	}

	function downloadJson()
	{
		if (!validation.isValid)
		{
			return;
		}

		const blob = new Blob([dom.json.textContent], { type: "application/json" });
		const url = URL.createObjectURL(blob);
		const link = document.createElement("a");
		link.href = url;
		link.download = slugify(state.name || "strong-buffs-rule") + ".json";
		document.body.appendChild(link);
		link.click();
		link.remove();
		URL.revokeObjectURL(url);
	}

	function toMap(items, key)
	{
		return items.reduce(function (accumulator, item)
		{
			accumulator[item[key]] = item;
			return accumulator;
		}, {});
	}

	function clone(value)
	{
		return JSON.parse(JSON.stringify(value));
	}

	function replaceChildren(node)
	{
		node.innerHTML = "";

		for (let i = 1; i < arguments.length; i++)
		{
			node.appendChild(arguments[i]);
		}
	}

	function manifestValueOptions(values)
	{
		return values.map(function (value)
		{
			return { value: value, label: value };
		});
	}

	function normalizeNumber(value, fallback)
	{
		const parsed = Number(value);
		return Number.isFinite(parsed) ? parsed : fallback;
	}

	function parseOptionValue(options, rawValue)
	{
		const match = options.find(function (option)
		{
			return String(option.value) === rawValue;
		});

		return match ? match.value : rawValue;
	}

	function prettyLabel(value)
	{
		return String(value)
			.replace(/([A-Z])/g, " $1")
			.replace(/_/g, " ")
			.replace(/^./, function (firstCharacter)
			{
				return firstCharacter.toUpperCase();
			});
	}

	function slugify(value)
	{
		return String(value)
			.toLowerCase()
			.replace(/[^a-z0-9]+/g, "-")
			.replace(/^-+|-+$/g, "");
	}
})();
