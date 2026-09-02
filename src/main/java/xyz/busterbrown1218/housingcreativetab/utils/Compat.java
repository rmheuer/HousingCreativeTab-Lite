package xyz.busterbrown1218.housingcreativetab.utils;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;import net.minecraft.world.level.Level;

public class Compat {
    public static void sendPlayerMessage(Player player, Component message) {
        //?if >= 26.2 {
        player.sendSystemMessage(message);
        //?} else {
            /*player.displayClientMessage(message, false);
         *///?}
    }
    public static Level getLevel(CommandContext<FabricClientCommandSource> context) {
        //?if >= 26.2 {
        return context.getSource().getLevel();
        //?} else {
        /*return context.getSource().getWorld();
         *///?}
    }
}
