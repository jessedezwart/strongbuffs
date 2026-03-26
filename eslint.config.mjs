import js from "@eslint/js";
import tseslint from "typescript-eslint";

export default [
  {
    ignores: ["website/app.js", "website/generated/**", "node_modules/**"],
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  {
    files: ["website/src/**/*.ts", "build.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "module",
    },
    rules: {
      "@typescript-eslint/no-unused-vars": ["error", { caughtErrors: "none" }],
      "no-console": "off",
    },
  },
];
