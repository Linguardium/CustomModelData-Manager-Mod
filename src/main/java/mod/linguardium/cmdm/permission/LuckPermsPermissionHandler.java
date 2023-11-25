package mod.linguardium.cmdm.permission;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.network.ServerPlayerEntity;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class LuckPermsPermissionHandler implements PermissionHandler {

    public LuckPermsPermissionHandler() {
        LOGGER.info("Using LuckPerms permissions");
        LOGGER.info("{} provides item builder permissions",PermissionHandler.ITEM_BUILDER_NODE);
        LOGGER.info("{}} provides global unique list modification",PermissionHandler.UNIQUE_ADDER_NODE);
        LOGGER.info("{}} provides server config reload permissions",PermissionHandler.CONFIG_RELOAD_NODE);
        LOGGER.info("{}} provides CMD texture list regeneration permissions",PermissionHandler.CONFIG_GENERATE_NODE);
    }
    public boolean hasPermission(ServerPlayerEntity player, String string) {
        return LuckPermsProvider.get().getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player).checkPermission(string).asBoolean();
    }

    @Override
    public void init() {
    }
}
