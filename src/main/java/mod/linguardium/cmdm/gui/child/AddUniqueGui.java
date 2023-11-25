package mod.linguardium.cmdm.gui.child;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import mod.linguardium.cmdm.Config;
import mod.linguardium.cmdm.util.Helpers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class AddUniqueGui extends ItemListGui{
    private UUID selectedPlayer = null;
    private static final UUID ALL_ONES = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public AddUniqueGui(SlotGuiInterface parent, ServerPlayerEntity player, ItemStack displayItem, Consumer<ItemStack> onCloseCallback) {
        super(parent, player, displayItem, onCloseCallback);
        this.setSlot(9*5+2,new GuiElementBuilder(Items.PLAYER_HEAD).setName(Text.literal("To Player List")).setCallback(c->{
            selectedPlayer=null;
            initializeItemList();
            setSlots(0);
        }).build());
    }

    @Override
    protected void handleSelect(int index, ClickType type, SlotActionType action) {
        if (selectedPlayer == null) {
            if (this.getSlot(index).getItemStack().isEmpty()) return;
            if (type.isLeft) {
                selectedPlayer = this.getSlot(index).getItemStack().getOrCreateNbt().getUuid("uuid");
                this.initializeItemList();
                this.setSlots(0);
            }
        }else if (type.isLeft && !player.currentScreenHandler.getCursorStack().isEmpty()) {
            List<ItemStack> list = Config.getInstance().getUniqueItems().computeIfAbsent(selectedPlayer, u->new ArrayList<>());
            ItemStack cursorStack = player.currentScreenHandler.getCursorStack().copy();

            // this is expensive but it should only happen when dropping stuff into the window so its fine i guess
            if (list.stream().noneMatch(stack->Helpers.deepCompareStacksAreEqual(cursorStack,stack))) {
                list.add(cursorStack);
            }

            this.initializeItemList();
            setSlots(0);
            this.selected=ItemStack.EMPTY;
            player.getInventory().insertStack(cursorStack.copy());
            player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
            try {
                Config.getInstance().save();
            } catch (IOException e) {
                LOGGER.warn("Failed to save config");
                LOGGER.warn(e.getMessage());
                e.printStackTrace();
            }

        } else if (type.isRight) {
            List<ItemStack> list = Config.getInstance().getUniqueItems().computeIfAbsent(selectedPlayer, u->new ArrayList<>());
            list.remove(this.getSlot(index).getItemStack());

            this.initializeItemList();
            setSlots(0);
            try {
                Config.getInstance().save();
            } catch (IOException e) {
                LOGGER.warn("Failed to save config");
                LOGGER.warn(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void initializeItemList() {
        if (selectedPlayer==null) {
            List<ItemStack> list = new ArrayList<>();
            list.add(setUUID(new GuiElementBuilder(Items.SKELETON_SKULL).setName(Text.literal("Everybody")).setLore(List.of(Text.literal(Util.NIL_UUID.toString()))),Util.NIL_UUID).asStack());
            list.add(setUUID(new GuiElementBuilder(Items.WITHER_SKELETON_SKULL).setName(Text.literal("Nobody")).setLore(List.of(Text.literal(ALL_ONES.toString()))),ALL_ONES).asStack());
            List<UUID> playerList = new ArrayList<>(Objects.requireNonNull(player.getServer()).getPlayerManager().getPlayerList().stream().map(Entity::getUuid).toList());
            playerList.addAll(Config.getInstance().getUniqueItems().keySet());
            playerList.remove(Util.NIL_UUID);
            playerList.remove(ALL_ONES);
            playerList.stream().distinct().forEach(k->
                list.add(setUUID
                        (new GuiElementBuilder(Items.PLAYER_HEAD)
                            .setName(nameOrUUID(k))
                            .setLore(List.of(Text.literal(k.toString())))
                        ,k)
                        .setSkullOwner(new GameProfile(k,""),player.getServer())
                        .asStack()
                    )
            );
            setItemList(list);
            this.setTitle(Text.literal("Select Player"));
        }else {
            this.setTitle(nameOrUUID(selectedPlayer));
            setItemList(new ArrayList<>(Config.getInstance().getUniqueItems().getOrDefault(selectedPlayer, new ArrayList<>())));
        }
    }
    private GuiElementBuilder setUUID(GuiElementBuilder builder, UUID uuid) {
        builder.getOrCreateNbt().putUuid("uuid",uuid);
        return builder;
    }
    private Text nameOrUUID(UUID uuid) {
        return Optional.ofNullable(player.getServer())
                .map(MinecraftServer::getPlayerManager)
                .map(m->m.getPlayer(uuid))
                .map(PlayerEntity::getName)
                .orElse(Text.literal(uuid.toString()));
    }
}
