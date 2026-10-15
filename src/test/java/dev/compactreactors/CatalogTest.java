package dev.compactreactors;
import java.util.*;
import io.netty.buffer.Unpooled;
import io.netty.buffer.ByteBuf;
import erogenousbeef.bigreactors.api.registry.ReactorInterior;
import erogenousbeef.bigreactors.api.data.ReactorInteriorData;
public class CatalogTest {
 private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
 private static CatalogNetwork.Packet roundTrip(CatalogNetwork.Packet p) {
  ByteBuf b=Unpooled.buffer(); p.toBytes(b); CatalogNetwork.Packet result=new CatalogNetwork.Packet(); result.fromBytes(b);
  check(b.readableBytes()==0,"unconsumed payload"); b.release(); return result;
 }
 public static void main(String[] args) {
  ReactorInterior.registerBlock("blockCatalogCustom",.12f,.34f,5.6f,7.8f);
  ReactorInterior.registerFluid("catalogCustomFluid",.9f,.8f,7f,6f);
  Map<String,ReactorInteriorData> blocks=CoolantCatalog.registry(false),fluids=CoolantCatalog.registry(true);
  check(blocks.containsKey("blockCatalogCustom") && fluids.containsKey("catalogCustomFluid"),"custom registrations missing");
  check(blocks.get("blockCatalogCustom").heatConductivity==7.8f,"wrong conductivity");
  ReactorInterior.getBlockData("blockCatalogCustom").moderation=99f;
  check(blocks.get("blockCatalogCustom").moderation==5.6f,"snapshot mutated after capture");
  check(CoolantCatalog.registry(false).get("blockCatalogCustom").moderation==99f,"late changes missing");
  List<CoolantEntry> entries=new ArrayList<CoolantEntry>();
  for(int i=0;i<70;i++) entries.add(new CoolantEntry(i%3,"catalog_"+i,.12f,.34f,5.6f,7.8f,true,i%3!=2,null));
  List<CoolantEntry> mixed=new ArrayList<CoolantEntry>(entries);
  mixed.add(new CoolantEntry(CoolantEntry.FLUID,"missing_fluid",1,1,1,1,false,false,null));
  mixed.add(new CoolantEntry(CoolantEntry.BLOCK,"missing_block",1,1,1,1,false,false,null));
  List<CoolantEntry> installed=CoolantCatalog.installedOnly(mixed);
  check(installed.size()==70 && mixed.size()==72,"installed material filter mutated input or kept missing entries");
  for(CoolantEntry entry:installed) check(entry.available,"uninstalled coolant leaked into catalog");
  for(int start=0;start<entries.size();start+=32) {
   CatalogNetwork.Packet packet=roundTrip(new CatalogNetwork.Packet(7,entries.size(),start,entries.subList(start,Math.min(start+32,entries.size()))));
   check(packet.entries[0].conductivity==7.8f && packet.entries[0].absorption==.12f,"coefficient wire format");
   CatalogNetwork.receive(packet);
   check((CatalogNetwork.current(7)!=null)==(start==64),"partially received catalog published");
  }
  CatalogNetwork.State state=CatalogNetwork.current(7);
  check(state.entries.length==70 && "catalog_69".equals(state.entries[69].key),"chunk assembly lost data");
  check(CatalogNetwork.current(8)==null,"catalog leaked to another container");
  CatalogNetwork.receive(roundTrip(new CatalogNetwork.Packet(8,1,0,entries.subList(0,1))));
  check(CatalogNetwork.current(8).entries.length==1 && CatalogNetwork.current(7)==null,"new container did not replace catalog");
  ByteBuf bad=Unpooled.buffer().writeInt(7).writeInt(50000).writeInt(0).writeInt(0);
  try { new CatalogNetwork.Packet().fromBytes(bad); throw new AssertionError("unbounded payload accepted"); }
  catch(IllegalArgumentException expected) {} finally { bad.release(); }
  System.out.println("PASS: custom server registries, immutable coefficient snapshots, catalog codec, chunk assembly bounds and missing-material filtering");
 }
}
