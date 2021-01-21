package net.fabricmc.fabric.test;

import net.fabricmc.loader.api.config.data.Constraint;

public abstract class Bounds<T extends Integer> extends Constraint<T> {
	protected final T min;
	protected final T max;

	protected Bounds(String namespace, String name, T min, T max) {
		super(namespace, name);
		this.min = min;
		this.max = max;
	}

	@Override
	public String toString() {
		return super.toString() + "[" + this.min + ", " + this.max + "]";
	}

	public static class Int extends Bounds<Integer> {
		public Int(Integer min, Integer max) {
			super("fabric", "bounds/int", min, max);
		}

		@Override
		public boolean passes(Integer integer) {
			return integer >= this.min && integer <= this.max;
		}
	}
}
