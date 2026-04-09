package com.icegamer7810.contimove.client;

import com.icegamer7810.contimove.ContiMove;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ContiMoveClient implements ClientModInitializer {
    private static KeyBinding toggleKeyBinding;
    private static boolean toggleEnabled;
    private static boolean continuationActive;
    private static boolean previousContinuationScreenOpen;
    private static ContinuationSnapshot manualSnapshot = ContinuationSnapshot.NONE;
    private static ContinuationSnapshot activeSnapshot = ContinuationSnapshot.NONE;

    @Override
    public void onInitializeClient() {
        toggleKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.contimove.toggle",
            GLFW.GLFW_KEY_M,
            new KeyBinding.Category(Identifier.of(ContiMove.MOD_ID, "general"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(ContiMoveClient::tickClient);
    }

    private static void tickClient(MinecraftClient client) {
        while (toggleKeyBinding.wasPressed()) {
            toggleEnabled = !toggleEnabled;
            if (!toggleEnabled) {
                stopContinuation();
            }
            sendToggleMessage(client, toggleEnabled);
        }

        if (client.options == null || client.player == null) {
            stopContinuation();
            previousContinuationScreenOpen = false;
            manualSnapshot = ContinuationSnapshot.NONE;
            return;
        }

        boolean continuationScreenOpen = isContinuationScreen(client.currentScreen);

        if (!continuationScreenOpen) {
            manualSnapshot = ContinuationSnapshot.capture(client.options);
        }

        if (!toggleEnabled) {
            if (!continuationScreenOpen) {
                previousContinuationScreenOpen = false;
            }
            return;
        }

        if (continuationScreenOpen && !previousContinuationScreenOpen) {
            activeSnapshot = manualSnapshot;
            continuationActive = activeSnapshot.hasAnyTrackedInput();
        } else if (!continuationScreenOpen && previousContinuationScreenOpen) {
            stopContinuation();
        }

        previousContinuationScreenOpen = continuationScreenOpen;
    }

    private static boolean isContinuationScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen;
    }

    private static void stopContinuation() {
        continuationActive = false;
        activeSnapshot = ContinuationSnapshot.NONE;
    }

    private static void sendToggleMessage(MinecraftClient client, boolean enabled) {
        if (client.player == null) {
            return;
        }

        MutableText stateText = Text.literal(enabled ? "ON" : "OFF")
            .formatted(enabled ? Formatting.GREEN : Formatting.RED);
        client.player.sendMessage(Text.literal("Move Continuation: ").append(stateText), true);
    }

    public static boolean shouldApplyContinuation(MinecraftClient client) {
        return toggleEnabled
            && continuationActive
            && client.player != null
            && isContinuationScreen(client.currentScreen);
    }

    public static ContinuationSnapshot getActiveSnapshot() {
        return activeSnapshot;
    }
}
