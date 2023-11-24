package mod.linguardium.cmdm.gui.child;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LoreGui extends SignGui {
    protected final SlotGuiInterface parent;
    protected Consumer<List<Text>> closeCallback;
    private static final Style LORE_STYLE = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(true);
    /**
     * Constructs a new SignGui for the provided player
     *
     * @param player the player to serve this gui to
     */

    public LoreGui(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<List<Text>> onCloseCallback) {
        super(player);
        this.parent=parent;
        this.closeCallback=onCloseCallback;
        List<Text> currentLore =GuiElementBuilder.getLore(displayItem);
        for (int i=0;i<currentLore.size() && i<4;i++) {
            this.setLine(i,currentLore.get(i));
        }
    }

    @Override
    public void onClose() {
        parent.open();
        List<Text> lore = new ArrayList<>();
        for (int i=0;i<4;i++) {
            String line = getLine(i).getString();
            Text tLine;
            if (!line.isBlank()) {
                tLine = Text.literal(line.replace('ÿ', '§')).setStyle(LORE_STYLE);
                lore.add(tLine);
            }
//            if (!line.isBlank() && !line.contains("ÿ")) { tLine = tLine.copy().setStyle(ItemStackAccessor.getLoreStyle()); }
        }
        closeCallback.accept(lore);
    }
}
