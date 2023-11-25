package mod.linguardium.cmdm.permission;

import net.minecraft.server.network.ServerPlayerEntity;

public interface PermissionHandler {
    String ITEM_BUILDER_NODE = "cmdm.admin.builder";
    String UNIQUE_ADDER_NODE = "cmdm.admin.uniques";
    String CONFIG_RELOAD_NODE = "cmdm.admin.reload";
    String CONFIG_GENERATE_NODE = "cmdm.admin.generate";
    boolean hasPermission(ServerPlayerEntity player, String string);
    void init();
}
