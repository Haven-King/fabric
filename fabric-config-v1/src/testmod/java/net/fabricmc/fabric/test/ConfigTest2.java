package net.fabricmc.fabric.test;

import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.SaveType;
import net.fabricmc.loader.api.config.serialization.PropertiesSerializer;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ConfigValueCollector;
import net.fabricmc.loader.api.entrypoint.ConfigInitializer;
import org.jetbrains.annotations.NotNull;

public class ConfigTest2 implements ConfigInitializer {
	public static final ConfigValue<Integer> MY_FAVORITE_NUMBER = new ConfigValue.Builder<>(() -> 7)
			.with(new Bounds.Int(0, 10))
			.build();

	public static final ConfigValue<String> MY_FAVORITE_FRUIT = new ConfigValue.Builder<>(() -> "Strawberry")
			.build();

	@Override
	public @NotNull ConfigSerializer getSerializer() {
		return PropertiesSerializer.INSTANCE;
	}

	@Override
	public @NotNull SaveType getSaveType() {
		return FabricSaveTypes.USER;
	}

	@Override
	public @NotNull String getName() {
		return "config2";
	}

	@Override
	public void addConfigValues(@NotNull ConfigValueCollector collector) {
		collector.addConfigValue(MY_FAVORITE_NUMBER, "favorite_number");
		collector.addConfigValue(MY_FAVORITE_FRUIT, "favorite_fruit");
	}
}
