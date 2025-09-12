package me.contaria.seedqueue.gui;

import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.util.Rect2i;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;

public class MemoryWarningScreen extends ConfirmScreen {
	static final String WIKI_URI = "https://github.com/contariaa/seedqueue/wiki/Troubleshooting#not-enough-memory";

	static MutableText WIKI_TEXT = new TranslatableText("seedqueue.menu.memoryWarning.wikiLink");
	static MutableText HOVERED_WIKI_TEXT = WIKI_TEXT
		.shallowCopy()
		.styled(style -> style.withFormatting(Formatting.UNDERLINE));

	boolean mouseOverWikiText = false;

	public MemoryWarningScreen(int allocatedMem, int recommendedMinMem) {
		super(
			(shouldShowAgain) -> {
				if (!shouldShowAgain) {
					SeedQueue.config.checkMinMemory = false;

					try {
						SeedQueue.config.container.save();
					} catch (IOException e) {
						SeedQueue.LOGGER.error("failed to save the SeedQueue config", e);
					}
				}

				MinecraftClient.getInstance().openScreen(null);
			},
			new TranslatableText("seedqueue.menu.memoryWarning.title"),
			new TranslatableText("seedqueue.menu.memoryWarning.message", allocatedMem, recommendedMinMem),
			ScreenTexts.PROCEED,
			new TranslatableText("seedqueue.menu.memoryWarning.doNotWarnAgain")
		);
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		this.drawCenteredText(
			matrices,
			this.textRenderer,
			this.mouseOverWikiText ? HOVERED_WIKI_TEXT : WIKI_TEXT,
			this.width / 2,
			this.height / 6 + 76,
			0xffffff
		);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		assert this.client != null;

		if (button == 0 && mouseOverWikiText((int) mouseX, (int) mouseY)) {
			if (this.client.options.chatLinksPrompt) {
				this.client.openScreen(new ConfirmChatLinkScreen((open) -> {
					if (open) {
						Util.getOperatingSystem().open(WIKI_URI);
					}

					this.client.openScreen(this);
				}, WIKI_URI, true));
			} else {
				Util.getOperatingSystem().open(WIKI_URI);
			}
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.mouseOverWikiText = mouseOverWikiText((int) mouseX, (int) mouseY);
	}

	private boolean mouseOverWikiText(int mouseX, int mouseY) {
		int wikiTextWidth = this.textRenderer.getWidth(WIKI_TEXT);

		int startX = this.width / 2 - wikiTextWidth / 2;
		int startY = this.height / 6 + 76;

		Rect2i wikiTextRect = new Rect2i(startX, startY, wikiTextWidth, textRenderer.fontHeight);

		return wikiTextRect.contains(mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			this.callback.accept(true);
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
}
