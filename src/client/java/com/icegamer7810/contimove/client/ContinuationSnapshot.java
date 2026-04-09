package com.icegamer7810.contimove.client;

import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;

public record ContinuationSnapshot(
    boolean forward,
    boolean backward,
    boolean left,
    boolean right,
    boolean jump,
    boolean sneak,
    boolean sprint
) {
    public static final ContinuationSnapshot NONE = new ContinuationSnapshot(false, false, false, false, false, false, false);

    public static ContinuationSnapshot capture(GameOptions options) {
        return new ContinuationSnapshot(
            options.forwardKey.isPressed(),
            options.backKey.isPressed(),
            options.leftKey.isPressed(),
            options.rightKey.isPressed(),
            options.jumpKey.isPressed(),
            options.sneakKey.isPressed(),
            options.sprintKey.isPressed()
        );
    }

    public boolean hasAnyTrackedInput() {
        return this.forward || this.backward || this.left || this.right || this.jump || this.sneak || this.sprint;
    }

    public PlayerInput toPlayerInput() {
        return new PlayerInput(this.forward, this.backward, this.left, this.right, this.jump, this.sneak, this.sprint);
    }
}
