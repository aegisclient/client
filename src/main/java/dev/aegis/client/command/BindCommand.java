package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.Module;
import dev.aegis.client.util.ChatHelper;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind", "Binds a key to a module", "bind <module> <key/none>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            ChatHelper.error("Usage: .bind <module> <key/none>");
            return;
        }

        Module mod = Aegis.getInstance().getModuleManager().getModule(args[0]);
        if (mod == null) {
            ChatHelper.error("Module not found: " + args[0]);
            return;
        }

        if (args[1].equalsIgnoreCase("none")) {
            mod.setKeyBind(GLFW.GLFW_KEY_UNKNOWN);
            ChatHelper.info("Unbound " + mod.getName());
            return;
        }

        // resolve key name to GLFW key code
        String keyName = "GLFW_KEY_" + args[1].toUpperCase();
        try {
            Field field = GLFW.class.getField(keyName);
            int keyCode = field.getInt(null);
            mod.setKeyBind(keyCode);
            ChatHelper.info("Bound " + mod.getName() + " to " + args[1].toUpperCase());
        } catch (Exception e) {
            ChatHelper.error("Unknown key: " + args[1]);
        }
    }
}
