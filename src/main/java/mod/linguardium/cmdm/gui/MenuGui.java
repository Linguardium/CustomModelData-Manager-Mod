package mod.linguardium.cmdm.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.linguardium.cmdm.gui.child.*;
import mod.linguardium.cmdm.gui.element.ItemHolderMenuOption;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static mod.linguardium.cmdm.util.Helpers.setOrClearLore;

public class MenuGui extends SimpleGui {
    private static final ItemStack VISUALS_ICON = new GuiElementBuilder().setItem(Items.ENDER_EYE).setName(Text.literal("Set Item Visuals")).asStack();
    private static final ItemStack RENAME_ICON = new GuiElementBuilder().setItem(Items.BOOK).setName(Text.literal("Set Item Name")).asStack();
    private static final ItemStack LORE_ICON = new GuiElementBuilder().setItem(Items.WRITABLE_BOOK).setName(Text.literal("Set Item Lore")).asStack();
    private static final ItemStack RESET_ICON = new GuiElementBuilder().setItem(Items.WATER_BUCKET).setName(Text.literal("Reset")).asStack();
    private static final ItemStack UNIQUE_ICON = new GuiElementBuilder().setItem(Items.ENDER_CHEST).setName(Text.literal("My Unique Items")).asStack();
    private static final ItemStack ADD_UNIQUE_ICON =  new GuiElementBuilder().setItem(Items.GLOWSTONE).setName(Text.literal("OP! Add Unique Item")).asStack();
    private boolean isLocked = false;

