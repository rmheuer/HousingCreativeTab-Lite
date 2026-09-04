package xyz.busterbrown1218.housingcreativetab.mixin;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("width")
    int housingcreativetab$getScreenWidth();

    @Accessor("height")
    int housingcreativetab$getScreenHeight();

    @SuppressWarnings("UnusedReturnValue")
    @Invoker("addRenderableWidget")
    <T extends GuiEventListener & Renderable & NarratableEntry> T housingcreativetab$addDrawableChildInvoker(T child);
}
