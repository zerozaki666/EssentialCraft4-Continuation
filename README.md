# EssentialCraft4Unofficial
EssentialCraft 4 Unofficial is a modified version of EssentialCraft 3, which is a huge magic themed industrial mod, which adds a lot of content to the game. A new energy system, recipes, tools, armor, devices, bosses - even a new dimension exists!

Feel free to report bugs. However, please, attach a crash report when reporting bugs!

EssentialCraft is redistributed under CreativeCommons 4.0 share-alike license. license(http://creativecommons.org/licenses/by-sa/4.0/)

## Building

The repository uses the current GT New Horizons 1.7.10 toolchain with
RetroFuturaGradle and Forge `10.13.4.1614`. JDK 25 is recommended (and recorded
in `.java-version`); the produced mod classes still target Java 8.

```bash
./gradlew build
```

On Windows, run `gradlew.bat build`. Build artifacts are written to
`build/libs/`. To create an IDE development workspace, run
`./gradlew setupDecompWorkspace` and then import the Gradle project.

The project follows the standard Gradle layout:

- Java sources: `src/main/java`
- Assets and other packaged resources: `src/main/resources`
- Dependency declarations: `dependencies.gradle`
- Additional Maven repositories: `repositories.gradle`
