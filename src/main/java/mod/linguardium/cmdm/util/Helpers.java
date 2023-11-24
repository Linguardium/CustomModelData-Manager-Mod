package mod.linguardium.cmdm.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.data.DataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.List;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class Helpers {
    public static final String CUSTOM_MODEL_DATA_NBT_KEY = "CustomModelData";
    public static final Identifier CUSTOM_MODEL_DATA_MODELPROVIDER_KEY = new Identifier("custom_model_data");
    public static int getCustomModelData(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains(CUSTOM_MODEL_DATA_NBT_KEY, NbtElement.INT_TYPE)) {
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
//            LOGGER.info(nbt2.asString());
//            LOGGER.info(nbt1.asString());
            return (nbt1.toString().equals(nbt2.toString()));
        } catch (IllegalStateException e) {
            LOGGER.warn("Failed to convert itemstacks to nbt during deep compare");
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public static ItemStack setOrClearLore(ItemStack stack, List<Text> lore) {
        if (lore.size() > 4) lore = lore.subList(0,4);
        boolean newLore = !lore.isEmpty() && lore.stream().anyMatch(t->!t.getString().isBlank());
        boolean oldLore = stack.hasNbt() && stack.getNbt().contains("display") && stack.getSubNbt("display").contains("Lore");
        if (!oldLore && !newLore) return stack;
        if (!newLore && oldLore) {
            stack.getSubNbt("display").remove("Lore");
            if (stack.getSubNbt("display").isEmpty()) {
                stack.removeSubNbt("display");
            }
        }else{
            NbtList loreList = new NbtList();
            loreList.addAll(lore.stream().map(Text.Serializer::toJson).map(NbtString::of).toList());
            stack.getOrCreateSubNbt("display").put("Lore",loreList);
        }
        return stack;
    }
}
