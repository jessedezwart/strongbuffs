import { slugify } from "./format";

export function copyJson(json: string, fallbackNode: HTMLElement): void
{
	if (navigator.clipboard && navigator.clipboard.writeText)
	{
		void navigator.clipboard.writeText(json);
		return;
	}

	const range = document.createRange();
	range.selectNodeContents(fallbackNode);
	const selection = window.getSelection();

	if (!selection)
	{
		return;
	}

	selection.removeAllRanges();
	selection.addRange(range);
	document.execCommand("copy");
	selection.removeAllRanges();
}

export function downloadJson(json: string, ruleName: string): void
{
	const blob = new Blob([json], { type: "application/json" });
	const url = URL.createObjectURL(blob);
	const link = document.createElement("a");
	link.href = url;
	link.download = slugify(ruleName || "strong-buffs-rule") + ".json";
	document.body.appendChild(link);
	link.click();
	link.remove();
	URL.revokeObjectURL(url);
}
