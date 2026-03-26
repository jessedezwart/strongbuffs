import { build } from "esbuild";
import { readFile } from "node:fs/promises";

const manifestBootstrap = await readFile(
  "website/generated/definition-manifest.js",
  "utf8",
);

await build({
  entryPoints: ["website/src/main.ts"],
  bundle: true,
  minify: true,
  platform: "browser",
  format: "esm",
  target: "ES2022",
  banner: {
    js: manifestBootstrap.trim() + ";",
  },
  outfile: "website/app.js",
  logLevel: "info",
});
