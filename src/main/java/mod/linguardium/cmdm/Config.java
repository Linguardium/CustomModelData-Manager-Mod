package mod.linguardium.cmdm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;

public class Config {
    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Cost.CODEC.fieldOf("cost").forGetter(c->c.cost),
                    Codec.unboundedMap(Identifier.CODEC,Codec.list(Codec.INT).xmap(ArrayList::new,l->l)).fieldOf("entries").forGetter(Config::getEntries),
                    Codec.unboundedMap(Uuids.STRING_CODEC,Codec.list(ItemStack.CODEC).xmap(ArrayList::new,l->l)).fieldOf("uniqueItems").forGetter(Config::getUniqueItems)
            ).apply(instance, Config::new));


    Cost cost;
    Map<Identifier, ArrayList<Integer>> entries = new HashMap<>();
    Map<UUID,ArrayList<ItemStack>> uniqueItems = new HashMap<>();

    public Map<UUID, ArrayList<ItemStack>> getUniqueItems() {
        return uniqueItems;
    }
    public List<ItemStack> getAllUniqueItems() {
        return getUniqueItems().entrySet().stream().flatMap(e->e.getValue().stream()).toList();
    }
    public void setUniqueItems(Map<UUID, ArrayList<ItemStack>> uniqueItems) {
        this.uniqueItems = new HashMap<>(uniqueItems);
    }


    Config(Cost cost, Map<Identifier, ArrayList<Integer>> entries, Map<UUID,ArrayList<ItemStack>> uniqueItems) {
        this.cost=cost;
        setEntries(entries);
        setUniqueItems(uniqueItems);
    }

    public Map<Identifier, ArrayList<Integer>> getEntries() {
        return entries;
    }

    public void setEntries(Map<Identifier, ArrayList<Integer>> entries) {
        this.entries = new HashMap<>(entries);
    }

    public record Cost(Identifier item, Integer count, Integer levels) {
        public static final Codec<Cost> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("item").forGetter(Cost::item),
                        Codec.INT.fieldOf("count").forGetter(Cost::count),
                        Codec.INT.fieldOf("levels").forGetter(Cost::levels)
                ).apply(instance,Cost::new));
    }

    public static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("cmdm.json");
    public static final Path CONFIG_BACKUP_PATH = FabricLoader.getInstance().getConfigDir().resolve("cmdm.bak");
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Config INSTANCE;
    public static Config getInstance() {
        if (INSTANCE==null) {
            try {
                INSTANCE = initialLoad();
            } catch (IOException e) {
                INSTANCE = new Config(new Cost(Registries.ITEM.getId(Items.AIR),0,0),new HashMap<>(), new HashMap<>());
                LOGGER.warn("Failed to load config.");
                try {
                    INSTANCE.save();
                }catch (IOException ex) {
                    LOGGER.warn("Also failed to save the config file...");
                    LOGGER.warn(ex.getMessage());
                }
            }
        }
        return INSTANCE;
    }
    public static Config initialLoad() throws IOException {
        assert (Files.exists(CONFIG_PATH));
        Config c = Util.getResult(Config.CODEC.parse(JsonOps.INSTANCE,GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), JsonElement.class)),IOException::new);
        LOGGER.info("Loaded config file");
        return c;
    }
    public void load() throws IOException {
        Config c = initialLoad();
        this.cost = c.cost;
        this.setEntries(c.getEntries());
        this.setUniqueItems(c.getUniqueItems());
    }
    public void save() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            Files.deleteIfExists(CONFIG_BACKUP_PATH);
            Files.move(CONFIG_PATH,CONFIG_BACKUP_PATH, ATOMIC_MOVE);
        }
        DataProvider.writeCodecToPath(DataWriter.UNCACHED,Config.CODEC,this,CONFIG_PATH);
        LOGGER.info("Saved Configuration");
    }

}
