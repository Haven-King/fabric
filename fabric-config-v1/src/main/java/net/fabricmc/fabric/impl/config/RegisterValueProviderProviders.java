package net.fabricmc.fabric.impl.config;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.config.ConfigsLoadedEntrypoint;
import net.fabricmc.loader.api.config.value.ValueContainerProvider;
import net.minecraft.client.MinecraftClient;

public class RegisterValueProviderProviders implements ConfigsLoadedEntrypoint {
	@Override
	public void onConfigsLoaded() {
		ValueContainerProvider.register(saveType -> {
			EnvType envType = FabricLoader.getInstance().getEnvironmentType();


			if (saveType == FabricSaveTypes.LEVEL && envType == EnvType.CLIENT) {
				MinecraftClient client = MinecraftClient.getInstance();

				if (client.isIntegratedServerRunning() && client.getServer() != null) {
					return ((ValueContainerProvider) client.getServer());
				} else if (client.getCurrentServerEntry() != null) {
					return ((ValueContainerProvider) client.getCurrentServerEntry());
				}
			}

			return null;
		});
 	}
}
