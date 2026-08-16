package xyz.busterbrown1218.housingcreativetab.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TrimSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Registry<ArmorTrimPattern> patternRegistry = context.getSource().getWorld().getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN);
        Collection<String> trimIds = patternRegistry.getIds().stream().map(id -> id.getNamespace() + ":" + id.getPath()).collect(Collectors.toSet());

        for (String id : trimIds) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }
}