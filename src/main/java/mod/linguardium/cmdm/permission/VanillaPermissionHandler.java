package mod.linguardium.cmdm.permission;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class VanillaPermissionHandler implements PermissionHandler {
    public VanillaPermissionHandler() {
        LOGGER.info("Using vanilla permissions");
        LOGGER.info("OP or single player has access to all the things");
    }

    @Override
    public boolean hasPermission(ServerPlayerEntity player, String string) {
        return hasOperatorPermissions(player,player.getServer());
    }

    @Override
    public void init() {

    }

    public static boolean hasOperatorPermissions(ServerPlayerEntity player, MinecraftServer server) {
        return isOperator(player,server) || (server.isRemote() && server.isHost(player.getGameProfile()));
    }

    public static boolean isOperator(ServerPlayerEntity player, MinecraftServer server) {
        return server != null && server.getPlayerManager().isOperator(player.getGameProfile());
    }
}
