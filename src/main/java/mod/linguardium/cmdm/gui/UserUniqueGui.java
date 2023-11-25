package mod.linguardium.cmdm.gui;

import eu.pb4.sgui.api.ClickType;
import mod.linguardium.cmdm.Config;
import mod.linguardium.cmdm.gui.child.ItemListGui;
import mod.linguardium.cmdm.gui.element.ItemHolderMenuOption;
import mod.linguardium.cmdm.util.Helpers;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mod.linguardium.cmdm.util.Helpers.*;

public class UserUniqueGui extends ItemListGui {
    protected final ItemHolderMenuOption itemInputGuiElement = new ItemHolderMenuOption(ItemStack.EMPTY,true,this::handleInput);
    List<ItemStack> uniqueList;
    public UserUniqueGui(ServerPlayerEntity player) {
        super(null, player, ItemStack.EMPTY, (c)->{});
        itemInputGuiElement.setDisplayStack(Items.ITEM_FRAME,Text.literal("Item Input"), Collections.emptyList());
        this.setSlot(9*5+4,itemInputGuiElement);
    }
    @Override
    protected void handleSelect(int index, ClickType type, SlotActionType action) {
        if (!type.isLeft) return;
        ItemStack stack = this.getSlot(index).getItemStack();
        boolean refreshItemList = false;
        ItemStack donor = ItemStack.EMPTY;
        if (itemInputGuiElement.getItemStack().isEmpty()) {
            ItemStack input = findAndRemoveFromPlayer(player,stack.getItem());
            if (!input.isEmpty()) {
                itemInputGuiElement.setItemStack(input);
                refreshItemList=true;
            }else{
                player.sendMessage(Text.literal("Cannot find input item in player inventory").formatted(Formatting.RED),true);
            }
        }else if(stack.isOf(itemInputGuiElement.getItemStack().getItem())) {
            donor = itemInputGuiElement.getItemStack();
        }else if(player.currentScreenHandler.getCursorStack().isOf(stack.getItem())) {
            donor=player.currentScreenHandler.getCursorStack();
        }else{
            // This shouldnt happen
            player.sendMessage(Text.literal("Invalid item input!").formatted(Formatting.RED),true);
        }
        if (!donor.isEmpty()) {
            ItemStack merged = mergeItemStackProperties(donor, stack);
            if (action.equals(SlotActionType.QUICK_MOVE)) {
                if (player.getInventory().insertStack(merged)) {
                    donor.setCount(0);
                    if (itemInputGuiElement.getItemStack().isEmpty()) {
                        ItemStack input = findAndRemoveFromPlayer(player, stack.getItem());
                        if (!input.isEmpty()) {
                            itemInputGuiElement.setItemStack(input);
                        }
                    }
                }
            }else{
                donor.decrement(merged.getCount());
                Helpers.giveOrDropStackAndClear(player,merged,true);
            }
            refreshItemList = true;
        }
        if (refreshItemList) { checkInput();}
    }
    private void handleInput(int index, ClickType type, SlotActionType action) {
        if (type.isLeft) {
            // Swap item in cursor with held item
            player.currentScreenHandler.setCursorStack(
                    itemInputGuiElement.swapItemStack(player.currentScreenHandler.getCursorStack())
            );
        }
        checkInput();
    }
    private void checkInput() {
        if (itemInputGuiElement.getItemStack().isEmpty()) {
            setItemList(uniqueList);
        }else{
            setItemList(uniqueList.stream().filter(stack->stack.isOf(itemInputGuiElement.getItemStack().getItem())).toList());
        }
    }

    @Override
    protected void initializeItemList() {
        uniqueList=new ArrayList<>();
        uniqueList.addAll(Config.getInstance().getUniqueItems().getOrDefault(Util.NIL_UUID,new ArrayList<>()));
        uniqueList.addAll(Config.getInstance().getUniqueItems().getOrDefault(player.getUuid(),new ArrayList<>() ));
        setItemList(uniqueList);
    }

    @Override
    public void onClose() {
        giveOrDropStackAndClear(player, itemInputGuiElement.getItemStack(), false);
        giveOrDropStackAndClear(player,player.currentScreenHandler.getCursorStack(), false);
    }
}
