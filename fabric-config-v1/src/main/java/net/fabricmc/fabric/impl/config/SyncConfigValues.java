package net.fabricmc.fabric.impl.config;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.config.v1.DataTypes;
import net.fabricmc.fabric.api.config.v1.SyncType;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigManager;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ValueContainer;
import net.fabricmc.loader.api.config.value.ValueContainerProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

public class SyncConfigValues implements ModInitializer, ClientModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final Identifier CONFIG_VALUES = new Identifier("fabric", "packet/sync_values");

	@Override
	@Environment(EnvType.CLIENT)
	public void onInitializeClient() {
		ClientPlayConnectionEvents.JOIN.register(SyncConfigValues::sendConfigValues);
		ClientPlayConnectionEvents.INIT.register((handler, client) -> {
			ClientPlayNetworking.registerReceiver(CONFIG_VALUES, SyncConfigValues::receiveConfigValues);
		});
	}

	@Override
	public void onInitialize() {
		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
			ServerPlayNetworking.registerReceiver(handler, CONFIG_VALUES, SyncConfigValues::receiveConfigValues);
		}));
	}

	@Environment(EnvType.CLIENT)
	private static void sendConfigValues(ClientPlayNetworkHandler clientPlayNetworkHandler, PacketSender packetSender, MinecraftClient client) {
		sendConfigValues();
	}

	@Environment(EnvType.CLIENT)
	public static void sendConfigValues() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!client.isIntegratedServerRunning() && client.getCurrentServerEntry() == null) return;

		ValueContainer valueContainer = ValueContainer.ROOT;

		for (ConfigDefinition configDefinition : ConfigManager.getConfigKeys()) {
			Collection<SyncType> syncTypes = new ArrayList<>();

			for (ConfigValue<?> value : configDefinition) {
				for (SyncType syncType : value.getData(DataTypes.SYNC_TYPE)) {
					if (syncType != SyncType.NONE) {
						syncTypes.add(syncType);
					}
				}
			}

			for (SyncType syncType : configDefinition.getData(DataTypes.SYNC_TYPE)) {
				if (syncType != SyncType.NONE) {
					syncTypes.add(syncType);
				}
			}

			if (syncTypes.size() > 0) {
				ConfigSerializer serializer = configDefinition.getSerializer();
				PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

				buf.writeString(configDefinition.toString());
				buf.writeVarInt(syncTypes.size());

				for (SyncType syncType : syncTypes) {
					buf.writeEnumConstant(syncType);
				}

				try {
					byte[] bytes = Files.readAllBytes(serializer.getPath(configDefinition, valueContainer));
					buf.writeByteArray(bytes);
					ClientPlayNetworking.send(CONFIG_VALUES, buf);
				} catch (IOException e) {
					LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
				}
			}
		}
	}


	private static void receiveConfigValues(MinecraftServer server, ServerPlayerEntity sender, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender packetSender) {
		UUID playerUuid = sender.getUuid();

		ConfigDefinition configDefinition = ConfigManager.getDefinition(buf.readString(32767));

		Collection<SyncType> syncTypes = new HashSet<>();

		int n = buf.readVarInt();
		for (int i = 0; i < n; ++i) {
			syncTypes.add(buf.readEnumConstant(SyncType.class));
		}

		// We won't save the config values on the server if the definition is null, but we'll still forward it to other
		// players in the case of peer to peer syncing.
		if (configDefinition != null && syncTypes.contains(SyncType.INFO)) {
			InputStream inputStream = new ByteArrayInputStream(buf.readByteArray());
			ValueContainer valueContainer = ((ValueContainerProvider) server).getPlayerValueContainer(playerUuid);

			try {
				configDefinition.getSerializer().deserialize(configDefinition, inputStream, valueContainer);
			} catch (IOException e) {
				LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
			}
		}

		buf.resetReaderIndex();

		if (syncTypes.contains(SyncType.P2P)) {
			sendConfigValues(sender, server, buf);
		}
	}

	public static void sendConfigValues(ServerPlayerEntity except, MinecraftServer server, PacketByteBuf buf) {
		PacketByteBuf peerBuf = new PacketByteBuf(Unpooled.buffer());

		peerBuf.writeUuid(except.getUuid());
		peerBuf.writeString(buf.readString(32767));

		int n = buf.readVarInt();
		for (int i = 0; i < n; ++i) {
			buf.readEnumConstant(SyncType.class);
		}

		peerBuf.writeBytes(buf.readByteArray());

		PlayerLookup.all(server).forEach(player -> {
			if (player != except) {
				ServerPlayNetworking.send(player, CONFIG_VALUES, peerBuf);
			}
		});
	}

	@Environment(EnvType.CLIENT)
	private static void receiveConfigValues(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf buf, PacketSender packetSender) {
		UUID playerId = buf.readUuid();

		ConfigDefinition configDefinition = ConfigManager.getDefinition(buf.readString());

		// We won't save the config values on the server if the definition is null, but we'll still forward it to other
		// players in the case of peer to peer syncing.
		if (configDefinition != null && client.getCurrentServerEntry() != null) {
			InputStream inputStream = new ByteArrayInputStream(buf.readByteArray());
			ValueContainer valueContainer = ((ValueContainerProvider) client.getCurrentServerEntry()).getPlayerValueContainer(playerId);

			try {
				configDefinition.getSerializer().deserialize(configDefinition, inputStream, valueContainer);
			} catch (IOException e) {
				LOGGER.error("Failed to sync config '{}': {}", configDefinition, e.getMessage());
			}
		}
	}
}
