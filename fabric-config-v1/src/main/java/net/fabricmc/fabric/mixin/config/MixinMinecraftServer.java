package net.fabricmc.fabric.mixin.config;

import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.datafixers.DataFixer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.config.v1.ClientUtil;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.api.config.value.ValueContainerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListenerFactory;
import net.minecraft.util.UserCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.Proxy;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

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
