# Contributing

Thanks for taking an interest in Ramesses' Curse.

## Before you start

- Open an issue first for large gameplay changes
- Keep new mechanics aligned with the curse-and-relic theme of the mod
- Avoid unrelated refactors in the same pull request

## Development setup

1. Install Java 17
2. Clone the repository
3. Run:

```powershell
.\gradlew.bat runClient
```

## Pull request guidance

- Keep changes focused
- Explain gameplay impact clearly
- Include testing notes
- Update localization when adding player-facing text
- Add items, blocks, and food to the correct creative tab when applicable

## Content guidelines

- New curios should respect the Seal of Kadesh progression if they are part of the main curse line
- New text should match the existing tone of the mod
- Configurable mechanics should include clear config comments
