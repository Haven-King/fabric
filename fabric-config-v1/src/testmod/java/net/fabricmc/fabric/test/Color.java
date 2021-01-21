package net.fabricmc.fabric.test;

public class Color {
	public final int value;
	public final int a;
	public final int r;
	public final int g;
	public final int b;

	public Color(int value) {
		this.value = value;
		this.a = (value >> 24) & 0xFF;
		this.r = (value >> 16) & 0xFF;
		this.g = (value >> 8) & 0xFF;
		this.b = value & 0xFF;
	}
}
