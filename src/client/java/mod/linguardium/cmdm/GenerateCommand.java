package mod.linguardium.cmdm;

import mod.linguardium.cmdm.client.model.ModelOverrideListAdditions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static mod.linguardium.cmdm.C2SPackets.*;
import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;
import static mod.linguardium.cmdm.util.Helpers.CUSTOM_MODEL_DATA_MODELPROVIDER_KEY;

public class GenerateCommand  {

    public static void generateConfigForConnectedServer() {
        MinecraftClient.getInstance().reloadResources().thenRun(GenerateCommand::afterResourceReload);
    }

    private static void afterResourceReload() {
        sendInitializeListsPacket();
        Registries.ITEM.getEntrySet().forEach(itemEntry-> {
            List<Integer> CMDlist = getModelOverrideListCMDs(itemEntry.getValue());
            if (!CMDlist.isEmpty()) {
                sendItemList(itemEntry.getKey().getValue(),CMDlist);
                LOGGER.info("Completed "+itemEntry.getKey().getValue().toString());
            }
        });
        sendFinalizeListsPacket();
    }
    private static List<Integer> getModelOverrideListCMDs(Item item) {
                // Optional means that we dont have to check for nulls, just return an empty list if empty optional
                // This method returns the ModelOverrideList casted to our interface with the method we need below
        return  getItemModelOverridesOptional(item)
                        // see mixin: mod.linguardium.cmdm.mixin.client.ModelOverrideListMixin.cmdm$getIdentifiedConditionThresholds
                        // collects all conditions in the model, and maps them into Identifier keyed lists
                .map(ModelOverrideListAdditions::cmdm$getIdentifiedConditionThresholds)
                .map(conditionMap->
                        // only get CMD conditions
                        conditionMap.getOrDefault(CUSTOM_MODEL_DATA_MODELPROVIDER_KEY,Collections.emptyList())
                        .stream()
                        // conditions are all float values. CMD is an int so map to int
                        .map(Float::intValue)
                        // shouldnt happen since thresholds but remove duplicates just in case i guess
                        .distinct()
                        .toList()
                )
                .orElse(Collections.emptyList());
    }
    private static Optional<ModelOverrideListAdditions> getItemModelOverridesOptional(Item item) {
        // Optional of nullable lets us chain from this call instead of assigning and null checking
        return Optional.ofNullable(
                        MinecraftClient
                        .getInstance()
                        .getItemRenderer()
                        .getModels()
                        .getModel(item)) // end of optional creation, now we map to the value we really want
                .map(model->(ModelOverrideListAdditions)model.getOverrides());
    }

}