    ItemHolderMenuOption itemInputGuiElement = new ItemHolderMenuOption(ItemStack.EMPTY,true,this::handleInput);
    ItemHolderMenuOption itemOutputGuiElement = new ItemHolderMenuOption(ItemStack.EMPTY,true,this::handleOutput);
    public boolean fullClose = false;
    private <T> GuiElementInterface.ClickCallback handleOpenerClick(boolean lockable, ChildGuiFactory<T> factory, Consumer<T> onCloseCallback) {
        return (index, type, action, gui) -> {
            if ((!lockable || !isLocked) && type.isLeft) {
                fullClose = false;
                factory.create(MenuGui.this, player, itemOutputGuiElement.getItemStack().copyWithCount(1), (obj) -> {
                    onCloseCallback.accept(obj);
                    fullClose = true;
                }).open();
            }
        };
    }
    private <T> GuiElementInterface.ClickCallback handleActionClick(boolean lockable, Runnable action) {
        return (index, type, action1, gui) -> {
            if ((!lockable || !isLocked) && type.isLeft) {
                    action.run();
            }
        };
    }
    private void checkInput() {
        if (!itemOutputGuiElement.getItemStack().isOf(itemInputGuiElement.getItemStack().getItem())) {
            itemOutputGuiElement.setItemStack(itemInputGuiElement.getItemStack().copy());
        }
        LockMenu(itemInputGuiElement.getItemStack().isEmpty());
    }
    private void handleInput(int index, ClickType type, SlotActionType action, SlotGuiInterface gui) {
        if (type.isLeft) {
            // Swap item in cursor with held item
            player.currentScreenHandler.setCursorStack(
                    itemInputGuiElement.swapItemStack(player.currentScreenHandler.getCursorStack())
            );
        }
        checkInput();
    }
    private void handleOutput(int index, ClickType type, SlotActionType action, SlotGuiInterface gui) {
        if (!player.currentScreenHandler.getCursorStack().isEmpty()) return;
        if (type.isLeft) {
            switch(action) {
                case PICKUP:
                case PICKUP_ALL:
                    player.currentScreenHandler.setCursorStack(itemOutputGuiElement.swapItemStack(ItemStack.EMPTY));
                    itemInputGuiElement.setItemStack(ItemStack.EMPTY);
                    break;
                case QUICK_MOVE:
                    if (player.getInventory().insertStack(itemOutputGuiElement.getItemStack())) {
                        itemOutputGuiElement.setItemStack(ItemStack.EMPTY);
                        itemInputGuiElement.setItemStack(ItemStack.EMPTY);
                    }
                    break;
                case THROW:
                    break;

            }
        }else if(action.equals(SlotActionType.THROW)) {
            int count = itemOutputGuiElement.getItemStack().getCount();
            if (type.equals(ClickType.DROP)) {
                count=1;
            }
            ItemStack stack = itemOutputGuiElement.getItemStack().copyWithCount(count);
            player.dropItem(stack,true);
            itemInputGuiElement.getItemStack().decrement(count);
            itemOutputGuiElement.getItemStack().decrement(count);
        }
        checkInput();
    }
    private void setCustomName(String name) {
        ItemStack outputStack = itemOutputGuiElement.getItemStack();
        if (outputStack.isEmpty()) return;
        if (name.isBlank() && outputStack.hasCustomName()) {
            outputStack.setCustomName(null);
        }else if (!name.isBlank() && !outputStack.getName().getString().equals(name)) {
            outputStack.setCustomName(Text.literal(name));
        }
    }
    private void setCustomLore(List<Text> lore) {
        ItemStack outputStack = itemOutputGuiElement.getItemStack();
        if (!outputStack.isEmpty() ) {
            setOrClearLore(outputStack,lore);
        }
    }
    private void setCMD(ItemStack item) {
        if (item == null || item.isEmpty()) return;
        int i = (item.hasNbt() && item.getOrCreateNbt().contains("CustomModelData", NbtElement.INT_TYPE))?item.getOrCreateNbt().getInt("CustomModelData"):0;
        if (i < 0) return;
        ItemStack outputStack = itemOutputGuiElement.getItemStack();
        if (!outputStack.isEmpty()) {
            itemOutputGuiElement.setItemStack(GuiElementBuilder.from(outputStack).setCustomModelData(i).asStack());
        }
    }
    private void resetOutput() {
        itemOutputGuiElement.setItemStack(itemInputGuiElement.getItemStack().copy());
    }
    private boolean hasInput() {
        return !itemInputGuiElement.getItemStack().isEmpty();
    }
    private void LockMenu(boolean lock) {
        this.isLocked=lock;
        if (lock) {
            for (int i = 2;i<6;i++) {
                ((GuiElement)this.getSlot(i)).setItemStack(
                        new GuiElementBuilder(Items.BARRIER).setName(Text.literal("Insert Item to modify")).asStack()
                );
            }
        }else{
            ((GuiElement)this.getSlot(2)).setItemStack(VISUALS_ICON);
            ((GuiElement)this.getSlot(3)).setItemStack(RENAME_ICON);
            ((GuiElement)this.getSlot(4)).setItemStack(LORE_ICON);
            ((GuiElement)this.getSlot(5)).setItemStack(RESET_ICON);
        }
    }

