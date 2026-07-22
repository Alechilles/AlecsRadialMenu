# Release deployment

The `Publish` GitHub Actions workflow deploys a prebuilt GitHub Release artifact to CurseForge, Modtale, and Modifold. Create the tagged GitHub Release first, with the artifact name defined in `publish-config.json`, then dispatch the workflow with the matching version.

## Required repository secrets

- `CURSEFORGE_API_TOKEN`
- `MODTALE_API_TOKEN`
- `MODIFOLD_API_TOKEN`

Platform uploads are opt-in workflow inputs and default to disabled. Keep `dry_run` enabled to validate the release without uploading anything.

## Modifold moderation

Modifold project `LQlh3A` is configured with an approval preflight. Dry runs report the current moderation status, while a live upload stops with a clear error until the project status is `approved`. After approval, add `MODIFOLD_API_TOKEN` and dispatch the workflow with `dry_run=false` and `publish_modifold=true`.
