# The Last Stand

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Java%20Swing-5382A1?style=for-the-badge&logo=java&logoColor=white)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Contributors](https://img.shields.io/badge/Contributors-2-orange?style=for-the-badge)
![Waves](https://img.shields.io/badge/Waves-15-red?style=for-the-badge)
![Bullet Types](https://img.shields.io/badge/Bullet%20Types-9-purple?style=for-the-badge)

> A 2D wave-based tower defense shooter built in Java. Protect the treasure, eliminate enemies, and master nine unique projectile types across fifteen escalating waves.

---

## Table of Contents

1. [Overview](#overview)
2. [Team](#team)
3. [Gameplay](#gameplay)
4. [Controls](#controls)
5. [Entities](#entities)
6. [Bullet Types](#bullet-types)
7. [Underground System](#underground-system)
8. [Wave and Level Progression](#wave-and-level-progression)
9. [Architecture](#architecture)
10. [Project Structure](#project-structure)
11. [Getting Started](#getting-started)
12. [Asset Credits](#asset-credits)

---

## Overview

The Last Stand is a 2D action-strategy game built entirely in Java using Swing for rendering. Inspired by survival modes such as Terraria's Old One's Army, the player defends a central treasure chest against waves of monsters advancing from both sides of a scrolling battlefield. The core loop demands that the player read each enemy type, select the appropriate projectile, and manage positioning across both the surface and an underground zone.

The game ships with nine enemy types, nine projectile types with distinct mechanics, a parallax scrolling surface environment, a procedurally decorated underground dungeon, portal-based enemy spawning, a coin economy tied to damage upgrades, and a save/load system that persists progress between sessions.

---

## Team

| Contributor | Responsibilities |
|---|---|
| [**Adrian Deo**](https://github.com/Adrian-Deo) | Controls and player mechanics, all bullet types and the `AnimatedBullet` class, bullet-monster interaction logic, underground background and pack system, UI buttons, `SolidObjectManager` level design, `GameWindow` rework |
| [**Josiah Alexis**](https://github.com/JOSIAHTHEPROGRAMMER) | Player and monster animations, `WaveManager`, `BackgroundManager` parallax, `Treasure`, portals and fireballs, `Monster` abstract class, status effects (burn, freeze), original Assignment 1 codebase |

---

## Gameplay

The player spawns near the treasure at the center of the map. Enemies emerge from portals distributed across the level and march toward the treasure. Each wave increases in enemy count, variety, and speed. Every third wave marks the end of a level, at which point a summary screen displays unlocked bullet types before the next level begins.

When the treasure's HP reaches zero the game ends in defeat. Surviving all fifteen waves across five levels triggers a win.

Between waves the player may descend underground by pressing Shift. The underground contains health packs that restore treasure HP and damage packs that permanently amplify the player's attack power in exchange for coins earned from kills. Returning to the surface resumes combat.

---

## Controls

| Input | Action |
|---|---|
| `A` / `D` | Move left / right |
| `Space` | Jump |
| `Left Mouse Button` | Shoot toward cursor |
| `Mouse Drag` | Continuous fire |
| `1` through `9` | Switch bullet type |
| `Shift` | Toggle underground / surface |
| `Escape` | Pause / resume |

---

## Entities

### Player

The player character is a keyboard-and-mouse-controlled unit capable of horizontal movement, jumping, and directional shooting. The player stores the currently selected bullet type and a damage multiplier that scales with purchased upgrades. Stomping on enemies while falling deals 40 damage and bounces the player upward.

### Treasure

The treasure is the object the player must protect. It maintains an HP value, takes damage from any enemy that reaches it, and changes visually as its health decreases. The game ends when it is destroyed.

### Enemies

| Enemy | Key Trait | Recommended Counter |
|---|---|---|
| **Ghost** | Immune to physical damage, cannot be stomped | Spirit Bullet |
| **Snake** | High HP, tanky | Freeze Bullet for instant elimination |
| **Shadow Walker** | Invisible during movement | Electric Bullet reveals it |
| **Armored Turtle** | Very high defense | Piercing Bullet bypasses armor |
| **Fire Imp** | Ranged attacker, fires projectiles | Freeze Bullet to slow and interrupt |
| **Split Slime** | Splits into two Mini Slimes on death | Fire or Freeze prevents splitting |
| **Healer** | Restores HP to nearby allies | Prioritize immediately |
| **Shield Guardian** | Frontal shield blocks bullets | Attack from behind or use Explosive |
| **Berserker Orc** | Enters a rage state at low HP | Freeze Bullet before the rage triggers |

---

## Bullet Types

All nine bullet types are unlocked progressively through level completions. Keys `1` through `9` cycle between them in-game.

| Key | Type | Mechanic |
|---|---|---|
| `1` | Basic | Standard projectile, no special effect |
| `2` | Fire | Applies a burn-over-time status effect |
| `3` | Freeze | Slows the target and can prevent Split Slime from splitting |
| `4` | Electric | Chains lightning between nearby enemies |
| `5` | Spirit | Effective against Ghosts; applies knockback |
| `6` | Rapid | Reduced cooldown, high fire rate |
| `7` | Piercing | Passes through multiple enemies in a line |
| `8` | Explosive | Deals area-of-effect damage on impact |
| `9` | Teleport | Displaces the target away from the treasure |

Attempting to use a locked bullet type displays a warning in the HUD without switching.

---

## Underground System

Pressing `Shift` during gameplay initiates a transition animation and moves the player into the underground zone. The transition takes twelve ticks, darkening the screen at the midpoint before revealing the dungeon.

The underground is a hand-decorated cavern with animated torch lights and floating glowing particles. It contains two categories of pickup:

**Health Packs** are scattered at four fixed positions across the world. Walking over one heals the treasure by 75 HP and removes the pack. If all four are collected, a respawn timer begins and restores them after 360 ticks. A countdown is displayed when packs are absent.

**Damage Packs** are tiered upgrades (Bronze, Silver, Gold) that permanently multiply the player's bullet damage. Each has a coin cost deducted from the player's total on collection. Purchased packs become unavailable for the rest of the session.

Bullets fired on the surface are cleared upon descending, and the player loses access to platform collision while underground.

---

## Wave and Level Progression

The game is divided into five levels, each comprising three waves, for a total of fifteen waves. The `WaveManager` maintains the ordered list of waves, the set of unlocked enemies, and the set of unlocked bullet types. Each call to `nextWave` advances the internal pointer and returns a `SpawnData` object describing the current wave number, level, and list of monster classes to spawn.

Enemy spawning is portal-based from Level 2 onward. Monsters are placed in a queue and released one at a time every 35 ticks, preventing frame-rate spikes from simultaneous instantiation. Each monster is assigned a portal at random and directed toward the treasure on spawn.

From Level 3 onward, Split Slimes can fall from above the visible area onto the player's current camera position. The drop interval decreases with each level, increasing surface pressure.

Upon completing every third wave a `LEVEL_COMPLETE` screen appears, listing the current score, coins, and all bullet types unlocked so far. Progress is written to `data/save.dat` at this point. On the main menu a Continue button appears when a save file is present, allowing the player to resume from the last completed level.

---

## Architecture

The codebase follows a layered object-oriented design.

**`GameApplication`** is the entry point. It instantiates `GameWindow` and nothing else.

**`GameWindow`** is the central controller. It owns the game loop (running at 20 ticks per second via a dedicated thread), all top-level entity collections, input handling, state machine transitions (`MENU`, `PLAYING`, `PAUSED`, `LEVEL_COMPLETE`, `GAME_OVER`, `WIN`), double-buffered rendering to an off-screen `BufferedImage`, and camera tracking with parallax delegation to `BackgroundManager`.

**`Monster` (abstract)** defines movement, bounding rectangle, HP, damage handling, and the shared monster list reference used by Healers and electric chain effects. Concrete subclasses override `move()`, `draw()`, and damage responses.

**`Bullet` (abstract)** defines velocity, activity state, previous-position tracking for swept collision, and the `onHit` hook. `AnimatedBullet` extends it to add sprite animation support. All nine concrete bullet classes extend `AnimatedBullet` and implement their special effects in `onHit`.

**`WaveManager`** drives all progression logic. It is the single source of truth for which enemies and bullet types are available at any point in the game.

**`SolidObjectManager`** loads level geometry from internal configuration and exposes it to the `Player` for platform collision and to `GameWindow` for portal placement.

**`BackgroundManager`** manages a stack of `Background` layers with independent horizontal and vertical parallax factors, producing the multi-plane scrolling effect on the surface.

**`SoundManager`** is a singleton that caches and plays `Clip` instances loaded from the `/sounds/` directory.

**`ImageManager`** caches `BufferedImage` instances loaded from the `/images/` directory to avoid repeated disk reads.

The simplified class relationships are:

```
GameApplication
    └── GameWindow
            ├── BackgroundManager ──► Background
            ├── WaveManager ──────────────────────► Monster (0..*)
            ├── Player ────────────────────────────► BulletType
            ├── Treasure
            ├── Bullet (0..*) ─► AnimatedBullet ─► (concrete bullets)
            └── Animation  ◄──── Monster, AnimatedBullet
```

---

## Project Structure

```
project-root/
├── images/
│   ├── armored_turtle/        Walk animation frames (left/right, 8 frames each)
│   ├── berserker_orc/         Walk and death animations
│   ├── bullets/               Projectile sprites and status effect overlays
│   ├── fire_imp/              Walk, death, and fireball sprites
│   ├── healer/                Walk, death, weak heal, and strong heal animations
│   ├── player/                Walking, jumping, and falling frames
│   ├── portal/                Eight-frame portal animation
│   ├── shadow_walker/         Walk frames
│   ├── shield_guardian/       Walk frames and shield sprite
│   ├── snake/                 Left/right static sprites
│   ├── split_slime/           Walk, split, and death frames
│   ├── surface/               Parallax background layers
│   ├── ghost.png
│   ├── gun.png
│   ├── player_left.png
│   └── player_right.png
├── sounds/
│   ├── background.wav         Looping music track
│   ├── BerserkerOrc_die.wav
│   ├── FireImp_die.wav
│   ├── ghost_die.wav
│   ├── Healer_die.wav
│   ├── HitSound.wav
│   ├── HitSound2.wav
│   ├── player_shoot.wav
│   ├── ShadowWalker_die.wav
│   ├── Shield_die.wav
│   ├── ShieldGuardian_die.wav
│   ├── slime_die.wav
│   └── snake_die.wav
├── data/
│   └── save.dat               Auto-generated on level completion
├── GameApplication.java       Entry point
├── GameWindow.java            Game loop, input, rendering, state machine
├── Player.java
├── Monster.java               Abstract base
├── Bullet.java                Abstract base
├── AnimatedBullet.java        Animated bullet base
├── BulletType.java            Enum of all nine bullet types
├── WaveManager.java
├── SolidObjectManager.java
├── BackgroundManager.java
├── Background.java
├── SoundManager.java
├── ImageManager.java
├── Animation.java
├── WorldConfig.java           Global constants (world width, floor Y, view dimensions)
├── SpawnData.java
├── Treasure.java
├── Portal.java
├── HealthPack.java
├── DamagePack.java
├── [Monster subclasses]       Snake, Ghost, ShadowWalker, ArmoredTurtle, FireImp,
│                              SplitSlime, MiniSlime, Healer, ShieldGuardian, BerserkerOrc
├── [Bullet subclasses]        BasicBullet, FireBullet, FreezeBullet, ElectricBullet,
│                              SpiritBullet, RapidBullet, PiercingBullet,
│                              ExplosiveBullet, TeleportBullet
└── [FX classes]               BurnFX, FreezeFX, ElectricFX, ChainFX, FlickerDeathFX,
                               MonsterTintFX, ImageFX, GreyFX, DisappearFX
```

---

## Getting Started

**Requirements:** Java 17 or later. No external libraries or build tools are required.

**Compile:**

```bash
javac *.java
```

**Run:**

```bash
java GameApplication
```

The game window opens at 960×540 pixels and is not resizable. All assets are loaded relative to the working directory, so the command must be run from the project root where the `images/` and `sounds/` directories reside.

**Save data** is written to `data/save.dat` automatically at the end of each level. Delete this file to clear saved progress.

---

## Asset Credits

### Sprites

| Asset | Source |
|---|---|
| All bullet sprites | [pimen.itch.io](https://pimen.itch.io/) |
| Snake, Ghost | [github.com/JOSIAHTHEPROGRAMMER/Snake-game](https://github.com/JOSIAHTHEPROGRAMMER/Snake-game) |
| Fire Imp | [trevor-pupkin.itch.io — Roguelike Dungeon Asset Bundle](https://trevor-pupkin.itch.io/roguelike-dungeon-asset-bundle) |
| Berserker Orc | [craftpix.net — Free Top-Down Orc Character](https://craftpix.net/freebies/free-top-down-orc-game-character-pixel-art/) |
| Split Slime / Mini Slime | [craftpix.net — Free Slime Mobs Sprite Pack](https://craftpix.net/freebies/free-slime-mobs-pixel-art-top-down-sprite-pack/) |
| Healer | [free-game-assets.itch.io — Free Yokai Sprite Sheets](https://free-game-assets.itch.io/free-yokai-pixel-sprite-sheets) |
| Armored Turtle | [spriters-resource.com](https://www.spriters-resource.com/pc_computer/koumajoudensetsuiistrangersrequiem/asset/89113) |
| Shield Guardian | [spriters-resource.com](https://www.spriters-resource.com/neo_geo_ngcd/bluesjourneyraguy/asset/169761/) |
| Shadow Walker | [spriters-resource.com — FF6](https://www.spriters-resource.com/snes/ff6/asset/6702/) |
| Player | [gandalfhardcore.itch.io — 2D Pixel Art Warrior](https://gandalfhardcore.itch.io/2d-pixel-art-male-and-female-character) |
| Fireball | [xzany.itch.io — Flying Demon 2D Pixel Art](https://xzany.itch.io/flying-demon-2d-pixel-art) |
| Portal | [elthen.itch.io — 2D Pixel Art Portal Sprites](https://elthen.itch.io/2d-pixel-art-portal-sprites) |
| Surface Backgrounds | [craftpix.net — Free Fantasy 2D Battlegrounds](https://craftpix.net/freebies/free-pixel-art-fantasy-2d-battlegrounds/) |
| Electric Chain / Shock FX | [frostwindz.itch.io — Pixel Art Lightning Animations](https://frostwindz.itch.io/pixel-art-skill-animations-lightning) |

### Sound Effects

| File | Source |
|---|---|
| `slime_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-hurt-sound-435314/) |
| `ShadowWalker_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-slap-hurt-pain-sound-effect-262618/) |
| `BerserkerOrc_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-small-monster-attack-195712/) |
| `Healer_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-heal-sound-2-409138/) |
| `ShieldGuardian_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-falling-rock-105396/) |
| `Shield_die.wav` | [pixabay.com](https://pixabay.com/sound-effects/film-special-effects-shield-guard-6963/) |
| `ghost_die.wav` | [freesound.org](https://freesound.org/people/guilledcf/sounds/832370/) |
| `snake_die.wav` | [freesound.org](https://freesound.org/search/?q=snake) |
| `background.wav` | [incompetech.com — Dentaneosuchus Hunt](https://incompetech.com/music/royalty-free/music.html) |

---

## License

The source code in this repository is released under the [MIT License](LICENSE).

The third-party sprites and audio files located in the `images/` and `sounds/` directories are not covered by this license. Each asset retains the terms of its original author or distributor, as listed in the [Asset Credits](#asset-credits) section above.
