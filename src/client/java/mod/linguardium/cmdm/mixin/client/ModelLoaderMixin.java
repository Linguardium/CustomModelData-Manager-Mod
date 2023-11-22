package mod.linguardium.cmdm.mixin.client;

import mod.linguardium.cmdm.UnbakedModelEvents;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin {
    @Inject(method="<init>",at=@At(value="RETURN",ordinal = 0))
    private void onUnbakedModelInit(BlockColors blockColors, Profiler profiler, Map<Identifier, JsonUnbakedModel> jsonUnbakedModels, Map blockStates, CallbackInfo ci) {
        for (Map.Entry<Identifier,JsonUnbakedModel> model: jsonUnbakedModels.entrySet()) {
            UnbakedModelEvents.EVENT.invoker().onInit(model.getKey(), model.getValue().getOverrides());
        }
    }

}
