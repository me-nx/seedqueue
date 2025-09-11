package me.contaria.seedqueue.mixin.client;

import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {
	@Unique
	private static boolean warningShown = false;

	@Inject(method = "init", at = @At("TAIL"))
	private void titleScreenInit(CallbackInfo _ci) {
		if (!warningShown && SeedQueue.config.checkMinMemory) {
			warningShown = true;
			SeedQueue.checkRamAllocation();
		}
	}
}
