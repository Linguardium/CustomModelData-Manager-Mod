package mod.linguardium.cmdm;


import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.MerchantGui;
import eu.pb4.sgui.virtual.SguiScreenHandlerFactory;
import eu.pb4.sgui.virtual.merchant.VirtualMerchantScreenHandler;
import eu.pb4.sgui.virtual.merchant.VirtualTradeOutputSlot;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import static mod.linguardium.cmdm.CustomModelDataManager.CustomModelDataOptions;


public class CMDSelector extends MerchantGui {
    private static int slotCount = 54;
    private int selectedSlot = -1;
    private boolean traded = false;
    private static final String CUSTOM_MODEL_DATA_KEY = "CustomModelData";
    private boolean refresh = true;

    @Override
    public void onTick() {
        super.onTick();
        if (traded) { traded = false; }
        if (refresh) {
            this.loadCustomModelDataTrades(this.getSlotRedirect(0).getStack());
            refresh=false;
        }

    }

    public CMDSelector(ServerPlayerEntity player) {
        super(player,false);
        setAutoUpdate(true);
        this.setTitle(usesLevelCost()?Text.translatable("container.enchant.level.requirement",CustomModelDataOptions.cost().levels().toString()):Text.translatable("container.crafting"));
        this.setSlotRedirect(0, new Slot(this.merchantInventory, 0, 0, 0) {
            @Override
            public void markDirty() {
                refresh=true;
                super.markDirty();
            }
        });
        this.setSlotRedirect(1, new Slot(this.merchantInventory, 1, 0, 0) {

            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }

            @Override
            public boolean canTakeItems(PlayerEntity playerEntity) {
                return false;
            }

            @Override
            public boolean canTakePartial(PlayerEntity player) {
                return false;
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
    }


    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        return super.onAnyClick(index,type,action);
    }

    public void updateSelectedItem(int index) {
        this.selectedSlot=index;
    }

    @Contract(pure = true)
    public void loadCustomModelDataTrades(ItemStack stack) {
        if (usesLevelCost() && !hasEnoughLevels()) {
            stack = ItemStack.EMPTY;
        }
        TradeOfferList list = merchant.getOffers();
        Item currentOfferItem = list.size() >= 7 ? list.get(0).getOriginalFirstBuyItem().getItem():Items.AIR;
        if (currentOfferItem.equals(Items.BARRIER)) currentOfferItem=Items.AIR;
        if (list.size() >= 7 && stack.isOf(currentOfferItem)) {

        }else {
            this.merchant.setOffersFromServer(new TradeOfferList());
            if (stack != null && !stack.isEmpty()) {
                this.addTrade(CMDTrade(stack, 0));
                for (int i : CustomModelDataOptions.entries().getOrDefault(Registries.ITEM.getId(stack.getItem()), Collections.emptyList())) {
                    this.addTrade(CMDTrade(stack, i));
                }
            }
            while (this.merchant.getOffers().size() < 7) {
                this.addTrade(CMDTrade(new ItemStack(Items.BARRIER), 0));
            }
        }
        if (list.size() >= 7 && this.merchant.getOffers().size() < list.size()) {
            this.sendGui();
        } else {
            sendUpdate();
        }
    }

    @Contract(pure = true)
    TradeOffer CMDTrade(ItemStack referenceStack, Integer iCMD) {
        ItemStack stack = referenceStack.copyWithCount(1);
        if (iCMD>0) {
            stack.getOrCreateNbt().putInt(CUSTOM_MODEL_DATA_KEY,iCMD);
        }else if(stack.getNbt() != null) {
            stack.getNbt().remove(CUSTOM_MODEL_DATA_KEY);
        }
        if (stack.getNbt() != null && stack.getNbt().isEmpty()) { stack.setNbt(null); }
        return new TradeOffer(referenceStack.copyWithCount(1), (!usesLevelCost())? new ItemStack(Registries.ITEM.get(CustomModelDataOptions.cost().item()),CustomModelDataOptions.cost().count()):ItemStack.EMPTY, stack,Integer.MAX_VALUE,0,1.0f);
    }

    @Override
    public TradeOffer getSelectedTrade() {
        return null;
    }

    public boolean onTrade(TradeOffer offer) {
        if (usesLevelCost() && !traded) {
            if (hasEnoughLevels()) {
                this.player.addExperienceLevels(-CustomModelDataOptions.cost().levels());
                traded=true;
            }else{
                return false;
            }
        }
        refresh=true;
        return !usesLevelCost() || hasEnoughLevels();
    }
    private boolean usesLevelCost() {
        return usesLevelCost(CustomModelDataOptions.cost());
    }
    private boolean usesLevelCost(Config.Cost cost) {
        return cost.count()==0 && cost.levels() > 0;

    }
    private boolean hasEnoughLevels() {
        return this.player.experienceLevel >= CustomModelDataOptions.cost().levels();
    }
}
