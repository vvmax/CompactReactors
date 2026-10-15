package dev.compactreactors;
import java.util.*;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.*;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
/** Server sends a bounded, chunked read-only catalog when the reactor container opens. */
public final class CatalogNetwork {
 private static final int CHUNK=32, MAX_ENTRIES=4096;
 private static SimpleNetworkWrapper channel;
 private CatalogNetwork() {}
 public static void init() {
  channel=NetworkRegistry.INSTANCE.newSimpleChannel("compactreactors");
  channel.registerMessage(Handler.class,Packet.class,0,Side.CLIENT);
 }
 public static void send(int window,EntityPlayerMP player) {
  List<CoolantEntry> all=CoolantCatalog.snapshot();
  if(all.size()>MAX_ENTRIES) throw new IllegalStateException("Moderator registry exceeds catalog capacity");
  for(int start=0;start<all.size();start+=CHUNK)
   channel.sendTo(new Packet(window,all.size(),start,all.subList(start,Math.min(start+CHUNK,all.size()))),player);
 }
 public static final class State {
  public final int window;
  public final CoolantEntry[] entries;
  private State(int window,CoolantEntry[] entries) { this.window=window; this.entries=entries; }
  public boolean complete() { for(CoolantEntry e:entries) if(e==null) return false; return true; }
 }
 private static volatile State latest;
 public static State current(int window) { State s=latest; return s!=null && s.window==window && s.complete()?s:null; }
 static synchronized void receive(Packet packet) {
  State old=latest;
  CoolantEntry[] entries=old!=null && old.window==packet.window && old.entries.length==packet.total && packet.start!=0?old.entries.clone():new CoolantEntry[packet.total];
  System.arraycopy(packet.entries,0,entries,packet.start,packet.entries.length);
  latest=new State(packet.window,entries);
 }
 public static class Packet implements IMessage {
  int window,total,start;
  CoolantEntry[] entries;
  public Packet() {}
  Packet(int window,int total,int start,List<CoolantEntry> entries) { this.window=window; this.total=total; this.start=start; this.entries=entries.toArray(new CoolantEntry[entries.size()]); }
  @Override public void toBytes(ByteBuf b) {
   b.writeInt(window); b.writeInt(total); b.writeInt(start); b.writeInt(entries.length);
   for(CoolantEntry e:entries) {
    b.writeByte(e.type); ByteBufUtils.writeUTF8String(b,e.key);
    b.writeFloat(e.absorption); b.writeFloat(e.efficiency); b.writeFloat(e.moderation); b.writeFloat(e.conductivity);
    b.writeBoolean(e.available); b.writeBoolean(e.usable); ByteBufUtils.writeItemStack(b,e.icon);
   }
  }
  @Override public void fromBytes(ByteBuf b) {
   window=b.readInt(); total=b.readInt(); start=b.readInt(); int count=b.readInt();
   if(total<0 || total>MAX_ENTRIES || count<0 || count>CHUNK || start<0 || start>total-count) throw new IllegalArgumentException("Invalid catalog packet bounds");
   entries=new CoolantEntry[count];
   for(int i=0;i<count;i++) {
    int type=b.readUnsignedByte(); String key=ByteBufUtils.readUTF8String(b);
    if(type>CoolantEntry.FLUID || key.length()>256) throw new IllegalArgumentException("Invalid catalog entry");
    float a=b.readFloat(),e=b.readFloat(),m=b.readFloat(),c=b.readFloat();
    boolean available=b.readBoolean(),usable=b.readBoolean(); ItemStack icon=ByteBufUtils.readItemStack(b);
    entries[i]=new CoolantEntry(type,key,a,e,m,c,available,usable,icon);
   }
  }
 }
 public static class Handler implements IMessageHandler<Packet,IMessage> {
  @Override public IMessage onMessage(Packet message,MessageContext context) { receive(message); return null; }
 }
}
