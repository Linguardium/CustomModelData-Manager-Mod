package mod.linguardium.cmdm.gui.element;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.GuiInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemStackSet;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ItemHolderMenuOption extends GuiElement {
    ItemStack displayStack;
    boolean shouldShowHeldItem = false;
    public ItemHolderMenuOption(GuiElement element) {
        super(element.getItemStack(),element.getGuiCallback());
    }
    public ItemHolderMenuOption(ItemStack stack, ClickCallback onClick) {
        this (stack, true, onClick);

    }
    public ItemHolderMenuOption(ItemStack stack, boolean showHeldItem, ClickCallback onClick) {
        super(stack, onClick);
        this.shouldShowHeldItem=showHeldItem;
    }

    @Override
    public void setItemStack(ItemStack itemStack) {
        if (itemStack==null || itemStack.isEmpty()) {
            super.setItemStack(ItemStack.EMPTY);
        }else{
            super.setItemStack(itemStack);
        }
    }
    public ItemStack swapItemStack(ItemStack stack) {
        ItemStack lastStack = getItemStack();
        setItemStack(stack);
        return lastStack;
    }

    @Override
    public ItemStack getItemStackForDisplay(GuiInterface gui) {
        if (shouldShowHeldItem && !this.getItemStack().isEmpty()) {
            return this.getItemStack().copy();
        }
        return this.displayStack;
    }
    public void setDisplayStack(Item item, Text name, List<Text> lore) {
        GuiElementBuilder builder = new GuiElementBuilder(item);
        if (name != null && !textIsEmpty(name)) builder.setName(name);
        if (lore != null && !lore.isEmpty()) builder.setLore(lore);
        this.displayStack= builder.build().getItemStack();
    }
    public void setDisplayStack(ItemStack stack) {
        this.displayStack = stack.copy();
    }
    private boolean textIsEmpty(Text text) {
        return text.getContent().toString().equals("empty");
    }

}
