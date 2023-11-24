package mod.linguardium.cmdm;

import eu.pb4.sgui.api.gui.GuiInterface;
import mod.linguardium.cmdm.gui.MenuGui;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomModelDataManager implements ModInitializer {

	public static final String MODID = "cmdm";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		// Register the cmdm command and the op "reload" command from it
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("cmdm")
					.then(CommandManager.literal("reload")
							.requires(s->
									(	environment.integrated &&
										s.isExecutedByPlayer() &&
										s.getServer().isHost(s.getPlayer().getGameProfile())
									) ||
									s.hasPermissionLevel(2)
							)
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
					).executes(context -> {
						GuiInterface cmdmScreen = new MenuGui(context.getSource().getPlayer());
						cmdmScreen.open();
						return 0;
					})
			);
		});

		//set up the timers and tell the server to load the config after it has started
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ServerTimer.timers.clear();
			Config.getInstance();
		});

		// Clear all timers when the server stops
		ServerLifecycleEvents.SERVER_STOPPING.register(server->{
			ServerTimer.timers.clear();
		});

		// tick timers at the end of the server tick
		ServerTickEvents.END_SERVER_TICK.register(server->ServerTimer.tick());

		//register the packet handler for config receiving
		ServerPlayNetworking.registerGlobalReceiver(Packets.CONFIG_PACKET_ID,Packets::receivePacket);
	}
}