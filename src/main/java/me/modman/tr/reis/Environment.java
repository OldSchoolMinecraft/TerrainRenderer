package me.modman.tr.reis;

import me.modman.tr.chunk.Chunk;
import me.modman.tr.minecraft.ColorizerFoliage;
import me.modman.tr.minecraft.ColorizerGrass;
import me.modman.tr.minecraft.NoiseGeneratorOctaves2;

import java.util.Random;

public class Environment
{
	private static final int foliageColorPine = ColorizerFoliage.getFoliageColorPine();
	private static final int foliageColorBirch = ColorizerFoliage.getFoliageColorBirch();
	private static long randomSeed;
	private static NoiseGeneratorOctaves2 temperatureGen;
	private static NoiseGeneratorOctaves2 humidityGen;
	private static NoiseGeneratorOctaves2 noiseGen;
	private static Environment[] envCache = new Environment[262144];
	private int x;
	private int z;
	private int grassColor;
	private int foliageColor;

	private boolean isLocation(int x, int z) {
		return this.x == x && this.z == z;
	}

	private void set(int x, int z, double temperature, double humidity) {
		this.x = x;
		this.z = z;
		this.grassColor = ColorizerGrass.getGrassColor(temperature, humidity);
		this.foliageColor = ColorizerFoliage.getFoliageColor(temperature, humidity);
	}

	public int getGrassColor() {
		return this.grassColor;
	}

	public int getFoliageColor() {
		return this.foliageColor;
	}

	public int getFoliageColorPine() {
		return foliageColorPine;
	}

	public int getFoliageColorBirch() {
		return foliageColorBirch;
	}

	public static Environment getEnvironment(int x, int z) {
		int ptr = (x & 511) << 9 | z & 511;
		Environment env = envCache[ptr];
		if(!env.isLocation(x, z)) {
			calcEnvironment(x, z, env);
		}

		return env;
	}

	public static Environment getEnvironment(Chunk chunk, int x, int z) {
		return getEnvironment(chunk.getChunkX() * 16 + x, chunk.getChunkZ() * 16 + z);
	}

	private static void calcEnvironment(int x, int z, Environment environment) {
		double noise = noiseGen.func_4112_a(null, x, z, 1, 1, 0.25D, 0.25D, 0.5882352941176471D)[0] * 1.1D + 0.5D;
		double temperature = temperatureGen.func_4112_a(null, x, z, 1, 1, 0.025F, 0.025F, 0.25D)[0];
		temperature = (temperature * 0.15D + 0.7D) * 0.99D + noise * 0.01D;
		temperature = 1.0D - (1.0D - temperature) * (1.0D - temperature);
		temperature = temperature < 0.0D ? 0.0D : (temperature <= 1.0D ? temperature : 1.0D);
		double humidity = humidityGen.func_4112_a(null, x, z, 1, 1, 0.05F, 0.05F, 1.0D / 3.0D)[0];
		humidity = (humidity * 0.15D + 0.5D) * 0.998D + noise * 0.002D;
		humidity = humidity < 0.0D ? 0.0D : (humidity <= 1.0D ? humidity : 1.0D);
		environment.set(x, z, temperature, humidity);
	}

	public static void setWorldSeed(long seed) {
		if(randomSeed != seed) {
			randomSeed = seed;
			temperatureGen = new NoiseGeneratorOctaves2(new Random(seed * 9871L), 4);
			humidityGen = new NoiseGeneratorOctaves2(new Random(seed * 39811L), 4);
			noiseGen = new NoiseGeneratorOctaves2(new Random(seed * 543321L), 2);

			for(int i = 0; i < envCache.length; ++i) {
				envCache[i].x = Integer.MIN_VALUE;
				envCache[i].z = Integer.MIN_VALUE;
			}

		}
	}

	static {
		for(int i = 0; i < envCache.length; ++i) {
			envCache[i] = new Environment();
		}

	}
}
