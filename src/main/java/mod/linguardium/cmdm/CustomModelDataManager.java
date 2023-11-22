package mod.linguardium.cmdm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mod.linguardium.cmdm.TypeAdapters.CONFIG_ADAPTER;
import static mod.linguardium.cmdm.TypeAdapters.IDENTIFIER_ADAPTER;

public class CustomModelDataManager implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("custommodeldatamanager");
	public static Config CustomModelDataOptions = new Config(new Config.Cost(Registries.ITEM.getId(Items.AIR),0,0),new HashMap<>());
	//public static final Map<Identifier, List<Integer>> CustomModelDataOptions = new HashMap<>();
	public static final Path ConfigFile = FabricLoader.getInstance().getConfigDir().resolve("cmdm.json");

	public static Gson gson = new GsonBuilder()
			.registerTypeAdapter(Config.class,CONFIG_ADAPTER)
			.setPrettyPrinting().create();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		try { loadConfig(); } catch (IOException ignored) { }
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("cmdm_reload_config")
					.requires(s->s.hasPermissionLevel(2))
					.executes(context -> {
						try {
							loadConfig();
						} catch (Exception e) {
							LOGGER.warn(e.getMessage());
							e.printStackTrace();
							context.getSource().sendFeedback(()->Text.literal("Failed to reload config."),false);
						}
						return 0;
					})
			);
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("cmdm").executes(context -> {
				CMDSelector cmdmScreen = new CMDSelector(context.getSource().getPlayer());
				cmdmScreen.open();
				return 0;
			}));
		});
	}
	public void loadConfig() throws IOException {
			assert(Files.exists(ConfigFile));
			CustomModelDataOptions = gson.fromJson(Files.newBufferedReader(ConfigFile), Config.class);
			LOGGER.info("Loaded config file");
	}
}