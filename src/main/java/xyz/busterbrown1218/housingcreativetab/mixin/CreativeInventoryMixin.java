package xyz.busterbrown1218.housingcreativetab.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryMixin {
    @Shadow
    private static ItemGroup selectedTab;

    @Shadow
    protected abstract void setSelectedTab(ItemGroup group);

    @Unique
    private ButtonWidget toggleButton;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/CreativeInventoryScreen;addSelectableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private void init(CallbackInfo ci) {
        if (!hasSetHideItems) {
            hideItems = MinecraftClient.getInstance().world.getScoreboard().getObjectiveNames().contains("housing");
            hasSetHideItems = true;
        }

        ScreenAccessor accessor = (ScreenAccessor) (Object) this;

        toggleButton = new ButtonWidget.Builder(Text.of("Housing Items: " + (hideItems ? "Enabled" : "Disabled")), (widget) -> {
            hideItems = !hideItems;
            toggleButton.setMessage(Text.of("Housing Items: " + (hideItems ? "Enabled" : "Disabled")));

            setSelectedTab(selectedTab);
        }).build();
        toggleButton.setPosition(accessor.getScreenWidth() / 2 - toggleButton.getWidth() / 2, accessor.getScreenHeight() - toggleButton.getHeight() - 1);
        toggleButton.visible = true;
        accessor.addDrawableChildInvoker(toggleButton);
    }

    @WrapOperation(method = "setSelectedTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemGroup;getDisplayStacks()Ljava/util/Collection;"))
    private Collection<ItemStack> filterCreativeItems(ItemGroup instance, Operation<Collection<ItemStack>> original) {
        Collection<ItemStack> items = new ArrayList<>(original.call(instance));
        if (!hideItems) return items;
        items.removeIf(this::shouldHide);
        if (items.isEmpty()) items.add(ItemStack.EMPTY);

        List<ItemStack> updatedItems = new ArrayList<>(items.size());

        for (ItemStack item : items) {
            Identifier id = Registries.ITEM.getId(item.getItem());

            if (DATA_VALUES.containsKey(id)) {
                Pair<Identifier, Integer> data = DATA_VALUES.get(id);

                ItemStack updatedItem = item.withItem(new ItemStack(Registries.ITEM.get(data.getLeft())).getItem());
                if (data.getRight() != -1) updatedItem.set(DataComponentTypes.LORE, new LoreComponent(List.of(Text.of("§7Data Value: " + data.getRight()))));
                updatedItem.set(DataComponentTypes.ITEM_MODEL, id);

                updatedItems.add(updatedItem);
            } else {
                updatedItems.add(item);
            }
        }

        items.clear();
        items.addAll(updatedItems);

        return items;
    }

    @Unique
    private boolean shouldHide(ItemStack stack) {
        return !HousingCreativeTabClient.ALLOWED_1_8_9_ITEMS.contains(stack.getItem());
    }
}
