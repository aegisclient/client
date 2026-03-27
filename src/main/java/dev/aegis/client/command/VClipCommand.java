package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.exploit.Clip;
import dev.aegis.client.util.ChatHelper;

public class VClipCommand extends Command {

    public VClipCommand() {
        super("vclip", "Vertical clip through blocks", "vclip <distance>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.error("Usage: .vclip <distance>");
            return;
        }

        try {
            double dist = Double.parseDouble(args[0]);
            Clip clip = (Clip) Aegis.getInstance().getModuleManager().getModule("Clip");
            if (clip != null) {
                clip.vClip(dist);
            }
        } catch (NumberFormatException e) {
            ChatHelper.error("Invalid number: " + args[0]);
        }
    }
}
