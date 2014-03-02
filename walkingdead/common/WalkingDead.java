//  
//  =====GPL=============================================================
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; version 2 dated June, 1991.
// 
//  This program is distributed in the hope that it will be useful, 
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
// 
//  You should have received a copy of the GNU General Public License
//  along with this program;  if not, write to the Free Software
//  Foundation, Inc., 675 Mass Ave., Cambridge, MA 02139, USA.
//  =====================================================================
//
//
// Copyright 2011-2014 Michael Sheppard (crackedEgg)
//
package com.walkingdead.common;

import cpw.mods.fml.common.FMLCommonHandler;
import java.util.LinkedList;

import net.minecraft.entity.EnumCreatureType;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import net.minecraft.init.Items;
import net.minecraftforge.common.BiomeDictionary.Type;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Configuration;

@Mod(
		modid = WalkingDead.modid,
		name = WalkingDead.name,
		version = WalkingDead.version
)

public class WalkingDead {

	public String getVersion()
	{
		return WalkingDead.version;
	}

	@Instance
	public static WalkingDead instance;

	public static final String version = "1.7.2";
	public static final String modid = "walkingdeadmod";
	public static final String name = "WalkingDead Mod";

	private int walkerSpawnProb;
	private int walkerSpawns;
	private boolean spawnCreepers;
	private boolean spawnSkeletons;
	private boolean spawnEnderman;
	private boolean spawnSpiders;
	private boolean spawnSlime;
	private boolean randomSkins;
	private boolean doorBusting;

	
	@SidedProxy(
			clientSide = "com.walkingdead.client.ClientProxyWalkingDead",
			serverSide = "com.walkingdead.common.CommonProxyWalkingDead"
	)

	public static CommonProxyWalkingDead proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		String generalComments = WalkingDead.name + " Config\nMichael Sheppard (crackedEgg)\n"
				+ " For Minecraft Version " + WalkingDead.version + "\n";
		String spawnProbComment = "walkerSpawnProb adjust to probability of walkers spawning,\n"
				+ "although the custom spawning most likely overrides this. the higher the\n"
				+ "the number the more likely walkers will spawn.";
		String walkerComment = "walkerSpawns adjusts the number of walkers spawned, play\n"
				+ "with it to see what you like. The higher the number the more\n"
				+ "walkers will spawn.";
		String creeperComment = "creeperSpawns, set to false to disable creeper spawning, set to true\n"
				+ "if you want to spawn creepers";
		String skeletonComment = "skeletonSpawns, set to false to disable skeleton spawning, set to true\n"
				+ "if you want to spawn skeletons";
		String endermanComment = "endermanSpawns, set to false to disable enderman spawning, set to true\n"
				+ "if you want to spawn enderman";
		String spiderComment = "spiderSpawns, set to false to disable spider spawning, set to true\n"
				+ "if you want to spawn spiders";
		String slimeComment = "slimeSpawns, set to false to disable slime spawning, set to true\n"
				+ "if you want to spawn slimes";
		String doorBustingComment = "doorBusting, set to true to have walkers try to break down doors,\n"
				+ "otherwise set to false. It's quieter.";

		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		walkerSpawnProb = config.get(Configuration.CATEGORY_GENERAL, "walkerSpawnProb", 10, spawnProbComment).getInt();
		walkerSpawns = config.get(Configuration.CATEGORY_GENERAL, "walkerSpawns", 60, walkerComment).getInt();
		spawnCreepers = config.get(Configuration.CATEGORY_GENERAL, "spawnCreepers", false, creeperComment).getBoolean(false);
		spawnSkeletons = config.get(Configuration.CATEGORY_GENERAL, "spawnSkeletons", false, skeletonComment).getBoolean(false);
		spawnEnderman = config.get(Configuration.CATEGORY_GENERAL, "spawnEnderman", false, endermanComment).getBoolean(false);
		spawnSpiders = config.get(Configuration.CATEGORY_GENERAL, "spawnSpiders", true, spiderComment).getBoolean(true);
		spawnSlime = config.get(Configuration.CATEGORY_GENERAL, "spawnSlime", false, slimeComment).getBoolean(false);
		doorBusting = config.get(Configuration.CATEGORY_GENERAL, "doorBusting", false, doorBustingComment).getBoolean(false);

		config.addCustomCategoryComment(Configuration.CATEGORY_GENERAL, generalComments);

		config.save();

		int id = EntityRegistry.findGlobalUniqueEntityId();
		EntityRegistry.registerGlobalEntityID(EntityWalkingDead.class, "WalkingDead", id, 0x00AFAF, 0x799C45);

