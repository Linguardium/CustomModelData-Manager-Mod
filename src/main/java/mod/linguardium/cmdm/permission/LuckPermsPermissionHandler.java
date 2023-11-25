package mod.linguardium.cmdm.permission;

import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.network.ServerPlayerEntity;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class LuckPermsPermissionHandler implements PermissionHandler {

    public LuckPermsPermissionHandler() {
        LOGGER.info("Using LuckPerms permissions");
        LOGGER.info("cmdm.builder provides item builder permissions");
        LOGGER.info("cmdm.uniques provides global unique list modification");
        LOGGER.info("cmdm.reload provides server config reload permissions");
        LOGGER.info("cmdm.generate provides CMD texture list regeneration permissions");
    }
    public boolean hasPermission(ServerPlayerEntity player, String string) {
        return LuckPermsProvider.get().getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player).checkPermission(string).asBoolean();
    }

    @Override
    public void init() {
        LuckPermsProvider.get().getNodeBuilderRegistry().forPermission().permission(PermissionHandler.CONFIG_GENERATE_NODE).build();
        LuckPermsProvider.get().getNodeBuilderRegistry().forPermission().permission(PermissionHandler.CONFIG_GENERATE_NODE).build();
        LuckPermsProvider.get().getNodeBuilderRegistry().forPermission().permission(PermissionHandler.CONFIG_GENERATE_NODE).build();
        LuckPermsProvider.get().getNodeBuilderRegistry().forPermission().permission(PermissionHandler.CONFIG_GENERATE_NODE).build();
    }
}
