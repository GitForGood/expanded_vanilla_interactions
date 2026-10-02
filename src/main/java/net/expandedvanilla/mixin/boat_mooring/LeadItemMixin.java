package net.expandedvanilla.mixin.boat_mooring;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.LeadItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.expandedvanilla.interactions.boat_mooring.BoatMooringHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeadItem.class)
public class LeadItemMixin {

    @Inject(method = "attachHeldMobsToBlock", at = @At("RETURN"), cancellable = true)
    private static void evi$onAttachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos, CallbackInfoReturnable<ActionResult> cir) {
        boolean attachedBoats = BoatMooringHandler.attachHeldBoatsToBlock(player, world, pos);
        if (attachedBoats) {
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }
}
