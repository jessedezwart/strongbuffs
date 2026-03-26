import type { SelectOption } from "./types";

export function prettyLabel(value: string): string {
  return String(value)
    .replace(/([A-Z])/g, " $1")
    .replace(/_/g, " ")
    .replace(/^./, function (firstCharacter) {
      return firstCharacter.toUpperCase();
    });
}

export function slugify(value: string): string {
  return String(value)
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");
}

export function normalizeNumber(value: string, fallback: number): number {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

export function parseOptionValue<T>(
  options: SelectOption<T>[],
  rawValue: string,
): T | string {
  const match = options.find(function (option) {
    return String(option.value) === rawValue;
  });

  return match ? match.value : rawValue;
}
