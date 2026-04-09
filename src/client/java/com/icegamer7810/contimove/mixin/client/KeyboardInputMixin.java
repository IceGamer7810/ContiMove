package com.icegamer7810.contimove.mixin.client;

import com.icegamer7810.contimove.client.ContiMoveClient;
import com.icegamer7810.contimove.client.ContinuationSnapshot;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.util.math.Vec2f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin extends Input {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void contimove$keepMovementWhileInventoryOpen(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!ContiMoveClient.shouldApplyContinuation(client)) {
            return;
        }

        ContinuationSnapshot snapshot = ContiMoveClient.getActiveSnapshot();
        this.playerInput = snapshot.toPlayerInput();
        this.movementVector = new Vec2f(
            getMovementMultiplier(snapshot.left(), snapshot.right()),
            getMovementMultiplier(snapshot.forward(), snapshot.backward())
        );
        ci.cancel();
    }

    private static float getMovementMultiplier(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        }

        return positive ? 1.0F : -1.0F;
    }
}
