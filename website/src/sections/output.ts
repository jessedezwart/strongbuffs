import type { DomRefs, RuleState, ValidationResult } from "../types";

export function renderOutputSection(
	domRefs: DomRefs,
	exportedRule: RuleState,
	validation: ValidationResult
): string
{
	const json = JSON.stringify(exportedRule, null, 2);
	domRefs.json.textContent = json;
	domRefs.copy.disabled = !validation.isValid;
	domRefs.download.disabled = !validation.isValid;

	domRefs.status.className = validation.isValid
		? "status"
		: "status status--error";
	domRefs.status.innerHTML = "";

	const title = document.createElement("strong");
	title.textContent = validation.isValid
		? "Ready to import"
		: "Fix these issues first";
	domRefs.status.appendChild(title);

	if (validation.isValid)
	{
		const description = document.createElement("p");
		description.textContent =
			"Copy the JSON and paste it into Strong Buffs with Import JSON.";
		domRefs.status.appendChild(description);
		return json;
	}

	const list = document.createElement("ul");

	validation.errors.forEach(function (error)
	{
		const item = document.createElement("li");
		item.textContent = error;
		list.appendChild(item);
	});

	domRefs.status.appendChild(list);
	return json;
}
