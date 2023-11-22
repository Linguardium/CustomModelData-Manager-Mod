package mod.linguardium.cmdm;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.render.model.json.ModelOverride;
import net.minecraft.util.Identifier;

import java.util.List;

public class UnbakedModelEvents {
    public static Event<ModelOverrideInitEvent> EVENT = EventFactory.createArrayBacked(ModelOverrideInitEvent.class,
            (listeners) -> (id, overrides) -> {
                for (ModelOverrideInitEvent listener : listeners) {
                    listener.onInit(id,overrides);
                }
            });

    @FunctionalInterface
    public interface ModelOverrideInitEvent {
        void onInit(Identifier id, List<ModelOverride> overrides);
    }
}
