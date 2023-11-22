package mod.linguardium.cmdm;

import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;

public class VirtualMerchantInventory extends MerchantInventory {
    public VirtualMerchantInventory(Merchant merchant) {
        super(merchant);
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

}
