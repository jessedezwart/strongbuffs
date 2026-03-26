import { prettyLabel } from "./format";
import { hasLeafCondition } from "./rule-tree";
import type {
	ConditionGroup,
	ConditionNode,
	DefinitionNode,
	ManifestField,
	ManifestRefs,
	RuleState,
	ValidationResult
} from "./types";

function getFieldOptions(field: ManifestField)
{
	return field.options || [];
}

function isConditionGroup(node: ConditionNode): node is ConditionGroup
{
	return node.type === "group" && Array.isArray((node as ConditionGroup).children);
}

export function isManifestFieldValid(field: ManifestField, value: unknown): boolean
{
	if (field.kind === "text")
	{
		return String(value || "").trim().length > 0;
	}

	if (field.kind === "color")
	{
		return /^#[0-9A-Fa-f]{6}$/.test(String(value || ""));
	}

	if (field.kind === "choice")
	{
		const allowed = getFieldOptions(field).map(function (option)
		{
			return String(option.value);
		});

		return allowed.indexOf(String(value)) !== -1;
	}

	if (field.kind === "spinner" || field.kind === "slider")
	{
		return typeof value === "number"
			&& Number.isFinite(value)
			&& value >= (field.minimumValue ?? 0)
			&& value <= (field.maximumValue ?? 0);
	}

	return true;
}

export function validateDefinition(
	definition: DefinitionNode | null | undefined,
	meta: ManifestRefs["conditionMap"][string] | ManifestRefs["actionMap"][string] | undefined,
	errors: string[]
): void
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
			const allowed = getFieldOptions(field).map(function (option)
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
			if (
				typeof value !== "number"
				|| !Number.isFinite(value)
				|| value < (field.minimumValue ?? 0)
				|| value > (field.maximumValue ?? 0)
			)
			{
				errors.push(
					label
					+ " must be between "
					+ (field.minimumValue ?? 0)
					+ " and "
					+ (field.maximumValue ?? 0)
					+ "."
				);
			}
		}
	});
}

export function validateNode(
	node: ConditionNode | null | undefined,
	manifestRefs: ManifestRefs,
	errors: string[]
): void
{
	if (!node)
	{
		errors.push("Condition tree is missing.");
		return;
	}

	if (isConditionGroup(node))
	{
		node.children.forEach(function (child)
		{
			validateNode(child, manifestRefs, errors);
		});
		return;
	}

	validateDefinition(node, manifestRefs.conditionMap[node.type], errors);
}

export function validateRule(rule: RuleState, manifestRefs: ManifestRefs): ValidationResult
{
	const errors: string[] = [];

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

	validateNode(rule.rootGroup, manifestRefs, errors);
	validateDefinition(rule.action, manifestRefs.actionMap[rule.action.type], errors);
	return { isValid: errors.length === 0, errors: errors };
}
