package dev.compactreactors;
import java.lang.reflect.Field;
import java.util.*;
import erogenousbeef.bigreactors.api.data.ReactorInteriorData;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.common.multiblock.helpers.RadiationHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fluids.FluidRegistry;
public final class CoolantCatalog {
 private CoolantCatalog() {}
 // BR 0.4.3A exposes individual getters, but no public registry iterator.
 // Copy maps read-only to preserve late registrations; only installed entries are sent to clients.
 static Map<String,ReactorInteriorData> registry(boolean fluids) {
  try {
   Field f=ReactorInterior.class.getDeclaredField(fluids?"_reactorModeratorFluids":"_reactorModeratorBlocks");
   f.setAccessible(true);
   Map<?,?> original=(Map<?,?>)f.get(null);
   Map<String,ReactorInteriorData> copy=new TreeMap<String,ReactorInteriorData>();
   for(Map.Entry<?,?> e:original.entrySet()) if(e.getKey() instanceof String && e.getValue() instanceof ReactorInteriorData) {
    ReactorInteriorData d=(ReactorInteriorData)e.getValue();
    copy.put((String)e.getKey(),new ReactorInteriorData(d.absorption,d.heatEfficiency,d.moderation,d.heatConductivity));
   }
   return copy;
  } catch(ReflectiveOperationException e) { throw new IllegalStateException("Cannot read BigReactors 0.4.3A moderator registry",e); }
 }
 public static List<CoolantEntry> snapshot() {
  List<CoolantEntry> list=new ArrayList<CoolantEntry>();
  list.add(entry(CoolantEntry.AIR,"air",RadiationHelper.airData,true,true,null));
  for(Map.Entry<String,ReactorInteriorData> e:registry(false).entrySet()) {
   ItemStack icon=null;
   for(ItemStack sample:OreDictionary.getOres(e.getKey())) if(sample!=null && sample.getItem() instanceof ItemBlock) {
    icon=new ItemStack(sample.getItem(),1,sample.getItemDamage()==OreDictionary.WILDCARD_VALUE?0:sample.getItemDamage()); break;
   }
   list.add(entry(CoolantEntry.BLOCK,e.getKey(),e.getValue(),icon!=null,icon!=null && Parts.interior(icon),icon));
  }
  for(Map.Entry<String,ReactorInteriorData> e:registry(true).entrySet()) {
   net.minecraftforge.fluids.Fluid fluid=FluidRegistry.getFluid(e.getKey());
   list.add(entry(CoolantEntry.FLUID,e.getKey(),e.getValue(),fluid!=null,LiquidCoolants.supported(fluid),LiquidCoolants.sampleContainer(fluid)));
  }
  return installedOnly(list);
 }
 static List<CoolantEntry> installedOnly(List<CoolantEntry> entries) {
  List<CoolantEntry> installed=new ArrayList<CoolantEntry>();
  for(CoolantEntry entry:entries) if(entry.available) installed.add(entry);
  return installed;
 }
 private static CoolantEntry entry(int type,String key,ReactorInteriorData d,boolean available,boolean usable,ItemStack icon) {
  return new CoolantEntry(type,key,d.absorption,d.heatEfficiency,d.moderation,d.heatConductivity,available,usable,icon);
 }
}
