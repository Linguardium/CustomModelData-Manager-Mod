package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ItemListGui extends SimpleGui {
    protected final SlotGuiInterface parent;
    protected final Consumer<ItemStack> closeCallback;
    protected List<ItemStack> stackList = new ArrayList<>();
    protected final ItemStack displayItem;
    protected int currentLine = 0;
    protected ItemStack selected=ItemStack.EMPTY;

    public ItemListGui(@Nullable SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<ItemStack> onCloseCallback) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.closeCallback=onCloseCallback;
        this.parent=parent;
        this.displayItem=displayItem;
        for (int i=0;i<9*4;i++) {
            this.setSlot(i,new GuiElementBuilder(Items.AIR).setCallback(this::handleSelect).build());
        }
        for (int i=9*4;i<9*5;i++) {
            this.setSlot(i,new GuiElementBuilder(Items.RED_STAINED_GLASS_PANE).build());
        }
        this.setSlot(9*5,new GuiElementBuilder(Items.ARROW).setName(Text.literal("Scroll Down")).setLore(List.of(Text.literal("Right click to scroll entire page"))) .setCallback(this::handleNext));
        this.setSlot((9*5)+1,new GuiElementBuilder(Items.ARROW).setName(Text.literal("Scroll Up")).setLore(List.of(Text.literal("Right click to scroll entire page"))) .setCallback(this::handlePrev));
        initializeItemList();
        setSlots(0);
    }
    protected abstract void initializeItemList();

    public void setItemList(List<ItemStack> list) {
        stackList = list;
        setSlots(0);
    }
    protected void handlePrev(ClickType click) {
        int newLine = currentLine;
        if (click.isLeft) {
            newLine--;
        }else if(click.isRight) {
            newLine-=4;
        }
        if (newLine < 0) newLine=0;
        if (this.currentLine != newLine) setSlots(newLine);
    }
    protected void handleNext(ClickType click) {
        int max = (int)Math.ceil(stackList.size()/9.0d)-1;
        int newLine = currentLine;
        if (click.isLeft) {
            newLine += 1;
        } else if (click.isRight) {
            newLine += 4;
        }
        if (newLine > max) newLine=max;
        if (newLine != currentLine) setSlots(newLine);
    }
    protected void handleSelect(int index, ClickType type, SlotActionType action) {
        selected = this.getSlot(index).getItemStack();
        this.close();
    }
    protected void setSlots(int startLine) {
        currentLine = startLine;
        for (int i=0;i<9*4;i++) {
            GuiElement slot = (GuiElement)this.getSlot(i);
            if ((currentLine*9)+i < stackList.size()) {
                slot.setItemStack(stackList.get((currentLine*9)+i));
            }else{
                slot.setItemStack(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void onClose() {
        if (parent!=null) {
            parent.open();
        }
        closeCallback.accept(selected);
    }
}
