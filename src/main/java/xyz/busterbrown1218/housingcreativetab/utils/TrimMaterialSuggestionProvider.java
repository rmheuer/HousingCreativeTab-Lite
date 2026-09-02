package xyz.busterbrown1218.housingcreativetab.utils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TrimMaterialSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Registry<TrimMaterial> materialRegistry = Compat.getLevel(context).registryAccess().lookup(Registries.TRIM_MATERIAL).get();
        Collection<String> materialIds = materialRegistry.keySet().stream().map(id -> id.getNamespace() + ":" + id.getPath()).collect(Collectors.toSet());
        for (String id : materialIds) {
            builder.suggest(id);
        }

        return builder.buildFuture();
    }
}