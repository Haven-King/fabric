package net.fabricmc.fabric.api.config.v1;

import net.fabricmc.loader.api.config.ConfigDefinition;
import net.fabricmc.loader.api.config.ConfigSerializer;
import net.fabricmc.loader.api.config.serialization.ReflectionUtil;
import net.fabricmc.loader.api.config.value.ConfigValue;
import net.fabricmc.loader.api.config.value.ValueContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractTreeSerializer<E, O extends E> implements ConfigSerializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings("rawtypes")
	private final HashMap<Class<?>, ValueSerializer> serializableTypes = new HashMap<>();
	private final HashMap<Class<?>, Function<ConfigValue<?>, ValueSerializer<E, ?, ?>>> typeDependentSerializers = new HashMap<>();

	protected final <T> void addSerializer(Class<T> valueClass, ValueSerializer<E, ?, T> valueSerializer) {
		this.serializableTypes.putIfAbsent(valueClass, valueSerializer);

		//noinspection unchecked
		valueClass = (Class<T>) ReflectionUtil.getClass(valueClass);

		for (Class<?> clazz : ReflectionUtil.getClasses(valueClass)) {
			this.serializableTypes.putIfAbsent(clazz, valueSerializer);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected final <T> void addSerializer(Class<T> valueClass, Function<ConfigValue<T>, ValueSerializer<E, ?, T>> serializerBuilder) {
		this.typeDependentSerializers.putIfAbsent(valueClass, (Function) serializerBuilder);
	}

	@SuppressWarnings("unchecked")
	protected final <V> ValueSerializer<E, ?, V> getSerializer(ConfigValue<V> configValue) {
		V defaultValue = configValue.getDefaultValue();

		if (typeDependentSerializers.containsKey(defaultValue.getClass())) {
			return (ValueSerializer<E, ?, V>) typeDependentSerializers.get(defaultValue.getClass()).apply(configValue);
		}

		return (ValueSerializer<E, ?, V>) this.getSerializer(defaultValue.getClass());
	}

	@SuppressWarnings("unchecked")
	protected final <V> ValueSerializer<E, ?, V> getSerializer(Supplier<V> defaultValue) {
		return this.getSerializer((Class<V>) defaultValue.get().getClass());
	}

	@SuppressWarnings("unchecked")
	protected final <V> ValueSerializer<E, ?, V> getSerializer(Class<V> valueClass) {
		return (ValueSerializer<E, ?, V>) serializableTypes.get(valueClass);
	}

	@Override
	public void serialize(ConfigDefinition configDefinition, ValueContainer valueContainer) throws IOException {
		O root = this.start();

		for (ConfigValue<?> value : configDefinition) {
			doNested(root, value, (object, key) -> {
						Object v = value.get();
						this.add(object, key, this.getSerializer(value).serializeValue(v), this.getComment(value));
					}
			);
		}

		Path file = this.getPath(configDefinition, valueContainer);

		if (!Files.exists(file.getParent())) {
			Files.createDirectories(file.getParent());
		}

		this.write(root, file);
	}

	@Override
	public void deserialize(ConfigDefinition configDefinition, ValueContainer valueContainer) throws IOException {
		Path path = this.getPath(configDefinition, valueContainer);

		if (Files.exists(path)) {
			this.deserialize(configDefinition, Files.newInputStream(path), valueContainer);
		}
	}

	@Override
	public void deserialize(ConfigDefinition configDefinition, InputStream inputStream, ValueContainer valueContainer) throws IOException {
		O root = this.read(inputStream);

		for (ConfigValue value : configDefinition) {
			doNested(root, value, (object, key) -> {
				ValueSerializer<E, ?, ?> serializer = this.getSerializer(value);
				E representation = this.get(object, key);
				if (representation != null) {
					value.set(serializer.deserialize(representation));
				} else {
					LOGGER.info("Missing key: " + value.getKey());
				}
			});
		}
	}

	private void doNested(O root, ConfigValue<?> value, Consumer<O, String> consumer) {
		O object = root;
		String[] path = value.getKey().getPath();

		for (int i = 0; i < path.length; ++i)  {
			if (i == path.length - 1) {
				consumer.consume(object, path[i]);
			} else {
				if (this.get(object, path[i]) == null) {
					object = this.add(object, path[i], this.start(), null);
				} else {
					object = this.get(object, path[i]);
				}
			}
		}
	}

	protected abstract O start();

	protected abstract <R extends E> R add(O object, String key, R representation, @Nullable String comment);

	protected abstract <V> V get(O object, String s);

	protected abstract O read(InputStream in) throws IOException;

	protected abstract void write(O root, Path file) throws IOException;

	protected abstract @Nullable String getComment(ConfigValue<?> configValue);

	private interface Consumer<T1, T2> {
		void consume(T1 t1, T2 t2);
	}

	public interface ValueSerializer<E, R extends E, V> {
		R serialize(V value);

		@SuppressWarnings("unchecked")
		default R serializeValue(Object value) {
			return this.serialize((V) value);
		}

		V deserialize(E representation);

	}
}
