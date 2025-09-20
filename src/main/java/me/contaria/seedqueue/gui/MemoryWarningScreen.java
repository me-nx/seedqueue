package me.contaria.seedqueue.gui;

import me.contaria.seedqueue.SeedQueue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.Rect2i;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.List;

public class MemoryWarningScreen extends Screen {
	static final String WIKI_URI = "https://github.com/contariaa/seedqueue/wiki/Troubleshooting#not-enough-memory";

	static final MutableText HOVERED_WIKI_LINK = new LiteralText(WIKI_URI)
		.styled(style -> style.withFormatting(Formatting.UNDERLINE));

	final Text message;
	List<StringRenderable> messageParts;
	int wikiLinkY;

	public MemoryWarningScreen(int allocatedMem, int recommendedMinMem) {
		super(new TranslatableText("seedqueue.menu.memoryWarning.title"));

		this.message = new TranslatableText("seedqueue.menu.memoryWarning.message", allocatedMem, recommendedMinMem);
	}

	@Override
	protected void init() {
		super.init();

		this.messageParts = this.textRenderer.wrapLines(this.message, this.width - 50);

		int btnXBase = this.width / 2 - 155;
		int btnY = this.height / 6 + 100;

		this.addButton(new ButtonWidget(btnXBase, btnY, 150, 20, ScreenTexts.PROCEED, (_btn) ->
				MinecraftClient.getInstance().openScreen(null)
		));

		Text ignoreWarningText = new TranslatableText("seedqueue.menu.memoryWarning.doNotWarnAgain");

		this.addButton(new ButtonWidget(btnXBase + 160, btnY, 150, 20, ignoreWarningText, (_btn) -> {
			SeedQueue.config.checkMinMemory = false;

			try {
				SeedQueue.config.container.save();
			} catch (IOException e) {
				SeedQueue.LOGGER.error("failed to save the SeedQueue config", e);
			}

			MinecraftClient.getInstance().openScreen(null);
		}));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		this.drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 70, 0xffffff);

		int nextLineY = 90;

		for (StringRenderable line : messageParts) {
			this.drawCenteredText(matrices, this.textRenderer, line, this.width / 2, nextLineY, 0xffffff);
			nextLineY += 9;
		}

		nextLineY += 9;

		Text wikiLinkInfo = new TranslatableText("seedqueue.menu.memoryWarning.wikiLinkInfo");
		this.drawCenteredText(matrices, this.textRenderer, wikiLinkInfo, this.width / 2, nextLineY, 0xffffff);
		nextLineY += 9;

		this.drawCenteredText(
			matrices,
			this.textRenderer,
			this.mouseOverWikiText(mouseX, mouseY) ? HOVERED_WIKI_LINK : StringRenderable.plain(WIKI_URI),
			this.width / 2,
			nextLineY,
			0xffffff
		);
		wikiLinkY = nextLineY;

		super.render(matrices, mouseX, mouseY, delta);
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

	private boolean mouseOverWikiText(int mouseX, int mouseY) {
		int wikiTextWidth = this.textRenderer.getWidth(WIKI_URI);

		int startX = this.width / 2 - wikiTextWidth / 2;

		Rect2i wikiTextRect = new Rect2i(startX, wikiLinkY, wikiTextWidth, textRenderer.fontHeight);

		return wikiTextRect.contains(mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == 256) {
			MinecraftClient.getInstance().openScreen(null);
			return true;
		} else {
			return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
}
