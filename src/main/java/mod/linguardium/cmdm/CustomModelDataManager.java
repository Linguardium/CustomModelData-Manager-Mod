package mod.linguardium.cmdm;

import mod.linguardium.cmdm.gui.MenuGui;
import mod.linguardium.cmdm.gui.UserUniqueGui;
import mod.linguardium.cmdm.permission.LuckPermsPermissionHandler;
import mod.linguardium.cmdm.permission.PermissionHandler;
import mod.linguardium.cmdm.permission.VanillaPermissionHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class CustomModelDataManager implements ModInitializer {

	public static final String MODID = "cmdm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
	public static PermissionHandler permissions;

	@Override
	public void onInitialize() {
		Supplier<PermissionHandler> permissionFactory = VanillaPermissionHandler::new;
		if (FabricLoader.getInstance().isModLoaded("luckperms")) {
			permissionFactory= LuckPermsPermissionHandler::new;
		}
		permissions = permissionFactory.get();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(CommandManager.literal("cmdm")
					.requires(ServerCommandSource::isExecutedByPlayer)
					.then(CommandManager.literal("reload")
							.requires(source-> permissions.hasPermission(source.getPlayer(), PermissionHandler.CONFIG_RELOAD_NODE))
							.executes(context -> {
								try {
									Config.getInstance().load();
								} catch (Exception e) {
									LOGGER.warn(e.getMessage());
									e.printStackTrace();
									context.getSource().sendFeedback(()->Text.literal("Failed to reload config."),false);
								}
								return 0;
							})
					)
					.then(CommandManager.literal("generate")
							.requires(source-> permissions.hasPermission(source.getPlayer(), PermissionHandler.CONFIG_GENERATE_NODE))
							.executes(context -> {
								if (context.getSource().getPlayer() == null) return 1;
								context.getSource().getPlayer().sendMessage(Text.literal("[ Click Here Generate Config ]").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE,PermissionHandler.CONFIG_GENERATE_NODE))));
								context.getSource().getPlayer().sendMessage(Text.literal("This link will only work if you are running the mod on your client.").formatted(Formatting.ITALIC).formatted(Formatting.GRAY));
								return 0;
							})
					)
					.then(CommandManager.literal("user")
							.requires(context->permissions.hasPermission(context.getPlayer(), PermissionHandler.ITEM_BUILDER_NODE))
							.executes(context->{
						openUserGui(context.getSource().getPlayer());
						return 0;
					}))
					.executes(context -> {
						if (permissions.hasPermission(context.getSource().getPlayer(), PermissionHandler.ITEM_BUILDER_NODE)) {
							openOPGui(context.getSource().getPlayer());
						}else{
							openUserGui(context.getSource().getPlayer());
						}
						return 0;
					})
			)
		);

		//set up the timers and tell the server to load the config after it has started
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerTimer.timers.clear();
			Config.getInstance();
			if (permissions != null) {
				permissions.init();
			}
		});

		// Clear all timers when the server stops
		ServerLifecycleEvents.SERVER_STOPPING.register(server->
			ServerTimer.timers.clear()
		);

		// tick timers at the end of the server tick
		ServerTickEvents.END_SERVER_TICK.register(server->ServerTimer.tick());

		//register the packet handler for config receiving
		ServerPlayNetworking.registerGlobalReceiver(Packets.CONFIG_PACKET_ID,Packets::receivePacket);
	}
	public static void openUserGui(ServerPlayerEntity player) {
		new UserUniqueGui(player).open();
	}
	public static void openOPGui(ServerPlayerEntity player) {
		new MenuGui(player).open();
	}
}