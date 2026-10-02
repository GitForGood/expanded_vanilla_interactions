package net.expandedvanilla.mixin.boat_mooring;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.event.GameEvent;
import net.expandedvanilla.interactions.boat_mooring.BoatMooringHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashKnotEntity.class)
public class LeashKnotEntityMixin {

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void evi$onInteract(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        LeashKnotEntity knot = (LeashKnotEntity) (Object) this;
        if (knot.getWorld().isClient) {
            cir.setReturnValue(ActionResult.SUCCESS);
            return;
        }

        boolean handled = BoatMooringHandler.interactWithKnot(knot, player);
        if (handled) {
            knot.emitGameEvent(GameEvent.BLOCK_ATTACH, player);
            // If the knot was untied and is empty, check if it should discard
            cir.setReturnValue(ActionResult.CONSUME);
        }
    }

    @Inject(method = "onBreak", at = @At("HEAD"))
    private void evi$onBreak(Entity breaker, CallbackInfo ci) {
        LeashKnotEntity knot = (LeashKnotEntity) (Object) this;
        if (!knot.getWorld().isClient) {
            BoatMooringHandler.detachAllFromKnot(knot);
        }
    }
}
