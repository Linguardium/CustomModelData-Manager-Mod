package mod.linguardium.cmdm;

import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public record Config(Cost cost, Map<Identifier, List<Integer>> entries) {


    public record Cost(Identifier item, Integer count, Integer levels) {


    }
}
