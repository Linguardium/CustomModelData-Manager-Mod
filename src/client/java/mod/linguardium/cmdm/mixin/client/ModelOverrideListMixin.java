package mod.linguardium.cmdm.mixin.client;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import mod.linguardium.cmdm.client.model.ModelOverrideListAdditions;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.stream.Collectors;

@Mixin(ModelOverrideList.class)
public class ModelOverrideListMixin implements ModelOverrideListAdditions {
    @Shadow @Final private Identifier[] conditionTypes;

    @Shadow @Final private ModelOverrideList.BakedOverride[] overrides;

    @Override
    public Map<Identifier,List<Float>> cmdm$getIdentifiedConditionThresholds() {
        return Arrays
                .stream(overrides)
                .flatMap(bo->
                        Arrays.stream(bo.conditions)
                                .map(boc->new Pair<>(conditionTypes[boc.index], Lists.newArrayList(boc.threshold)))
                )
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond,(a,b)->{a.addAll(b);return a;}));
    }
}
