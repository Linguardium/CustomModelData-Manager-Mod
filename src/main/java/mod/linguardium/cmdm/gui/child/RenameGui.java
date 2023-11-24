package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

public class RenameGui extends AnvilInputGui {
    protected final SlotGuiInterface parent;
    protected Consumer<String> closeCallback;
    public RenameGui(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<String> onCloseCallback) {
        super(player,false);
        this.parent = parent;
        this.closeCallback=onCloseCallback;
        this.setDefaultInputValue(displayItem.getName().getString());
        this.setLockPlayerInventory(true);
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (index==2) {
            this.close();
            return false;
        }
        return super.onAnyClick(index,type,action);
    }

    @Override
    public void onClose() {
        parent.open();
        closeCallback.accept(this.getInput());
    }

}
