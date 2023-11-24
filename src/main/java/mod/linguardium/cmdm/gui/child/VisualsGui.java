package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.linguardium.cmdm.Config;
import mod.linguardium.cmdm.util.Helpers;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VisualsGui extends ItemListGui {

    public VisualsGui(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<ItemStack> onCloseCallback) {
        super(parent, player, displayItem, onCloseCallback);
    }

    @Override
    protected void initializeItemList() {
        Identifier id = Registries.ITEM.getId(displayItem.getItem());
        List<Integer> list = new ArrayList<>();
        if (Helpers.getCustomModelData(displayItem) != 0) {
            list.add(0);
        }
        list.addAll(Config.getInstance().getEntries().getOrDefault(id,new ArrayList<>()));
        setItemList(
                list.stream().map(i->
                        GuiElementBuilder.from(displayItem.copy()).setCustomModelData(i).asStack()
                ).toList());
    }
}
