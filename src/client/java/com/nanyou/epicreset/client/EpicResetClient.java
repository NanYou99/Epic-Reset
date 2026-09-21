package com.nanyou.epicreset.client;

import com.nanyou.epicreset.client.event.ClientTooltipHandler;
import net.fabricmc.api.ClientModInitializer;

public class EpicResetClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientTooltipHandler.register();
	}
}
