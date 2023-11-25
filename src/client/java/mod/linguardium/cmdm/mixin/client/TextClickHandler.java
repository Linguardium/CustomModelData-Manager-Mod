package mod.linguardium.cmdm.mixin.client;

import mod.linguardium.cmdm.GenerateCommand;
import mod.linguardium.cmdm.permission.PermissionHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public class TextClickHandler {

    @Inject(at=@At("HEAD"),method="handleTextClick", cancellable = true)
    private void handleCustomClickEvent(Style style, CallbackInfoReturnable<Boolean> cir) {
        if (style.getClickEvent() != null &&
            style.getClickEvent().getAction().equals(ClickEvent.Action.CHANGE_PAGE) &&
            style.getClickEvent().getValue().equals(PermissionHandler.CONFIG_GENERATE_NODE)) {
                cir.setReturnValue(true);
                MinecraftClient.getInstance().execute(()->{
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if (player != null) {
                        player.sendMessage(Text.literal("Click registered, generating configuration"));
                        GenerateCommand.generateConfigForConnectedServer();
                    }
                });
        }
    }
}
