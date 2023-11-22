package mod.linguardium.cmdm;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeAdapters {
    public static TypeAdapter<Identifier> IDENTIFIER_ADAPTER = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Identifier value) throws IOException {
            out.jsonValue(value.toString());
        }

        @Override
        public Identifier read(JsonReader in) throws IOException {
            return Identifier.tryParse(in.nextString());
        }
    };
    public static TypeAdapter<List<Integer>> INTEGER_LIST_ADAPTER = new TypeAdapter<List<Integer>>() {
        @Override
        public void write(JsonWriter out, List<Integer> list) throws IOException {
            out.beginArray();
            for (Integer v:list) {
                out.value(v);
            }
            out.endArray();
        }

        @Override
        public List<Integer> read(JsonReader in) throws IOException {
            ArrayList<Integer> vals = new ArrayList<>();
            in.beginArray();
            while(in.hasNext()) {
                vals.add(in.nextInt());
            }
            in.endArray();
            return vals;
        }
    };
    public static TypeAdapter<Config> CONFIG_ADAPTER = new TypeAdapter<Config>() {
        @Override
        public void write(JsonWriter out, Config config) throws IOException {
            out.beginObject();
            out.name("cost");
            out.beginObject();
            out.name("item");
            out.value(config.cost().item().toString());
            out.name("count");
            out.value(config.cost().count());
            out.name("levels");
            out.value(config.cost().levels());
            out.endObject();
            out.name("entries");
            out.beginObject();
            for (Map.Entry<Identifier, List<Integer>> e: config.entries().entrySet()) {
                out.name(e.getKey().toString());
                out.beginArray();
                for (Integer i : e.getValue()) {
                    out.value(i);
                }
                out.endArray();
            }
            out.endObject();
            out.endObject();
        }

        @Override
        public Config read(JsonReader in) throws IOException {
            Map<Identifier,List<Integer>> CMDEntries = new HashMap<>();
            Config.Cost cost=null;
            in.beginObject();
            while (in.hasNext()) {
                String name = in.nextName();
                if (name.equals("entries")) {
                    in.beginObject();
                    while (in.hasNext()) {
                        CMDEntries.put(new Identifier(in.nextName()), INTEGER_LIST_ADAPTER.read(in));
                    }
                    in.endObject();
                } else if (name.equals("cost")) {
                    Identifier itemId = Registries.ITEM.getId(Items.AIR);
                    int count = 0;
                    int levels = 0;

                    in.beginObject();
                    while (in.hasNext()) {
                        switch (in.nextName()) {
                            case "item" -> itemId = new Identifier(in.nextString());
                            case "count" -> count = in.nextInt();
                            case "levels" -> levels = in.nextInt();
                        }
                    }
                    in.endObject();
                    cost = new Config.Cost(itemId, count, levels);
                }
            }
            in.endObject();
            if (cost==null) { cost = new Config.Cost(Registries.ITEM.getId(Items.AIR),0,1); }

            return new Config(cost,CMDEntries);
        }
    };
}
