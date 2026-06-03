# Ramesses' Curse

Ramesses' Curse is a Minecraft Forge mod for 1.20.1 built around a custom Curios slot, dynastic curses, and a dark Egyptian-themed progression centered on the Seal of Kadesh.

## What the mod currently includes

- A dedicated `curse` Curios slot
- The `Seal of Kadesh` as the core relic of the mod
- A custom creative tab for mod items
- A standalone advancement page with the Seal of Kadesh as the entry point
- Curse systems tied to combat, hunger debt, guilt, trade pressure, dimension triggers, and item locking
- Supporting items such as the `Purification Scroll` and `Satiety Crystal`

## Design direction

The project aims to build a themed curse-and-relic experience rather than a generic equipment expansion. Future development is planned around:

- More cursed curios gated by the Seal of Kadesh
- Better JEI support and content discoverability
- More items, blocks, food, and rituals tied to the same mythic tone
- Stronger data/config support for pack makers
- More advancement chains and long-term progression hooks

## Development

### Requirements

- Java 17
- Minecraft Forge 1.20.1

### Build

```powershell
.\gradlew.bat build
```

### Run in development

```powershell
.\gradlew.bat runClient
```

## Repository structure

- `src/main/java` - mod source code
- `src/main/resources` - assets, language files, metadata, advancements
- `src/generated/resources` - generated data output

## License

Source code in this repository is licensed under the MIT License. See [LICENSE](LICENSE).

Art assets, textures, animations, models, sounds, music, written lore, and localization content are All Rights Reserved unless explicitly stated otherwise. See [LICENSE_ASSETS](LICENSE_ASSETS).
