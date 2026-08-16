package xyz.busterbrown1218.housingcreativetab.client;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.trim.ArmorTrim;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import xyz.busterbrown1218.housingcreativetab.utils.ClientIdentifierArgumentType;
import xyz.busterbrown1218.housingcreativetab.utils.ItemSuggestionProvider;
import xyz.busterbrown1218.housingcreativetab.utils.TrimMaterialSuggestionProvider;
import xyz.busterbrown1218.housingcreativetab.utils.TrimSuggestionProvider;

import java.util.ArrayList;
import java.util.List;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ItemCommands {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("rename")
                    .then(argument("name", StringArgumentType.greedyString())
                            .executes(itemCommand((context, item) -> {
                                item.set(DataComponentTypes.CUSTOM_NAME, Text.of(StringArgumentType.getString(context, "name").replaceAll("&([a-f0-9klmnor])", "§$1")));
                            }))));

            dispatcher.register(literal("itemmodel")
                    .then(argument("id", ClientIdentifierArgumentType.identifier())
                            .suggests(new ItemSuggestionProvider())
                            .executes(itemCommand(((context, item) -> {
                                item.set(DataComponentTypes.ITEM_MODEL, ClientIdentifierArgumentType.getIdentifier(context, "id"));
                            })))));

            dispatcher.register(literal("damage")
                    .then(argument("amount", IntegerArgumentType.integer(0))
                            .executes(itemCommand(((context, item) -> {
                                item.set(DataComponentTypes.DAMAGE, IntegerArgumentType.getInteger(context, "amount"));
                            })))));

            dispatcher.register(literal("unbreakable")
                    .then(argument("value", BoolArgumentType.bool())
                            .executes(itemCommand((context, item) -> {
                                if (BoolArgumentType.getBool(context, "value")) item.set(DataComponentTypes.UNBREAKABLE, Unit.INSTANCE);
                                else item.remove(DataComponentTypes.UNBREAKABLE);
                            }))));

            dispatcher.register(literal("lore")
                    .then(literal("add")
                            .then(argument("value", StringArgumentType.greedyString())
                                    .executes(itemCommand((context, item) -> {
                                        LoreComponent lore = item.get(DataComponentTypes.LORE);

                                        List<Text> newLines = new ArrayList<>();
                                        if (lore != null) {
                                            newLines.addAll(lore.lines());
                                        }

                                        newLines.add(Text.of(StringArgumentType.getString(context, "value").replaceAll("&([a-f0-9klmnor])", "§$1")));

                                        item.set(DataComponentTypes.LORE, new LoreComponent(newLines));
                                    }))))

                    .then(literal("remove")
                            .then(argument("index", IntegerArgumentType.integer(0))
                                    .executes(itemCommand((context, item) -> {
                                        LoreComponent lore = item.get(DataComponentTypes.LORE);
                                        if (lore == null || lore.lines().isEmpty()) return;

                                        List<Text> newLines = new ArrayList<>(lore.lines());
                                        int index = IntegerArgumentType.getInteger(context, "index");

                                        if (index < newLines.size()) {
                                            newLines.remove(index);
                                            item.set(DataComponentTypes.LORE, new LoreComponent(newLines));
                                        }
                                    }))))

                    .then(literal("edit")
                            .then(argument("index", IntegerArgumentType.integer(0))
                                    .then(argument("value", StringArgumentType.greedyString())
                                            .executes(itemCommand(((context, item) -> {
                                                LoreComponent lore = item.get(DataComponentTypes.LORE);
                                                if (lore == null || lore.lines().isEmpty()) return;

                                                List<Text> newLines = new ArrayList<>(lore.lines());
                                                int index = IntegerArgumentType.getInteger(context, "index");

                                                if (index < newLines.size()) {
                                                    newLines.set(index, Text.of(StringArgumentType.getString(context, "value").replaceAll("&([a-f0-9klmnor])", "§$1")));
                                                    item.set(DataComponentTypes.LORE, new LoreComponent(newLines));
                                                }
                                            }))))))
            );

            dispatcher.register(literal("itemtype")
                    .then(argument("id", ClientIdentifierArgumentType.identifier())
                            .suggests(new ItemSuggestionProvider())
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                PlayerEntity player = client.player;

                                if (cannotEdit(player)) return Command.SINGLE_SUCCESS;

                                ItemStack item = player.getStackInHand(player.getActiveHand());

                                setItem(client, player, item.withItem(new ItemStack(Registries.ITEM.get(ClientIdentifierArgumentType.getIdentifier(context, "id"))).getItem()));

                                return Command.SINGLE_SUCCESS;
                            })));

            dispatcher.register(literal("armorcolor")
                    .then(argument("hex", StringArgumentType.word())
                            .executes(itemCommand(((context, item) -> {
                                String value = StringArgumentType.getString(context, "hex");
                                if (!value.matches("[a-fA-F0-9]{6}")) {
                                    context.getSource().getPlayer().sendMessage(Text.of("Invalid hex format! Digits must be (0-9, a-f). §8Example: §fFFFFFF"), false);
                                    return;
                                }
                                int color = Integer.parseInt(value, 16);
                                item.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(color));
                            })))));

            dispatcher.register(literal("armortrim")
                    .then(argument("template", ClientIdentifierArgumentType.identifier())
                            .suggests(new TrimSuggestionProvider())
                            .then(argument("material", ClientIdentifierArgumentType.identifier())
                                    .suggests(new TrimMaterialSuggestionProvider())
                                    .executes(itemCommand(((context, item) -> {
                                        Identifier templateId = ClientIdentifierArgumentType.getIdentifier(context, "template");
                                        Identifier materialId = ClientIdentifierArgumentType.getIdentifier(context, "material");

                                        Registry<ArmorTrimPattern> patternRegistry = context.getSource().getWorld().getRegistryManager().getOrThrow(RegistryKeys.TRIM_PATTERN);
                                        Registry<ArmorTrimMaterial> materialRegistry = context.getSource().getWorld().getRegistryManager().getOrThrow(RegistryKeys.TRIM_MATERIAL);

                                        RegistryEntry<ArmorTrimPattern> patternEntry = patternRegistry.getEntry(templateId).orElse(null);
                                        RegistryEntry<ArmorTrimMaterial> materialEntry = materialRegistry.getEntry(materialId).orElse(null);

                                        if (patternEntry == null || materialEntry == null) return;

                                        item.set(DataComponentTypes.TRIM, new ArmorTrim(materialEntry, patternEntry));
                                    }))))));

            dispatcher.register(literal("count")
                    .then(argument("amount", IntegerArgumentType.integer(1, 64))
                            .executes(itemCommand((context, item) -> {
                                item.setCount(IntegerArgumentType.getInteger(context, "amount"));
                            }))));
        });
    }

    private static boolean cannotEdit(PlayerEntity player) {
        if (!player.isInCreativeMode()) {
            player.sendMessage(Text.of("You must be in creative to edit an item!"), false);
            return true;
        } else if (player.getStackInHand(player.getActiveHand()).isEmpty()) {
            player.sendMessage(Text.of("You must be holding an item to edit!"), false);
            return true;
        }
        return false;
    }

    private static void setItem(MinecraftClient client, PlayerEntity player, ItemStack item) {
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendPacket(
                    new CreativeInventoryActionC2SPacket(player.getInventory().getSelectedSlot() + 36, item)
            );
        }
    }

    @FunctionalInterface
    interface ItemCommandAction {
        void run(CommandContext<FabricClientCommandSource> context, ItemStack item);
    }

    private static Command<FabricClientCommandSource> itemCommand(ItemCommandAction action) {
        return context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;

            if (cannotEdit(player)) return Command.SINGLE_SUCCESS;

            ItemStack item = player.getStackInHand(player.getActiveHand());

            action.run(context, item);

            setItem(client, player, item);

            return Command.SINGLE_SUCCESS;
        };
    }
}
