package mod.linguardium.cmdm.client.model;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public interface ModelOverrideListAdditions {
    Map<Identifier,List<Float>> cmdm$getIdentifiedConditionThresholds();
}
