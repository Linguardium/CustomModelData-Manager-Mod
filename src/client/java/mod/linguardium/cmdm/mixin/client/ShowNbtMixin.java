package mod.linguardium.cmdm.mixin.client;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.visitor.StringNbtWriter;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ShowNbtMixin {
    @Shadow @Nullable public abstract NbtCompound getNbt();

    @Inject(method="getTooltip", at=@At("RETURN"), cancellable = true)
    private void addNbtTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (getNbt() != null) {
            cir.getReturnValue().add(Text.literal(new StringNbtWriter().apply(getNbt())));
        }
    }
}
