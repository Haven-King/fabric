/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.config;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.config.ConfigsLoadedEntrypoint;
import net.fabricmc.loader.api.config.value.ValueContainerProvider;

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
