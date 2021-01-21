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

package net.fabricmc.fabric.mixin.config;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.api.config.v1.ClientUtil;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.api.config.value.ValueContainerProvider;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer implements ValueContainerProvider {
	@Shadow
	public abstract Path getSavePath(WorldSavePath worldSavePath);

	@Unique
	private Map<UUID, ValueContainer> playerValueContainers;
	@Unique private ValueContainer valueContainer;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Thread thread, DynamicRegistryManager.Impl impl, LevelStorage.Session session, SaveProperties saveProperties, ResourcePackManager resourcePackManager, Proxy proxy, DataFixer dataFixer, ServerResourceManager serverResourceManager, MinecraftSessionService minecraftSessionService, GameProfileRepository gameProfileRepository, UserCache userCache, WorldGenerationProgressListenerFactory worldGenerationProgressListenerFactory, CallbackInfo ci) {
		this.playerValueContainers = new HashMap<>();
		this.valueContainer = ValueContainer.of(this.getSavePath(WorldSavePath.ROOT).normalize().resolve("config"), FabricSaveTypes.LEVEL);
	}

	@Override
	public ValueContainer getValueContainer() {
		return this.valueContainer;
	}

	@Override
	public ValueContainer getPlayerValueContainer(UUID playerId) {
		if (ClientUtil.isLocalPlayer(playerId)) {
			return ValueContainer.ROOT;
		}

		return this.playerValueContainers.computeIfAbsent(playerId, id -> ValueContainer.of(null, FabricSaveTypes.USER));
	}

	@Override
	public int playerCount() {
		return playerValueContainers.size();
	}

	@NotNull
	@Override
	public Iterator<Map.Entry<UUID, ValueContainer>> iterator() {
		return playerValueContainers.entrySet().iterator();
	}
}
