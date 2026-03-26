import {
	createInlineLabel,
	createMetaHeader,
	replaceChildren
} from "../dom";
import {
	createFieldsBlock,
	createSelect
} from "../fields";
import { manifestValueOptions } from "../manifest";
import type {
	ConditionGroup,
	DefinitionNode,
	DomRefs,
	ManifestRefs,
	RuleState
} from "../types";

type NodePath = number[];

interface ConditionActions
{
	addCondition: (group: ConditionGroup, typeId: string) => void;
	addGroup: (group: ConditionGroup) => void;
	removeNode: (path: NodePath) => void;
	updateConditionField: (path: NodePath, key: string, value: unknown) => void;
	updateGroupLogic: (path: NodePath, value: string) => void;
}

function createAddConditionDropdown(
	manifestRefs: ManifestRefs,
	group: ConditionGroup,
	actions: ConditionActions
): HTMLDetailsElement
{
	const details = document.createElement("details");
	details.className = "dropdown";

	const toggle = document.createElement("summary");
	toggle.className = "dropdown__toggle button--with-glyph button--glyph-add";
	toggle.textContent = "Add condition";
	details.appendChild(toggle);

	const menu = document.createElement("div");
	menu.className = "dropdown__menu";

	manifestRefs.manifest.conditions.forEach(function (item)
	{
		const option = document.createElement("button");
		option.type = "button";
		option.className = "button--secondary dropdown__item";
		option.textContent = item.label;
		option.addEventListener("click", function (event)
		{
			event.preventDefault();
			actions.addCondition(group, item.typeId);
			details.open = false;
		});
		menu.appendChild(option);
	});

	details.appendChild(menu);
	return details;
}

function renderCondition(
	condition: DefinitionNode,
	path: NodePath,
	manifestRefs: ManifestRefs,
	actions: ConditionActions
): HTMLDivElement
{
	const meta = manifestRefs.conditionMap[condition.type];
	const card = document.createElement("div");
	card.className = "card";

	const header = document.createElement("div");
	header.className = "condition__head";
	header.appendChild(createMetaHeader(meta.label));

	const removeButton = document.createElement("button");
	removeButton.type = "button";
	removeButton.className = "button--secondary button--icon condition__remove";
	removeButton.setAttribute("aria-label", "Remove condition");
	removeButton.title = "Remove condition";
	removeButton.addEventListener("click", function ()
	{
		actions.removeNode(path);
	});

	const removeIcon = document.createElement("span");
	removeIcon.className = "button__icon icon--trash";
	removeIcon.setAttribute("aria-hidden", "true");
	removeButton.appendChild(removeIcon);

	header.appendChild(removeButton);
	card.appendChild(header);
	card.appendChild(
		createFieldsBlock(meta.fields, condition, function (key, value)
		{
			actions.updateConditionField(path, key, value);
		})
	);

	return card;
}

function renderGroup(
	group: ConditionGroup,
	path: NodePath,
	manifestRefs: ManifestRefs,
	actions: ConditionActions
): HTMLDivElement
{
	const card = document.createElement("div");
	card.className = "card card--group";
	const hasMultipleChildren = group.children.length > 1;
	const isNestedGroup = path.length > 0;

	const header = document.createElement("div");
	header.className = "group__head";

	const controls = document.createElement("div");
	controls.className = "group__controls";

	if (hasMultipleChildren)
	{
		const logicChip = document.createElement("div");
		logicChip.className = "chip--logic";
		logicChip.appendChild(createInlineLabel("Logic"));
		logicChip.appendChild(
			createSelect(
				manifestValueOptions(["AND", "OR"]),
				group.logic,
				function (value)
				{
					actions.updateGroupLogic(path, String(value));
				}
			)
		);
		controls.appendChild(logicChip);
	}

	if (isNestedGroup)
	{
		const removeButton = document.createElement("button");
		removeButton.type = "button";
		removeButton.className = "button--secondary button--icon group__remove";
		removeButton.setAttribute("aria-label", "Remove group");
		removeButton.title = "Remove group";
		removeButton.addEventListener("click", function ()
		{
			actions.removeNode(path);
		});

		const removeIcon = document.createElement("span");
		removeIcon.className = "button__icon icon--trash";
		removeIcon.setAttribute("aria-hidden", "true");
		removeButton.appendChild(removeIcon);

		controls.appendChild(removeButton);
	}

	if (controls.childNodes.length > 0)
	{
		header.appendChild(controls);
	}

	card.appendChild(header);

	const toolbar = document.createElement("div");
	toolbar.className = "toolbar";
	toolbar.appendChild(createAddConditionDropdown(manifestRefs, group, actions));

	const addGroupButton = document.createElement("button");
	addGroupButton.type = "button";
	addGroupButton.className =
		"button--secondary button--with-glyph button--glyph-add";
	addGroupButton.textContent = "Add group";
	addGroupButton.addEventListener("click", function ()
	{
		actions.addGroup(group);
	});
	toolbar.appendChild(addGroupButton);

	if (group.children.length === 0)
	{
		const empty = document.createElement("p");
		empty.className = "field__hint";
		empty.textContent = "Add at least one condition or nested group.";
		card.appendChild(empty);
	}

	group.children.forEach(function (child, index)
	{
		const childPath = path.concat(index);

		if (child.type === "group")
		{
			card.appendChild(
				renderGroup(child as ConditionGroup, childPath, manifestRefs, actions)
			);
			return;
		}

		card.appendChild(
			renderCondition(child as DefinitionNode, childPath, manifestRefs, actions)
		);
	});

	card.appendChild(toolbar);
	return card;
}

export function renderConditionsSection(
	domRefs: DomRefs,
	manifestRefs: ManifestRefs,
	rule: RuleState,
	actions: ConditionActions
): void
{
	const root = document.createElement("div");
	root.className = "conditions";
	root.appendChild(renderGroup(rule.rootGroup, [], manifestRefs, actions));
	replaceChildren(domRefs.conditions, root);
}
