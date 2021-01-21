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

package net.fabricmc.fabric.test.config;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.config.v1.FabricSaveTypes;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.SaveType;
import net.fabricmc.loader.api.config.serialization.PropertiesSerializer;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ConfigValueCollector;
import net.fabricmc.loader.api.entrypoint.ConfigInitializer;

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
