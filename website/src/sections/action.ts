import { replaceChildren, createMetaHeader } from "../dom";
import { createFieldsBlock, createSelectField } from "../fields";
import { getActionTypeOptions } from "../manifest";
import type { DomRefs, ManifestRefs, RuleState } from "../types";

interface ActionActions
{
	changeActionType: (typeId: string) => void;
	updateActionField: (key: string, value: unknown) => void;
}

export function renderActionSection(
	domRefs: DomRefs,
	manifestRefs: ManifestRefs,
	rule: RuleState,
	actions: ActionActions
): void
{
	const actionMeta = manifestRefs.actionMap[rule.action.type];

	const actionTypeField = createSelectField(
		"Action type",
		getActionTypeOptions(manifestRefs.manifest),
		rule.action.type,
		function (value)
		{
			actions.changeActionType(String(value));
		}
	);

	const card = document.createElement("div");
	card.className = "card";
	card.appendChild(createMetaHeader(actionMeta.label));
	card.appendChild(
		createFieldsBlock(actionMeta.fields, rule.action, function (key, value)
		{
			actions.updateActionField(key, value);
		})
	);

	replaceChildren(domRefs.action, actionTypeField, card);
}
