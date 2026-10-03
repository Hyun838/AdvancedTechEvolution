# Сборка jar (Forge 1.12.2)

Нужно: **JDK 8** и **Gradle 4.9** (ForgeGradle 2.3 не работает с новыми версиями Gradle/Java), интернет.

    gradle setupDecompWorkspace   # один раз, качает и декомпилирует Minecraft
    gradle build

Готовый мод: `build/libs/AdvancedTechEvolution-0.1.0.jar` → положить в `.minecraft/mods`.