		proxy.registerRenderers();
	}

	@EventHandler
	public void Init(FMLInitializationEvent evt)
	{
		// placing this function here should allow the walkers to spawn in biomes
		// created by other mods provided those mods are loaded before this one.
		proxy.print("*** Scanning for available biomes");
		BiomeGenBase[] biomes = getBiomeList();

		EntityRegistry.addSpawn(EntityWalkingDead.class, walkerSpawnProb, 2, 10, EnumCreatureType.monster, biomes);

		// walkers should spawn in dungeon spawners
		DungeonHooks.addDungeonMob("WalkingDead", 200);
		// add steel swords to the loot. you may need these.
		ChestGenHooks.addItem(ChestGenHooks.DUNGEON_CHEST, new WeightedRandomChestContent(new ItemStack(Items.iron_sword), 1, 1, 4));
		
		// remove zombie spawning, we are replacing zombies with walkers
		EntityRegistry.removeSpawn(EntityZombie.class, EnumCreatureType.monster, biomes);
		DungeonHooks.removeDungeonMob("Zombie");

		// optionally remove creeper, skeleton, enderman, spaiders and slime spawns for these biomes
		if (!spawnCreepers) {
			EntityRegistry.removeSpawn(EntityCreeper.class, EnumCreatureType.monster, biomes);
			proxy.print("*** Removing creeper spawns");
		}
		if (!spawnSkeletons) {
			EntityRegistry.removeSpawn(EntitySkeleton.class, EnumCreatureType.monster, biomes);
			DungeonHooks.removeDungeonMob("Skeleton");
			proxy.print("*** Removing skeleton spawns and dungeon spawners");
		}
		if (!spawnEnderman) {
			EntityRegistry.removeSpawn(EntityEnderman.class, EnumCreatureType.monster, biomes);
			proxy.print("*** Removing enderman spawns");
		}
		if (!spawnSpiders) {
			EntityRegistry.removeSpawn(EntitySpider.class, EnumCreatureType.monster, biomes);
			DungeonHooks.removeDungeonMob("Spider");
			proxy.print("*** Removing spider spawns and dungeon spawners");
		}
		if (!spawnSlime) {
			EntityRegistry.removeSpawn(EntitySlime.class, EnumCreatureType.monster, biomes);
			proxy.print("*** Removing slime spawns");
		}

		FMLCommonHandler.instance().bus().register(new WorldTickHandler());
	}

	// This function should get all biomes that are derived from BiomeGenBase,
	// even those from other mods.
	public BiomeGenBase[] getBiomeList()
	{
		LinkedList<BiomeGenBase> linkedlist = new LinkedList<BiomeGenBase>();
		
		// Add some new (1.7) biomes that are not added in Forge BiomeDictionary
		BiomeDictionary.registerBiomeType(BiomeGenBase.mesa, Type.DESERT);
		BiomeDictionary.registerBiomeType(BiomeGenBase.mesaPlateau, Type.DESERT);
		BiomeDictionary.registerBiomeType(BiomeGenBase.mesaPlateau_F, Type.DESERT);
		BiomeDictionary.registerBiomeType(BiomeGenBase.savanna, Type.PLAINS);
		BiomeDictionary.registerBiomeType(BiomeGenBase.savannaPlateau, Type.PLAINS);
		BiomeDictionary.registerBiomeType(BiomeGenBase.birchForest, Type.FOREST);
		BiomeDictionary.registerBiomeType(BiomeGenBase.birchForestHills, Type.FOREST, Type.HILLS);
		BiomeDictionary.registerBiomeType(BiomeGenBase.roofedForest, Type.FOREST);
		BiomeDictionary.registerBiomeType(BiomeGenBase.stoneBeach, Type.BEACH);
		
		Type[] t = {Type.FOREST, Type.PLAINS, Type.MOUNTAIN, Type.HILLS, Type.SWAMP, Type.MAGICAL,
			Type.DESERT, Type.FROZEN, Type.JUNGLE, Type.WASTELAND, Type.BEACH, Type.MUSHROOM};

		for (Type type : t) {
			BiomeGenBase[] biomes = BiomeDictionary.getBiomesForType(type);
			for (BiomeGenBase bgb : biomes) {
				if (!linkedlist.contains(bgb)) {
                    proxy.print(" >>> Adding " + bgb.biomeName + " for spawning");
					linkedlist.add(bgb);
				}
			}
		}
		return (BiomeGenBase[]) linkedlist.toArray(new BiomeGenBase[0]);
	}

	public int getWalkerSpawns()
	{
		return walkerSpawns;
	}

	public boolean getRandomSkins()
	{
		return randomSkins;
	}

	public boolean getDoorBusting()
	{
		return doorBusting;
	}

}