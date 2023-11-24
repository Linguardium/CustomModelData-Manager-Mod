package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.linguardium.cmdm.Config;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class UniqueGui extends ItemListGui {
    public UniqueGui(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<ItemStack> onCloseCallback) {
        super(parent, player, displayItem, onCloseCallback);
    }

    @Override
    protected void initializeItemList() {
        List<ItemStack> uniqueList = new ArrayList<>();
        uniqueList.addAll(Config.getInstance().getUniqueItems().getOrDefault(Util.NIL_UUID,new ArrayList<>()));
        uniqueList.addAll(Config.getInstance().getUniqueItems().getOrDefault(player.getUuid(),new ArrayList<>() ));
        setItemList(uniqueList);
    }
}
