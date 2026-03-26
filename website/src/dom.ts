import type { FocusState } from "./types";

let nextFocusId = 0;

export function replaceChildren(node: HTMLElement, ...children: Node[]): void
{
	node.innerHTML = "";
	children.forEach(function (child)
	{
		node.appendChild(child);
	});
}

export function createFieldGrid(): HTMLDivElement
{
	const grid = document.createElement("div");
	grid.className = "field-grid";
	return grid;
}

export function createFieldShell(label: string): HTMLLabelElement
{
	const field = document.createElement("label");
	field.className = "field";
	const caption = document.createElement("span");
	caption.textContent = label;
	field.appendChild(caption);
	return field;
}

export function createInlineLabel(text: string): HTMLSpanElement
{
	const label = document.createElement("span");
	label.textContent = text;
	return label;
}

export function createMetaHeader(title: string, description?: string): HTMLDivElement
{
	const wrapper = document.createElement("div");
	wrapper.className = "card__header";
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

export function beginRender(): void
{
	nextFocusId = 0;
}

export function assignFocusId<T extends HTMLElement>(element: T): T
{
	element.setAttribute("data-focus-id", String(nextFocusId));
	nextFocusId += 1;
	return element;
}

export function captureFocusState(): FocusState | null
{
	const active = document.activeElement;

	if (!(active instanceof HTMLElement))
	{
		return null;
	}

	const focusId = active.getAttribute("data-focus-id");

	if (!focusId)
	{
		return null;
	}

	const selectionElement = active as HTMLInputElement | HTMLTextAreaElement;
	const start = typeof selectionElement.selectionStart === "number"
		? selectionElement.selectionStart
		: null;
	const end = typeof selectionElement.selectionEnd === "number"
		? selectionElement.selectionEnd
		: null;

	return {
		focusId: focusId,
		selectionStart: start,
		selectionEnd: end
	};
}

export function restoreFocusState(focusState: FocusState | null): void
{
	if (!focusState)
	{
		return;
	}

	const selector = '[data-focus-id="' + focusState.focusId + '"]';
	const element = document.querySelector<HTMLElement>(selector);

	if (!element)
	{
		return;
	}

	element.focus();

	if (
		focusState.selectionStart == null
		|| focusState.selectionEnd == null
		|| !(element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement)
	)
	{
		return;
	}

	try
	{
		element.setSelectionRange(
			focusState.selectionStart,
			focusState.selectionEnd
		);
	}
	catch (error)
	{
		// Some input types do not support selection ranges.
	}
}
