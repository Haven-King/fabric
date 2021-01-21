package net.fabricmc.fabric.api.config.v1;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.util.UUID;

public class ClientUtil {
	public static boolean isLocalPlayer(UUID playerId) {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && playerId.equals(MinecraftClient.getInstance().getSession().getProfile().getId());
	}
}
