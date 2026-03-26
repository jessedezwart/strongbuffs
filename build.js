import { build } from "esbuild";

await build({
  entryPoints: ["website/src/main.ts"],
  bundle: true,
  minify: true,
  platform: "browser",
  format: "esm",
  target: "ES2022",
  outfile: "website/app.js",
  logLevel: "info",
});
