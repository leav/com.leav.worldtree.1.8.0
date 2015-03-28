package com.leav.worldtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;



public class BlockCache {
	
	private Map<BlockPos, IBlockState> cache;
	private ArrayList<HashMap<BlockPos, IBlockState>> memory;
	private int memoryIndex; 
	
	public BlockCache() {
		cache = new HashMap<BlockPos, IBlockState>();
		memory = new ArrayList<HashMap<BlockPos, IBlockState>>();
		memoryIndex = 0;
	}
	
	public IBlockState get(int x, int y, int z) {
		return cache.get(new BlockPos(x, y, z));
	}
	
	public IBlockState get(BlockPos pos) {
		return cache.get(pos);
	}
	
	public void put(int x, int y, int z, IBlockState block) {
		cache.put(new BlockPos(x, y, z), block);
	}
	
	public void put(BlockPos pos, IBlockState block) {
		cache.put(pos, block);
	}
	
	public void putArray(ArrayList<int[]> coords, IBlockState block) {
		for (int[] coord : coords) {
			put(coord[0], coord[1], coord[2], block);
		}
	}
	
	public void put(Collection<BlockPos> poses, IBlockState block) {
		for (BlockPos pos : poses) {
			put(pos, block);
		}
	}
	
	public void clear() {
		cache.clear();
	}
	
	public void apply(World world) {
		HashMap<BlockPos, IBlockState> lastBlocks = new HashMap<BlockPos, IBlockState>(); 
		for (Map.Entry<BlockPos, IBlockState> entry : cache.entrySet()) {
			BlockPos pos = entry.getKey();
			IBlockState oldBlock = world.getBlockState(pos);
			lastBlocks.put(pos, oldBlock);
			world.setBlockState(pos, entry.getValue());
		}
		recordMemory(lastBlocks);
		clear();
	}
	
	public void apply(World world, boolean record) {
		if (record) {
			HashMap<BlockPos, IBlockState> lastBlocks = new HashMap<BlockPos, IBlockState>(); 
			for (Map.Entry<BlockPos, IBlockState> entry : cache.entrySet()) {
				BlockPos pos = entry.getKey();
				IBlockState oldBlock = world.getBlockState(pos);
				lastBlocks.put(pos, oldBlock);
				world.setBlockState(pos, entry.getValue());
			}
			recordMemory(lastBlocks);
			clear();			
		}
		else {
			for (Map.Entry<BlockPos, IBlockState> entry : cache.entrySet()) {
				world.setBlockState(entry.getKey(), entry.getValue());
			}
			clear();					
		}

	}
	
	public void recordMemory(HashMap<BlockPos, IBlockState> blocks) {
		for (int i = memoryIndex + 1; i < memory.size(); i++) {
			memory.remove(i);
		}
		memory.add(blocks);
		memoryIndex = memory.size() - 1;
	}
	
	public void revert(World world) {
		if (memoryIndex < 0) {
			return;
		}
		cache = (Map<BlockPos, IBlockState>) memory.get(memoryIndex).clone();
		apply(world);
		memoryIndex--;
	}
}
