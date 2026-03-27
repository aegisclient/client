<div align="center">

<img src="https://raw.githubusercontent.com/ArhanCodes/aegis/main/assets/logo.png" alt="Aegis" width="200" />

# Aegis

A free and open-source utility client for Minecraft, built on Fabric.

**27 modules** | **ClickGUI** | **Minecraft 1.20.4**

[Website](https://arhancodes.github.io/aegis) | [Discord](https://discord.gg/) | [Releases](https://github.com/ArhanCodes/aegis/releases)

![GitHub License](https://img.shields.io/github/license/ArhanCodes/aegis?style=flat-square&color=e6a817)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4-brightgreen?style=flat-square)
![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue?style=flat-square)
![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)

</div>

---

## Modules

| Category | Modules |
|----------|---------|
| **Combat** | KillAura, Reach, Criticals, AutoTotem, AutoArmor |
| **Movement** | Flight, Speed, NoFall, Sprint, Step, Jesus, BoatFly |
| **Render** | ESP, Fullbright, Xray, Tracers, Nametags, NoWeather |
| **Player** | AutoMine, FastPlace, AutoEat, ChestStealer, Scaffold, AutoFish |
| **World** | Nuker, Timer, AntiHunger |

## Issues

If you notice any bugs or have suggestions, please let us know by opening an [issue](https://github.com/ArhanCodes/aegis/issues).

## Requirements

- **Minecraft** Java Edition 1.20.4
- **Java** JDK 17 or newer
- **Fabric Loader** 0.15.3+
- **Fabric API** 0.91.0+1.20.4
- **RAM** 2GB+ allocated to Minecraft

## Setting up a Workspace

Aegis uses Gradle and requires JDK 17+ to build. Follow these steps to set up a development workspace:

1. Clone the repository
   ```bash
   git clone https://github.com/ArhanCodes/aegis.git
   cd aegis
   ```

2. Generate the Gradle wrapper (if not present)
   ```bash
   gradle wrapper
   ```

3. Build the project
   ```bash
   ./gradlew build
   ```

4. The compiled JAR will be in `build/libs/aegis-client-1.0.0.jar`

5. To set up your IDE, run:
   ```bash
   ./gradlew genSources     # Generate Minecraft sources
   ./gradlew eclipse         # For Eclipse
   ./gradlew idea            # For IntelliJ IDEA
   ```

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.4
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods/` folder
3. Download the latest Aegis JAR from [Releases](https://github.com/ArhanCodes/aegis/releases) and place it in your `mods/` folder
4. Launch Minecraft with the Fabric profile
5. Press **Right Shift** in-game to open the ClickGUI

## Additional Libraries

### Mixins

Aegis uses [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) to hook into Minecraft's codebase. Mixins allow us to inject code into existing Minecraft classes at runtime without modifying the source directly. See the [Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) for more info.

## Keybinds

| Key | Module |
|-----|--------|
| `R` | KillAura |
| `F` | Flight |
| `V` | Speed |
| `B` | Fullbright |
| `X` | Xray |
| `J` | Jesus |
| `G` | Scaffold |
| `N` | Nuker |
| `Right Shift` | ClickGUI |

## Contributing

We welcome contributions! If you'd like to improve Aegis, feel free to open a pull request. Please make sure your code follows the existing style and conventions.

## License

This project is subject to the [GNU General Public License v3.0](LICENSE). This does only apply for source code located directly in this clean repository. During the development and compilation process, additional source code may be used to which we have obtained no rights. Such code is not covered by the GPL license.

For those who are unfamiliar with the license, here is a summary of its main points. This is by no means legal advice nor legally binding.

**You are allowed to:**
- Use the source code for personal and commercial purposes
- Share and distribute the source code
- Modify the source code

**If you use any of this code, you must:**
- **Disclose** the source code of your modified work and the source code you took from this project, under the GPL license
- **State any changes** you have made to the code
- License your modified work under the **GPL-3.0** license

---

<div align="center">

**Aegis** is intended for educational purposes and single-player testing only.

</div>
