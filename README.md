<div align="center">


<br>

### A free and open-source Minecraft utility mod for Fabric

<br>

[![GitHub Release](https://img.shields.io/github/v/release/ArhanCodes/aegis?style=flat-square&color=FFD700)](https://github.com/ArhanCodes/aegis/releases)
[![Modules](https://img.shields.io/badge/modules-134+-FFD700?style=flat-square)](https://github.com/ArhanCodes/aegis#modules-134)
[![License](https://img.shields.io/github/license/ArhanCodes/aegis?style=flat-square&color=FFD700)](LICENSE.md)

[Website](https://arhancodes.github.io/aegis) · [Download](https://github.com/ArhanCodes/aegis/releases) · [Issues](https://github.com/ArhanCodes/aegis/issues)

</div>

<br>

## What is Aegis?

Aegis is a free Fabric injection client for Minecraft 1.20.4. It features 134+ modules across 8 categories, advanced anti-cheat bypass with NCP-safe packet timing, and a native launcher.

> This project is for educational purposes. Use responsibly and at your own risk

## Issues

If you notice any bugs or missing features, please let us know by opening an [issue](https://github.com/ArhanCodes/aegis/issues)

## Modules (134)

<details>
<summary><b>Combat (21)</b></summary>

| Module | Description |
|--------|-------------|
| KillAura | Auto-attack with smooth rotation and randomized timing |
| Reach | Extended attack and interaction range |
| Criticals | Packet-based critical hits with NCP-bypass offsets |
| AutoTotem | Auto-equip totem of undying to offhand |
| AutoArmor | Auto-equip best armor via shift-click |
| BowAimbot | 3-pass iterative pitch solver with EMA velocity smoothing |
| AutoCrystal | Full crystal PvP automation with damage estimation |
| Velocity | Anti-knockback via packet cancellation |
| TriggerBot | Auto-attack entity under crosshair |
| Anchor | Respawn anchor PvP automation |
| AutoClicker | Configurable CPS with randomization |
| KeepSprint | Prevents sprint reset on attack |
| BackTrack | Delays incoming entity packets to extend hit range |
| FakeLag | Holds outgoing movement packets for teleport effect |
| Hitbox | Expands entity hitboxes for easier hits |
| SuperKnockback | Sprint reset trick for extra knockback |
| AutoWeapon | Auto-switch to best weapon before attack |
| AutoLeave | Disconnect when enemy detected while low HP |
| TickBase | Client tick manipulation for combat advantage |
| AutoRod | Auto fishing rod throw for knockback |
| NoMissCooldown | Resets attack cooldown on miss |
</details>

<details>
<summary><b>Movement (27)</b></summary>

| Module | Description |
|--------|-------------|
| Flight | Packet-based fly with ground spoofing |
| Speed | NCP-safe strafe bhop within vanilla limits |
| NoFall | Alternating packet fall damage prevention |
| Sprint | Auto-sprint when moving forward |
| Step | Increased step height (2 blocks) |
| Jesus | Water/lava walking with dolphin bob pattern |
| BoatFly | Fly while riding a boat |
| ElytraFly | Controlled elytra flight with speed cap |
| Phase | NoClip through blocks |
| LongJump | Momentum boost on jump |
| EntitySpeed | Speed boost for ridden entities |
| NoSlow | Prevents item use/soul sand/honey slowdown |
| InventoryMove | Move while inventory is open |
| SafeWalk | Prevents walking off edges |
| Spider | Climb any wall |
| Strafe | Improved air control |
| NoPush | Prevents entity/water/piston push |
| AirJump | Jump while in the air |
| HighJump | Increased jump height |
| NoJumpDelay | Removes delay between jumps |
| Sneak | Auto-sneak without holding key |
| NoWeb | Prevents cobweb slowdown |
| Parkour | Auto-jump at block edges |
| TargetStrafe | Circle-strafe around combat target |
| AntiLevitation | Removes levitation/slow falling effects |
| AutoWalk | Automatically holds forward key |
| Teleport | Teleport to where you're looking |
</details>

<details>
<summary><b>Render (31)</b></summary>

| Module | Description |
|--------|-------------|
| ESP | Entity highlighting with glow effect |
| Fullbright | Night vision effect |
| Xray | See through blocks to find ores |
| Tracers | Lines drawn to entities |
| Nametags | Enhanced nametags with health and distance |
| NoWeather | Disables rain and thunder |
| StorageESP | Highlights storage containers |
| HoleESP | Crystal PvP hole detection |
| Chams | Entity rendering through walls |
| BreakHighlight | Block breaking progress overlay |
| FreeLook | Look around without changing movement direction |
| Trajectories | Predicted path of thrown projectiles |
| NewChunks | Highlights newly generated chunks |
| Zoom | Spyglass-like zoom |
| NoBob | Disables view bobbing |
| NoFov | Locks FOV to prevent speed/potion changes |
| NoHurtCam | Removes damage screen shake |
| AntiBlind | Removes blindness/darkness/nausea effects |
| NoSwing | Hides hand swing animation |
| ItemESP | Highlights dropped items |
| Breadcrumbs | Trail showing where you've been |
| CameraClip | Camera clips through blocks in third person |
| DamageParticles | Floating damage numbers |
| Radar | Minimap radar with nearby entities |
| TNTTimer | Shows time until TNT explodes |
| VoidESP | Highlights holes to the void |
| BlockOutline | Custom block selection outline |
| LogoffSpot | Marks where players log off |
| Crosshair | Custom crosshair with configurable style |
| Animations | 1.7-style hand/item animations |
| TrueSight | See invisible entities and barriers |
</details>

<details>
<summary><b>Player (18)</b></summary>

| Module | Description |
|--------|-------------|
| AutoMine | Auto-mine block under crosshair |
| FastPlace | Removes block placement delay |
| AutoEat | Auto-eat when hunger is low |
| ChestStealer | Auto-loot chests |
| Scaffold | Auto-place blocks under you while walking |
| AutoFish | Automated fishing |
| Freecam | Detached camera with free movement |
| AutoGap | Auto-eat golden apples when low HP |
| AutoDisconnect | Disconnect when health is critical |
| InventorySort | Sort inventory by item type priority |
| Blink | Queue movement packets, release on disable |
| Eagle | Auto-sneak at edges for bridging |
| AntiVoid | Teleport to safety when falling into void |
| AutoRespawn | Auto-respawn on death |
| FastUse | Removes item use delay |
| InventoryCleaner | Auto-drop junk items |
| Offhand | Smart totem/crystal offhand management |
| Replenish | Auto-refill hotbar stacks from inventory |
</details>

<details>
<summary><b>World (14)</b></summary>

| Module | Description |
|--------|-------------|
| Nuker | Mass block breaking |
| Timer | Game tick speed manipulation |
| AntiHunger | Reduces hunger drain |
| PacketMine | Mine via packets while doing other things |
| AutoSign | Auto-fill sign text |
| AntiAFK | Prevents AFK kick |
| AutoTool | Auto-switch to best tool for block |
| FastBreak | Increased mining speed |
| HoleFiller | Auto-fill 1x1 holes with obsidian |
| Surround | Place obsidian around feet for crystal protection |
| AirPlace | Place blocks in the air |
| NoSlowBreak | Prevents mining speed reduction |
| StrongholdFinder | Triangulate stronghold from eye throws |
| AutoFarm | Auto-harvest and replant crops |
</details>

<details>
<summary><b>Exploit (10)</b></summary>

| Module | Description |
|--------|-------------|
| Clip | Teleport through blocks (VClip/HClip) |
| Disabler | Confuse server-side anti-cheat systems |
| GhostHand | Open containers through walls |
| PingSpoof | Spoof your ping higher or lower |
| Plugins | Detect server plugins |
| PortalMenu | Open GUIs inside nether portals |
| NoPitchLimit | Remove 90-degree pitch limit |
| MultiActions | Eat and attack/place simultaneously |
| Damage | Self-damage via position exploit |
| MoreCarry | Carry items on cursor between screens |
</details>

<details>
<summary><b>Fun (3)</b></summary>

| Module | Description |
|--------|-------------|
| Derp | Spins your head randomly |
| SkinDerp | Rapidly toggles skin layer parts |
| Twerk | Rapid sneak toggle animation |
</details>

<details>
<summary><b>Misc (10)</b></summary>

| Module | Description |
|--------|-------------|
| AntiBot | Detects and filters fake players |
| BetterChat | Anti-spam, timestamps, infinite history |
| NameProtect | Hides your real username |
| Spammer | Auto-send chat messages on interval |
| Notifier | Alerts when players enter/leave visual range |
| PacketLogger | Logs all sent/received packets |
| Macros | Bind chat commands to keys |
| MiddleClickAction | Middle click to throw pearl at target |
| Teams | Detect team members, prevent targeting |
| FlagCheck | Detects anti-cheat flagging (lagbacks) |
</details>

## Commands

Aegis includes a chat command system (default prefix: `.`).

| Command | Description |
|---------|-------------|
| `.help` | List all commands |
| `.bind <module> <key>` | Bind a key to a module |
| `.toggle <module>` | Toggle a module |
| `.friend <add/remove/list> [name]` | Manage friends list |
| `.config <save/load/list> [name]` | Save/load configurations |
| `.vclip <distance>` | Vertical clip through blocks |
| `.hclip <distance>` | Horizontal clip through blocks |
| `.prefix <new>` | Change command prefix |


## Setting up a Workspace

Aegis uses Gradle and requires JDK 17+ to build.

1. Clone the repository using `git clone https://github.com/ArhanCodes/aegis.git`
2. `cd aegis`
3. Run `./gradlew genSources` to generate Minecraft sources
4. Open the project in IntelliJ IDEA or your preferred IDE
5. Run `./gradlew runClient` to test, or `./gradlew build` to compile

## Additional Libraries

### Mixins

Aegis uses [SpongePowered Mixin](https://github.com/SpongePowered/Mixin) for bytecode injection at runtime. See the [Mixin Wiki](https://github.com/SpongePowered/Mixin/wiki) for documentation

## Contributing

We appreciate contributions. If you want to improve Aegis, feel free to open a pull request
