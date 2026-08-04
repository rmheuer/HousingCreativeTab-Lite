package xyz.busterbrown1218.housingcreativetab.mixin;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Accessor("width")
    int getScreenWidth();

    @Accessor("height")
    int getScreenHeight();

    @Invoker("addDrawableChild")
    <T extends Element & Drawable> T addDrawableChildInvoker(T child);
}
