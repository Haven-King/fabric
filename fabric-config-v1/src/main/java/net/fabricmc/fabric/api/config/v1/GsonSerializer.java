package net.fabricmc.fabric.api.config.v1;

import com.google.gson.*;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ValueContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class GsonSerializer extends AbstractTreeSerializer<JsonElement, JsonObject> {
	public static ConfigSerializer DEFAULT = new GsonSerializer(new GsonBuilder().setPrettyPrinting().create());

	private final Gson gson;

	public GsonSerializer(Gson gson) {
		this.gson = gson;

		this.addSerializer(Boolean.class, BooleanSerializer.INSTANCE);
		this.addSerializer(Integer.class, IntSerializer.INSTANCE);
		this.addSerializer(Long.class, LongSerializer.INSTANCE);
		this.addSerializer(String.class, StringSerializer.INSTANCE);
		this.addSerializer(Float.class, FloatSerializer.INSTANCE);
		this.addSerializer(Double.class, DoubleSerializer.INSTANCE);
	}

	@Override
	public @Nullable SemanticVersion getVersion(ConfigDefinition configDefinition, ValueContainer valueContainer) throws Exception {
		Reader reader = new InputStreamReader(Files.newInputStream(this.getPath(configDefinition, valueContainer)));
		JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
		reader.close();

		return SemanticVersion.parse(object.get("version").getAsString());
	}

	@Override
	public @NotNull String getExtension() {
		return "json";
	}

	@Override
	protected JsonObject start() {
		return new JsonObject();
	}

	@Override
	protected <R extends JsonElement> R add(JsonObject object, String key, R representation, @Nullable String comment) {
		object.add(key, representation);
		return representation;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <V> V get(JsonObject object, String s) {
		return (V) object.get(s);
	}

	@Override
	protected JsonObject read(InputStream in) throws IOException {
		Reader reader = new InputStreamReader(in);
		JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
		reader.close();

		return object;
	}

	@Override
	protected void write(JsonObject root, Path file) throws IOException {
		Writer writer = Files.newBufferedWriter(file);
		this.gson.toJson(root, writer);
		writer.flush();
		writer.close();
	}

	@Override
	protected @Nullable String getComment(ConfigValue<?> configValue) {
		return null;
	}

	interface GsonValueSerializer<R extends JsonElement, V> extends ValueSerializer<JsonElement, R, V> {
	}

	private static class BooleanSerializer implements GsonValueSerializer<JsonPrimitive, Boolean> {
		static BooleanSerializer INSTANCE = new BooleanSerializer();

		@Override
		public JsonPrimitive serialize(Boolean value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Boolean deserialize(JsonElement representation) {
			return representation.getAsBoolean();
		}
	}

	private static class IntSerializer implements GsonValueSerializer<JsonPrimitive, Integer> {
		static IntSerializer INSTANCE = new IntSerializer();

		@Override
		public JsonPrimitive serialize(Integer value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Integer deserialize(JsonElement representation) {
			return representation.getAsInt();
		}
	}

	private static class LongSerializer implements GsonValueSerializer<JsonPrimitive, Long> {
		static LongSerializer INSTANCE = new LongSerializer();

		@Override
		public JsonPrimitive serialize(Long value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Long deserialize(JsonElement representation) {
			return representation.getAsLong();
		}
	}

	private static class StringSerializer implements GsonValueSerializer<JsonPrimitive, String> {
		static StringSerializer INSTANCE = new StringSerializer();

		@Override
		public JsonPrimitive serialize(String value) {
			return new JsonPrimitive(value);
		}

		@Override
		public String deserialize(JsonElement representation) {
			return representation.getAsString();
		}
	}

	private static class FloatSerializer implements GsonValueSerializer<JsonPrimitive, Float> {
		public static final FloatSerializer INSTANCE = new FloatSerializer();

		@Override
		public JsonPrimitive serialize(Float value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Float deserialize(JsonElement representation) {
			return representation.getAsFloat();
		}
	}

	private static class DoubleSerializer implements GsonValueSerializer<JsonPrimitive, Double> {
		public static final DoubleSerializer INSTANCE = new DoubleSerializer();

		@Override
		public JsonPrimitive serialize(Double value) {
			return new JsonPrimitive(value);
		}

		@Override
		public Double deserialize(JsonElement representation) {
			return representation.getAsDouble();
		}
	}
}
