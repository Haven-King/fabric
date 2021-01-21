package net.fabricmc.fabric.test;

import net.fabricmc.fabric.api.config.v1.DataTypes;
import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.fabric.api.config.v1.SyncType;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.SaveType;
import net.fabricmc.loader.api.config.data.DataCollector;
import net.fabricmc.loader.api.config.data.DataType;
import net.fabricmc.loader.api.config.serialization.PropertiesSerializer;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ConfigValueCollector;
import net.fabricmc.loader.api.entrypoint.ConfigInitializer;
import org.jetbrains.annotations.NotNull;

public class ConfigTest implements ConfigInitializer {
	public static final ConfigValue<Integer> MY_FAVORITE_NUMBER = new ConfigValue.Builder<>(() -> 7)
			.with(new Bounds.Int(0, 10))
			.build();

	public static final ConfigValue<String> MY_FAVORITE_FRUIT = new ConfigValue.Builder<>(() -> "Strawberry")
			.with(DataType.COMMENT, "So much delicious flavor!")
			.with(DataTypes.SYNC_TYPE, SyncType.INFO)
			.build();

	public static final ConfigValue<Color> MY_FAVORITE_COLOR = new ConfigValue.Builder<>(() -> new Color(0xFFFFFFFF))
			.with(DataTypes.SYNC_TYPE, SyncType.P2P)
			.build();

	@Override
	public @NotNull ConfigSerializer getSerializer() {
		return CustomPropertiesSerializer.INSTANCE;
	}

	@Override
	public @NotNull SaveType getSaveType() {
		return FabricSaveTypes.USER;
	}

	@Override
	public void addConfigValues(@NotNull ConfigValueCollector collector) {
		collector.addConfigValue(MY_FAVORITE_NUMBER, "favorite_number");
		collector.addConfigValue(MY_FAVORITE_FRUIT, "favorite_fruit");
		collector.addConfigValue(MY_FAVORITE_COLOR, "favorite_color");
	}

	@Override
	public void addConfigData(@NotNull DataCollector collector) {
		collector.add(DataType.COMMENT, "This is a comment");
		collector.add(DataType.COMMENT, "This is another one");
		collector.add(DataType.COMMENT, "This is a third comment");
	}

	private static class CustomPropertiesSerializer extends PropertiesSerializer {
		private static ConfigSerializer INSTANCE = new CustomPropertiesSerializer();

		CustomPropertiesSerializer() {
			super();
			this.addSerializer(Color.class, new ColorSerializer());
		}
	}

	private static class ColorSerializer implements PropertiesSerializer.ValueSerializer<Color> {
		@Override
		public String serialize(Color value) {
			return Integer.toString(value.value, 16);
		}

		@Override
		public Color deserialize(String representation) {
			return new Color(Integer.parseInt(representation, 16));
		}
	}
}
