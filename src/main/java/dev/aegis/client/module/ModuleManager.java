package dev.aegis.client.module;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.combat.*;
import dev.aegis.client.module.movement.*;
import dev.aegis.client.module.render.*;
import dev.aegis.client.module.player.*;
import dev.aegis.client.module.world.*;
import dev.aegis.client.module.exploit.*;
import dev.aegis.client.module.fun.*;
import dev.aegis.client.module.misc.*;
import dev.aegis.client.module.premium.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    public void init() {
        // combat (21)
        register(new KillAura());
        register(new Reach());
        register(new Criticals());
        register(new AutoTotem());
        register(new AutoArmor());
        register(new BowAimbot());
        register(new AutoCrystal());
        register(new Velocity());
        register(new TriggerBot());
        register(new Anchor());
        register(new AutoClicker());
        register(new KeepSprint());
        register(new BackTrack());
        register(new FakeLag());
        register(new Hitbox());
        register(new SuperKnockback());
        register(new AutoWeapon());
        register(new AutoLeave());
        register(new TickBase());
        register(new AutoRod());
        register(new NoMissCooldown());

        // movement (24)
        register(new Flight());
        register(new Speed());
        register(new NoFall());
        register(new Sprint());
        register(new Step());
        register(new Jesus());
        register(new BoatFly());
        register(new ElytraFly());
        register(new Phase());
        register(new LongJump());
        register(new EntitySpeed());
        register(new NoSlow());
        register(new InventoryMove());
        register(new SafeWalk());
        register(new Spider());
        register(new Strafe());
        register(new NoPush());
        register(new AirJump());
        register(new HighJump());
        register(new NoJumpDelay());
        register(new Sneak());
        register(new NoWeb());
        register(new Parkour());
        register(new TargetStrafe());
        register(new AntiLevitation());
        register(new AutoWalk());
        register(new Teleport());

        // render (23)
        register(new ESP());
        register(new Fullbright());
        register(new Xray());
        register(new Tracers());
        register(new Nametags());
        register(new NoWeather());
        register(new StorageESP());
        register(new HoleESP());
        register(new Chams());
        register(new BreakHighlight());
        register(new FreeLook());
        register(new Trajectories());
        register(new NewChunks());
        register(new Zoom());
        register(new NoBob());
        register(new NoFov());
        register(new NoHurtCam());
        register(new AntiBlind());
        register(new NoSwing());
        register(new ItemESP());
        register(new Breadcrumbs());
        register(new CameraClip());
        register(new DamageParticles());
        register(new Radar());
        register(new TNTTimer());
        register(new VoidESP());
        register(new BlockOutline());
        register(new LogoffSpot());
        register(new Crosshair());
        register(new Animations());
        register(new TrueSight());

        // player (18)
        register(new AutoMine());
        register(new FastPlace());
        register(new AutoEat());
        register(new ChestStealer());
        register(new Scaffold());
        register(new AutoFish());
        register(new Freecam());
        register(new AutoGap());
        register(new AutoDisconnect());
        register(new InventorySort());
        register(new Blink());
        register(new Eagle());
        register(new AntiVoid());
        register(new AutoRespawn());
        register(new FastUse());
        register(new InventoryCleaner());
        register(new Offhand());
        register(new Replenish());

        // world (14)
        register(new Nuker());
        register(new Timer());
        register(new AntiHunger());
        register(new PacketMine());
        register(new AutoSign());
        register(new AntiAFK());
        register(new AutoTool());
        register(new FastBreak());
        register(new HoleFiller());
        register(new Surround());
        register(new AirPlace());
        register(new NoSlowBreak());
        register(new StrongholdFinder());
        register(new AutoFarm());

        // exploit (10)
        register(new Clip());
        register(new Disabler());
        register(new GhostHand());
        register(new PingSpoof());
        register(new Plugins());
        register(new PortalMenu());
        register(new NoPitchLimit());
        register(new MultiActions());
        register(new Damage());
        register(new MoreCarry());

        // fun (3)
        register(new Derp());
        register(new SkinDerp());
        register(new Twerk());

        // misc (8)
        register(new AntiBot());
        register(new BetterChat());
        register(new NameProtect());
        register(new Spammer());
        register(new Notifier());
        register(new PacketLogger());
        register(new Macros());
        register(new MiddleClickAction());
        register(new Teams());
        register(new FlagCheck());

        // premium (8) - only register if premium key is detected
        if (Aegis.getInstance().getPremiumManager().isPremium()) {
            register(new HypixelDisabler());
            register(new HypixelSpeed());
            register(new HypixelFlight());
            register(new HypixelScaffold());
            register(new HypixelKillAura());
            register(new HypixelBowAimbot());
            register(new ServerBypasses());
            register(new AegisCape());
            register(new PremiumInfo());
            Aegis.LOGGER.info("Premium modules loaded!");
        }

        Aegis.LOGGER.info("Registered {} modules", modules.size());
    }

    private void register(Module mod) {
        modules.add(mod);
    }

    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // handle keybinds
        long window = mc.getWindow().getHandle();
        for (Module mod : modules) {
            if (mod.getKeyBind() != GLFW.GLFW_KEY_UNKNOWN) {
                if (GLFW.glfwGetKey(window, mod.getKeyBind()) == GLFW.GLFW_PRESS) {
                    if (!keyStates.contains(mod.getKeyBind())) {
                        keyStates.add(mod.getKeyBind());
                        mod.toggle();
                    }
                } else {
                    keyStates.remove(Integer.valueOf(mod.getKeyBind()));
                }
            }
        }

        // tick enabled modules
        for (Module mod : modules) {
            if (mod.isEnabled()) {
                try {
                    mod.onTick();
                } catch (Exception e) {
                    Aegis.LOGGER.error("Error ticking module {}: {}", mod.getName(), e.getMessage());
                }
            }
        }
    }

    private final List<Integer> keyStates = new ArrayList<>();

    public List<Module> getModules() { return modules; }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .collect(Collectors.toList());
    }
}
