package mod.linguardium.cmdm.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class Helpers {
    public static final String CUSTOM_MODEL_DATA_NBT_KEY = "CustomModelData";
    public static final Identifier CUSTOM_MODEL_DATA_MODELPROVIDER_KEY = new Identifier("custom_model_data");
    public static int getCustomModelData(ItemStack stack) {
        if (stack.getNbt() != null && stack.getNbt().contains(CUSTOM_MODEL_DATA_NBT_KEY, NbtElement.INT_TYPE)) {
            return stack.getNbt().getInt(CUSTOM_MODEL_DATA_NBT_KEY);
        }
        return 0;
    }
    public static boolean deepCompareStacksAreEqual(ItemStack stack1, ItemStack stack2) {
        JsonElement nbt1;
        JsonElement nbt2;
        try {
            nbt1 = Util.getResult(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack1), IllegalStateException::new);
            nbt2 = Util.getResult(ItemStack.CODEC.encodeStart(JsonOps.INSTANCE, stack2), IllegalStateException::new);
            if (nbt1 == null || nbt2==null) return false;
            return (nbt1.toString().equals(nbt2.toString()));
        } catch (IllegalStateException e) {
            LOGGER.warn("Failed to convert itemstacks to json during deep compare");
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public static ItemStack setOrClearLore(ItemStack stack, List<Text> lore) {
        if (stack.isEmpty()) return stack;
        if (lore.size() > 4) lore = lore.subList(0,4);
        boolean newLore = !lore.isEmpty() && lore.stream().anyMatch(t->!t.getString().isBlank());
        boolean oldLore = hasLore(stack);
        if (!oldLore && !newLore) return stack;
        if (!newLore) { // oldLore Exists
            NbtCompound nbt = stack.getSubNbt("display");
            if (nbt != null) {
                nbt.remove("Lore");
                if (nbt.isEmpty()) {
                    stack.removeSubNbt("display");
                }
            }
        }else{ // new lore exists
            NbtList loreList = new NbtList();
            loreList.addAll(lore.stream().map(Text.Serializer::toJson).map(NbtString::of).toList());
            stack.getOrCreateSubNbt("display").put("Lore",loreList);
        }
        return stack;
    }
    public static boolean hasLore(ItemStack stack) {
        NbtCompound displayNbt = stack.getSubNbt("display");
        if (displayNbt != null) {
            NbtList loreList = displayNbt.getList("Lore",NbtElement.STRING_TYPE);
            if (loreList != null) {
                List<Text> loreTexts=new ArrayList<>();
                for (int i=0;i<loreList.size();i++) {
                    loreTexts.add(Text.Serializer.fromJson(loreList.getString(i)));
                }
                return loreTexts.size() > 0 && loreTexts.stream().anyMatch(t->!t.getString().isBlank());
            }
        }
        return false;
    }
    public static ItemStack findAndRemoveFromPlayer(ServerPlayerEntity player, Item target) {
        return findAndRemoveFromPlayer(player,s->s.isOf(target));
    }
    public static ItemStack findAndRemoveFromPlayer(ServerPlayerEntity player, Predicate<ItemStack> predicate) {
        for (int i = 0; i< PlayerInventory.getHotbarSize() + PlayerInventory.MAIN_SIZE; i++) {
            ItemStack playerStack = player.getInventory().getStack(i);
            if (predicate.test(playerStack)) {
                ItemStack returnedStack = playerStack.copy();
                playerStack.decrement(returnedStack.getCount());
                return returnedStack;
            }
        }
        return ItemStack.EMPTY;
    }
    @Contract(pure = true)
    public static ItemStack mergeItemStackProperties(ItemStack item1, ItemStack item2) {
        // Keep this method pure, only ever reference the reference copies, never the passed in stacks
        ItemStack receiver = item1.copy();
        ItemStack sender = item2.copy();
        NbtCompound receiverNbt = Optional.ofNullable(receiver.getNbt()).orElse(new NbtCompound());
        NbtCompound senderNbt = Optional.ofNullable(sender.getNbt()).orElse(new NbtCompound());

        // receiver base
        //GuiElementBuilder receiverBuilder = GuiElementBuilder.from(receiver);
        ItemStack merged = receiver.copy();

        // sender nbt base
        if (sender.hasNbt()) {
            merged.getOrCreateNbt().copyFrom(senderNbt);
        }

        // Merge enchantments
        if (receiver.hasEnchantments() || sender.hasEnchantments()) {
            merged.getOrCreateNbt().remove("Enchantments"); // remove enchantments to prepare to merge
            Map<Enchantment,Integer> enchants1 = EnchantmentHelper.get(receiver);
            Map<Enchantment,Integer> enchants2 = EnchantmentHelper.get(sender);

            enchants1.entrySet()
                    .stream()
                    .filter(e->EnchantmentHelper.isCompatible(enchants2.keySet(),e.getKey()))
                    .forEach(enchantEntry->
                            merged.addEnchantment(enchantEntry.getKey(),Math.max(enchantEntry.getValue(), enchants2.getOrDefault(enchantEntry.getKey(),0)))
                    );
        }
        // keep source damage. No free repairs!
        merged.setDamage(receiver.getDamage());

        // keep source count
        merged.setCount(receiver.getCount());

        // merge hide flags. Not sure the best way to handle this so just keep them all
        if (receiverNbt.contains("HideFlags",NbtElement.INT_TYPE) || senderNbt.contains("HideFlags",NbtElement.INT_TYPE)) {
            merged.getOrCreateNbt().putInt("HideFlags", receiverNbt.getInt("HideFlags") | senderNbt.getInt("HideFlags"));
        }

        // Delete nbt if the merged item doesnt have nbt. This doesnt make sense for the current usage but i dont like empty nbt preventing stacking
        if (merged.getNbt() != null && merged.getNbt().isEmpty()) {
            merged.setNbt(null);
        }
        return merged;
    }
    public static void giveOrDropStackAndClear(ServerPlayerEntity player, ItemStack stack, boolean toCursor) {
        ItemStack copyStack = stack.copy();
        stack.setCount(0);
        if (toCursor && player.currentScreenHandler.getCursorStack().isEmpty()) {
            player.currentScreenHandler.setCursorStack(copyStack);
        }else{
            player.getInventory().offerOrDrop(copyStack);
        }
    }


}
