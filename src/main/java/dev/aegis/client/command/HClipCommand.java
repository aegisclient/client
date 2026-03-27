package dev.aegis.client.command;

import dev.aegis.client.Aegis;
import dev.aegis.client.module.exploit.Clip;
import dev.aegis.client.util.ChatHelper;

public class HClipCommand extends Command {

    public HClipCommand() {
        super("hclip", "Horizontal clip through blocks", "hclip <distance>");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            ChatHelper.error("Usage: .hclip <distance>");
            return;
        }

        try {
            double dist = Double.parseDouble(args[0]);
            Clip clip = (Clip) Aegis.getInstance().getModuleManager().getModule("Clip");
            if (clip != null) {
                clip.hClip(dist);
            }
        } catch (NumberFormatException e) {
            ChatHelper.error("Invalid number: " + args[0]);
        }
    }
}
