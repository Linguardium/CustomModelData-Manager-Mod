package mod.linguardium.cmdm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static mod.linguardium.cmdm.CustomModelDataManager.*;

public class CustomModelDataManagerClient implements ClientModInitializer {
	private static Identifier CMD_KEY = new Identifier("custom_model_data");
	private static Map<Identifier, List<Integer>> CustomModelDataValues = new HashMap<>();
	public static KeyBinding CONFIG_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("cmdm.key.config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,KeyBinding.MISC_CATEGORY));

	@Override
	public void onInitializeClient() {
		UnbakedModelEvents.EVENT.register((id, overrides) -> {
			overrides.forEach(override->{
				override.streamConditions().filter(c->c.getType().equals(CMD_KEY)).forEach(condition->{
					CustomModelDataValues.computeIfAbsent(id,k->new ArrayList<>()).add((int)condition.getThreshold());
				});
			});
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("cmdm_generate").executes(context -> {
						CustomModelDataValues.clear();
						context.getSource().getClient().reloadResources().whenComplete((c,f)-> {
							registryAccess.createWrapper(RegistryKeys.ITEM).streamEntries().forEach(item-> {
								ModelIdentifier mid = MinecraftClient.getInstance().getItemRenderer().getModels().modelIds.get(Item.getRawId(item.value()));
								Optional<RegistryKey<Item>> oKey = item.getKey();
								if (mid != null && oKey.isPresent()) {
									Identifier id = mid.withPrefixedPath("models/item/").withSuffixedPath(".json");
									List<Integer> list = CustomModelDataValues.remove(id);
									if (list != null) {
										CustomModelDataValues.put(oKey.get().getValue(), list);
									}
								}
							});
							CustomModelDataOptions.entries().clear();
							CustomModelDataOptions.entries().putAll(CustomModelDataValues);
							saveConfig();
						});
						return 0;
					})
			);
		});

		ClientTickEvents.END_WORLD_TICK.register(world -> {
			if(CONFIG_KEY.wasPressed()) {
				if (MinecraftClient.getInstance().currentScreen instanceof ItemCMDEditScreen) {
					MinecraftClient.getInstance().setScreen(null);
				}else{
					MinecraftClient.getInstance().setScreen(new ItemCMDEditScreen(Text.literal("CustomModelData display")));
				}
				while (CONFIG_KEY.wasPressed()) {}
			}

		});
	}
	public static void saveConfig() {
		try {
			Files.writeString(ConfigFile, gson.toJson(CustomModelDataOptions));
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Generated config on client. Please copy to server and reload server config."));
			LOGGER.info("Generated CMDM config on client.");
		}catch (IOException e) {
			LOGGER.warn(e.getMessage());
			e.printStackTrace();
			MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("Unable to save config file. Check client logs for more info"));
		}
	}

}