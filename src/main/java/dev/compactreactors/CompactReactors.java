package dev.compactreactors;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
@Mod(modid=CompactReactors.ID, name="CompactReactors", version="0.4.1", dependencies="required-after:BigReactors@[0.4.3A];required-after:CoFHCore", acceptedMinecraftVersions="[1.7.10]")
public class CompactReactors {
 public static final String ID="compactreactors";
 @Mod.Instance(ID) public static CompactReactors instance;
 public static Block reactor,advancedReactor;
 public static ExpansionModule expansion1,expansion2;
 public static CoolantCell coolantCell;
 public static int renderType=0;
 @cpw.mods.fml.common.SidedProxy(clientSide="dev.compactreactors.ClientProxy",serverSide="dev.compactreactors.CommonProxy")
 public static CommonProxy proxy;
 public static int dimension;
 public static int advancedMaxWidth=32,advancedMaxHeight=32,advancedMaxDepth=32;
 public static void readConfiguration(net.minecraftforge.common.config.Configuration c) {
  c.load(); dimension=c.get("general","serviceDimension",983,"Reserved void dimension; keep this ID stable for existing worlds.").getInt();
  advancedMaxWidth=c.getInt("maxWidth","advanced",32,5,64,"Maximum advanced reactor X size including casing; also bounded by BigReactors.");
  advancedMaxHeight=c.getInt("maxHeight","advanced",32,5,64,"Maximum advanced reactor Y size including casing; also bounded by BigReactors.");
  advancedMaxDepth=c.getInt("maxDepth","advanced",32,5,64,"Maximum advanced reactor Z size including casing; also bounded by BigReactors.");
  c.save();
 }
 @EventHandler public void preInit(cpw.mods.fml.common.event.FMLPreInitializationEvent e) {
  net.minecraftforge.common.config.Configuration c=new net.minecraftforge.common.config.Configuration(e.getSuggestedConfigurationFile());
  readConfiguration(c);
  if(net.minecraftforge.common.DimensionManager.isDimensionRegistered(dimension)) throw new IllegalStateException("CompactReactors dimension ID occupied: "+dimension);
  if(!net.minecraftforge.common.DimensionManager.registerProviderType(dimension,PocketProvider.class,false)) throw new IllegalStateException("CompactReactors provider ID occupied: "+dimension);
  net.minecraftforge.common.DimensionManager.registerDimension(dimension,dimension);
  net.minecraftforge.common.ForgeChunkManager.setForcedChunkLoadingCallback(this,new NativeReactor.RestoreTickets());
 }
 @EventHandler public void init(FMLInitializationEvent e) {
  CatalogNetwork.init();
  reactor=new ReactorBlock();
  GameRegistry.registerBlock(reactor,"compact_reactor");
  advancedReactor=new ReactorBlock(true);
  GameRegistry.registerBlock(advancedReactor,"advanced_compact_reactor");
  expansion1=new ExpansionModule(1); expansion2=new ExpansionModule(2);
  GameRegistry.registerItem(expansion1,"expansion_module_1");
  GameRegistry.registerItem(expansion2,"expansion_module_2");
  coolantCell=new CoolantCell(); GameRegistry.registerItem(coolantCell,"coolant_cell");
  net.minecraftforge.oredict.RecipeSorter.register(ID+":coolant_bucket",CoolantBucketRecipe.class,net.minecraftforge.oredict.RecipeSorter.Category.SHAPELESS,"after:minecraft:shapeless");
  GameRegistry.addRecipe(new CoolantBucketRecipe());
  GameRegistry.registerTileEntity(ReactorTile.class,"compactreactors:reactor");
  GameRegistry.registerTileEntity(AdvancedReactorTile.class,"compactreactors:advanced_reactor");
  NetworkRegistry.INSTANCE.registerGuiHandler(this,new ReactorGuiHandler());
  proxy.init();
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(reactor),"CCC","PDP","CFC",'C',"reactorCasing",'P',"reactorPowerTap",'D',"reactorController",'F',"reactorControlRod"));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(advancedReactor),"DED","GCG","DED",'D',net.minecraft.init.Items.diamond,'E',net.minecraft.init.Items.ender_pearl,'G',net.minecraft.init.Items.gold_ingot,'C',reactor));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(expansion1),"CRC","RDR","CRC",'C',"reactorCasing",'R',net.minecraft.init.Items.redstone,'D',net.minecraft.init.Items.diamond));
  GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(expansion2),"CGC","DMD","CGC",'C',"reactorCasing",'G',net.minecraft.init.Items.gold_ingot,'D',net.minecraft.init.Items.diamond,'M',expansion1));
 }
}
