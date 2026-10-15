package dev.compactreactors;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
public class ReactorBlock extends BlockContainer {
 private final boolean advanced;
 public ReactorBlock() { this(false); }
 public ReactorBlock(boolean advanced) { super(Material.iron); this.advanced=advanced; setBlockName(advanced?"compactreactors.advanced_reactor":"compactreactors.reactor"); setBlockTextureName("compactreactors:reactor"); setHardness(advanced?6:4); setResistance(20); setStepSound(soundTypeMetal); setCreativeTab(CreativeTabs.tabRedstone); setHarvestLevel("pickaxe",1); }
 @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 private net.minecraft.util.IIcon[] modelIcons;
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister registry) {
  String[] names={"frame","side","front","top","core","base"}; modelIcons=new net.minecraft.util.IIcon[names.length];
  for(int i=0;i<names.length;i++) modelIcons[i]=registry.registerIcon("compactreactors:"+(advanced?"advanced_reactor_":"reactor_")+names[i]);
  blockIcon=modelIcons[ReactorShape.SIDE];
 }
 @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public net.minecraft.util.IIcon modelIcon(int material) { return modelIcons[material]; }
 @Override @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
 public net.minecraft.util.IIcon getIcon(int side,int meta) { return modelIcons[side==1?ReactorShape.TOP:side==0?ReactorShape.BASE:side==(meta>=2 && meta<=5?meta:2)?ReactorShape.FRONT:ReactorShape.SIDE]; }
 @Override public int getRenderType() { return CompactReactors.renderType; }
 @Override public boolean isOpaqueCube() { return false; }
 @Override public boolean renderAsNormalBlock() { return false; }
 @Override public void onBlockPlacedBy(World world,int x,int y,int z,net.minecraft.entity.EntityLivingBase placer,ItemStack stack) {
  int rotation=net.minecraft.util.MathHelper.floor_double(placer.rotationYaw*4.0/360.0+.5)&3;
  world.setBlockMetadataWithNotify(x,y,z,new int[]{2,5,3,4}[rotation],2);
 }
 @Override public TileEntity createNewTileEntity(World w,int meta) { return advanced?new AdvancedReactorTile():new ReactorTile(); }
 @Override public boolean onBlockActivated(World w,int x,int y,int z,EntityPlayer p,int side,float a,float b,float c) {
  if(!w.isRemote) p.openGui(CompactReactors.instance,0,w,x,y,z); return true;
 }
 @Override public void breakBlock(World w,int x,int y,int z,Block b,int meta) {
  TileEntity te=w.getTileEntity(x,y,z);
  if(!w.isRemote && te instanceof ReactorTile) {
   ReactorTile t=(ReactorTile)te; t.removed();
   for(int i:t.occupiedSlots()) { ItemStack s=t.getStackInSlot(i); if(s!=null) { w.spawnEntityInWorld(new EntityItem(w,x+.5,y+.5,z+.5,s.copy())); t.setInventorySlotContents(i,null); } }
  }
  super.breakBlock(w,x,y,z,b,meta);
 }
}
