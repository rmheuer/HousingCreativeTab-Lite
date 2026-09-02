package xyz.busterbrown1218.housingcreativetab.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.busterbrown1218.housingcreativetab.client.HousingCreativeTabClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static xyz.busterbrown1218.housingcreativetab.client.HousingCreativeTabClient.*;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryMixin {
    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    protected abstract void selectTab(CreativeModeTab group);
    @Shadow
    protected abstract void refreshSearchResults();

    @Unique
    private Button toggleButton;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;addWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private void init(CallbackInfo ci) {
        if (!hasSetHideItems) {
            hideItems = Minecraft.getInstance().level.getScoreboard().getObjectiveNames().contains("housing");
            hasSetHideItems = true;
        }

        ScreenAccessor accessor = (ScreenAccessor) (Object) this;

        toggleButton = new Button.Builder(Component.literal("Housing Items: " + (hideItems ? "Enabled" : "Disabled")), (widget) -> {
            hideItems = !hideItems;
            toggleButton.setMessage(Component.literal("Housing Items: " + (hideItems ? "Enabled" : "Disabled")));

            if (selectedTab.getType() == CreativeModeTab.Type.SEARCH)selectTab(selectedTab);
            else refreshSearchResults();
        }).build();
        toggleButton.setPosition(accessor.getScreenWidth() / 2 - toggleButton.getWidth() / 2, accessor.getScreenHeight() - toggleButton.getHeight() - 1);
        toggleButton.visible = true;
        accessor.addDrawableChildInvoker(toggleButton);
    }

    @WrapOperation(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/CreativeModeTab;getDisplayItems()Ljava/util/Collection;"))
    private Collection<ItemStack> filterCreativeItems(CreativeModeTab instance, Operation<Collection<ItemStack>> original) {
        Collection<ItemStack> items = new ArrayList<>(original.call(instance));
        if (!hideItems) return items;
        items.removeIf(this::shouldHide);
        if (items.isEmpty()) items.add(ItemStack.EMPTY);

        List<ItemStack> updatedItems = new ArrayList<>();

        for (ItemStack item : items) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item.getItem());

            if (DATA_VALUES.containsKey(id)) {
                Pair<Identifier, Integer> data = DATA_VALUES.get(id);

                ItemStack updatedItem = item.transmuteCopy(new ItemStack(BuiltInRegistries.ITEM.getValue(data.getFirst())).getItem());
                if (data.getSecond() != -1) updatedItem.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§7Data Value: " + data.getSecond()))));
                updatedItem.set(DataComponents.ITEM_MODEL, id);

                updatedItems.add(updatedItem);
            } else {
                updatedItems.add(item);
            }
        }

        items.clear();
        items.addAll(updatedItems);

        return items;
    }

    @WrapOperation(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z"))
    private boolean filterSearchItems(NonNullList instance, Collection<ItemStack> items, Operation<Boolean> original) {
        if (!hideItems) return original.call(instance, items);
        Collection<ItemStack> operableItems = new ArrayList<>(items);
        operableItems.removeIf(this::shouldHide);
        List<ItemStack> updatedItems = new ArrayList<>();
        for (ItemStack item : operableItems) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item.getItem());

            if (DATA_VALUES.containsKey(id)) {
                Pair<Identifier, Integer> data = DATA_VALUES.get(id);

                ItemStack updatedItem = item.transmuteCopy(new ItemStack(BuiltInRegistries.ITEM.getValue(data.getFirst())).getItem());
                if (data.getSecond() != -1) updatedItem.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("§7Data Value: " + data.getSecond()))));
                updatedItem.set(DataComponents.ITEM_MODEL, id);

                updatedItems.add(updatedItem);
            } else {
                updatedItems.add(item);
            }
        }
        return original.call(instance, updatedItems);
    }

    @Unique
    private boolean shouldHide(ItemStack stack) {
        return !HousingCreativeTabClient.ALLOWED_1_8_9_ITEMS.contains(stack.getItem());
    }
}