    @Contract(pure = true)
    private ItemStack merge(ItemStack item1, ItemStack item2) {
        // Keep this method pure, only ever reference the reference copies, never the passed in stacks
        ItemStack receiver = item1.copy();
        ItemStack sender = item2.copy();
        NbtCompound receiverNbt = Optional.ofNullable(receiver.getNbt()).orElse(new NbtCompound());
        NbtCompound senderNbt = Optional.ofNullable(sender.getNbt()).orElse(new NbtCompound());

        // receiver base
        GuiElementBuilder receiverBuilder = GuiElementBuilder.from(receiver);

        // sender nbt base
        if (sender.hasNbt()) {
            receiverBuilder.getOrCreateNbt().copyFrom(senderNbt);
        }

        // Merge enchantments
        if (receiver.hasEnchantments() || sender.hasEnchantments()) {
            receiverBuilder.getOrCreateNbt().remove("Enchantments"); // remove enchantments to prepare to merge
            Map<Enchantment,Integer> enchants1 = EnchantmentHelper.get(receiver);
            Map<Enchantment,Integer> enchants2 = EnchantmentHelper.get(sender);

            enchants1.entrySet()
                    .stream()
                    .filter(e->EnchantmentHelper.isCompatible(enchants2.keySet(),e.getKey()))
                    .forEach(enchantEntry->
                            receiverBuilder.enchant(enchantEntry.getKey(),Math.max(enchantEntry.getValue(), EnchantmentHelper.getLevel(enchantEntry.getKey(), sender)))
                    );
        }
         // keep source damage. No free repairs!
        receiverBuilder.setDamage(receiver.getDamage());

        // merge hide flags. Not sure the best way to handle this so just keep them all
        if (receiverNbt.contains("HideFlags",NbtElement.INT_TYPE) || senderNbt.contains("HideFlags",NbtElement.INT_TYPE)) {
            receiverBuilder.getOrCreateNbt().putInt("HideFlags", receiverNbt.getInt("HideFlags") | senderNbt.getInt("HideFlags"));
        }
        ItemStack mergedStack =receiverBuilder.asStack();

        // Delete nbt if the merged item doesnt have nbt. This doesnt make sense for the current usage but i dont like empty nbt preventing stacking
        if (mergedStack.getNbt() != null && mergedStack.getNbt().isEmpty()) {
            mergedStack.setNbt(null);
        }
        return mergedStack;
    }
    private ItemStack findAndRemoveFromPlayer(Item target) {
        for (int i = 0; i< PlayerInventory.getHotbarSize() + PlayerInventory.MAIN_SIZE;i++) {
            ItemStack playerStack = player.getInventory().getStack(i);
            if (playerStack.isOf(target)) {
                ItemStack returnedStack = playerStack.copy();
                playerStack.decrement(returnedStack.getCount());
                return returnedStack;
            }
        }
        return ItemStack.EMPTY;
    }
    private void setUniqueItemStack(ItemStack stack) {
        if (stack==null || stack.isEmpty()) return;
        if (!itemInputGuiElement.getItemStack().isEmpty() && !stack.isOf(itemInputGuiElement.getItemStack().getItem())) {
            removeItemInput();
        }
        if (itemInputGuiElement.getItemStack().isEmpty()) {
                itemInputGuiElement.setItemStack(findAndRemoveFromPlayer(stack.getItem()));
        }
        if (itemInputGuiElement.getItemStack().isOf(stack.getItem())) {
                itemOutputGuiElement.setItemStack(merge(itemInputGuiElement.getItemStack(),stack));
        }
        checkInput();
    }
    public MenuGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X1, player, false);
        itemInputGuiElement.setDisplayStack(Items.ITEM_FRAME,Text.literal("Item Input"), Collections.emptyList());
        itemOutputGuiElement.setDisplayStack(Items.ITEM_FRAME,Text.literal("Item Output"), Collections.emptyList());

        this.setSlot(0,itemInputGuiElement);
        this.setSlot(2,GuiElementBuilder.from(VISUALS_ICON).setCallback(handleOpenerClick(true, VisualsGui::new, this::setCMD)).build());
        this.setSlot(3,GuiElementBuilder.from(RENAME_ICON).setCallback(handleOpenerClick(true, RenameGui::new, this::setCustomName)).build());
        this.setSlot(4,GuiElementBuilder.from(LORE_ICON).setCallback(handleOpenerClick(true, LoreGui::new, this::setCustomLore)).build());
        this.setSlot(5,GuiElementBuilder.from(RESET_ICON).setCallback(handleActionClick(true,this::resetOutput)).build());
        if (player.getServer().isSingleplayer() || player.getServer().getPermissionLevel(player.getGameProfile()) >= 2) {
            this.setSlot(6,GuiElementBuilder.from(ADD_UNIQUE_ICON).setCallback(handleOpenerClick(false,AddUniqueGui::new,(click)->{})));
        }
        this.setSlot(7,GuiElementBuilder.from(UNIQUE_ICON).setCallback(handleOpenerClick(false, UniqueGui::new, this::setUniqueItemStack)).build());
        this.setSlot(8,itemOutputGuiElement);
        LockMenu(true);

    }

    @Override
    public void onClose() {
        if (fullClose) {
            removeItemInput();
        }
        super.onClose();
    }
    private boolean removeItemInput() {
        if (!player.giveItemStack(itemInputGuiElement.getItemStack())) {
            return player.dropItem(itemInputGuiElement.getItemStack(), true)!=null;
        }else{
            return true;
        }
    }
}
