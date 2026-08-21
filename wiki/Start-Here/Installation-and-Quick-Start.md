---
title: "Installation and Quick Start"
order: 2
published: true
draft: false
---

# Installation and Quick Start

Parent: [Start Here](/mod/alecs-radial-menu/start-here) · [Home](/mod/alecs-radial-menu/home)

## Install a release

1. Stop the server.
2. Put the Alec's Radial Menu JAR in the server `mods` folder.
3. Remove older copies of the same mod.
4. Start the server.
5. Confirm that the log contains `Alec's Radial Menu enabled`.

The plugin manifest requires the Hytale asset and NPC modules. These modules are part of the server runtime.

## Supported server versions

Version `2.0.1` declares this server range:

```text
>=0.5.0 <0.7.0
```

See [Compatibility and Limits](/mod/alecs-radial-menu/compatibility-and-limits) before you use another server build.

## Build from source

The repository contains Maven and Gradle wrappers. Java 25 is required.

```text
./mvnw test
./mvnw package
```

Or:

```text
./gradlew build
```

The Maven `install-plugin` profile copies the built JAR to the configured server plugin folder. The `run-server` profile starts the configured local server.

```text
./mvnw package -Pinstall-plugin
./mvnw package -Prun-server
```

## Quick verification

The mod ships an example item, menu, and NPC role. For the NPC path, run:

```text
/npc spawn Alec_Radial_Menu_Example_Npc
```

Interact with the cow. The radial menu must show `Sleep`, `Alert`, and `Wake and Mark`.

Next: [First Radial Menu](/mod/alecs-radial-menu/first-radial-menu)
