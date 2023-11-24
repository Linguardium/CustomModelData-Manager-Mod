package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public interface ChildGuiFactory<T> {
    GuiInterface create(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<T> onCloseCallback);
}
