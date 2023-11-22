package mod.linguardium.cmdm;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.advancement.AdvancementTab;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemCMDEditScreen extends Screen {
    private static final Identifier WINDOW_TEXTURE = new Identifier("textures/gui/advancements/window.png");
    public static final int WINDOW_WIDTH = 252;
    public static final int WINDOW_HEIGHT = 140;
    private static final int PAGE_OFFSET_X = 9;
    private static final int PAGE_OFFSET_Y = 18;
    public static final int PAGE_WIDTH = 234;
    public static final int PAGE_HEIGHT = 113;
    private static final int TITLE_OFFSET_X = 8;
    private static final int TITLE_OFFSET_Y = 6;
    public static final int field_32302 = 16;
    public static final int field_32303 = 16;
    public static final int field_32304 = 14;
    public static final int field_32305 = 7;
    private static final double field_45431 = 16.0;
    private static final Text SAD_LABEL_TEXT = Text.translatable("advancements.sad_label");
    private static final Text EMPTY_TEXT = Text.translatable("advancements.empty");
    private static final Text ADVANCEMENTS_TEXT = Text.translatable("gui.advancements");

    protected ItemCMDEditScreen(Text title) {
        super(title);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int i = (this.width - 252) / 2;
        int j = (this.height - 140) / 2;
        this.renderBackground(context, mouseX, mouseY, delta);
        //this.drawAdvancementTree(context, mouseX, mouseY, i, j);
        this.drawWindow(context, i, j);
        //this.drawWidgetTooltip(context, mouseX, mouseY, i, j);
    }
    public void drawWindow(DrawContext context, int x, int y) {
        RenderSystem.enableBlend();
        context.drawTexture(WINDOW_TEXTURE, x, y, 0, 0, 252, 140);
//        if (this.tabs.size() > 1) {
//            for(AdvancementTab advancementTab : this.tabs.values()) {
//                advancementTab.drawBackground(context, x, y, advancementTab == this.selectedTab);
//            }
//
//            for(AdvancementTab advancementTab : this.tabs.values()) {
//                advancementTab.drawIcon(context, x, y);
//            }
//        }

        context.drawText(this.textRenderer, this.title, x + 8, y + 6, 4210752, false);
    }

}
