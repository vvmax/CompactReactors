package dev.compactreactors;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderFlat;
public class PocketProvider extends WorldProvider {
 @Override protected void registerWorldChunkManager() { worldChunkMgr=new WorldChunkManagerHell(BiomeGenBase.plains,0); hasNoSky=true; }
 @Override public IChunkProvider createChunkGenerator() { return new ChunkProviderFlat(worldObj,worldObj.getSeed(),false,"2;0;1;"); }
 @Override public String getDimensionName() { return "Compact Reactors Service"; }
 @Override public boolean canRespawnHere() { return false; }
 @Override public boolean canCoordinateBeSpawn(int x,int z) { return false; }
 @Override public boolean isSurfaceWorld() { return false; }
 @Override public boolean canDoRainSnowIce(net.minecraft.world.chunk.Chunk chunk) { return false; }
}
