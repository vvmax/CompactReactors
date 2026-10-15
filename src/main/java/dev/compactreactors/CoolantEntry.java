package dev.compactreactors;
import net.minecraft.item.ItemStack;
/** Snapshot of the server's BR coefficients, without a fabricated RF/t multiplier. */
public final class CoolantEntry {
 public static final int AIR=0, BLOCK=1, FLUID=2;
 public final int type;
 public final String key;
 public final float absorption, efficiency, moderation, conductivity;
 public final boolean available, usable;
 public final ItemStack icon;
 public CoolantEntry(int type,String key,float absorption,float efficiency,float moderation,float conductivity,boolean available,boolean usable,ItemStack icon) {
  this.type=type; this.key=key; this.absorption=absorption; this.efficiency=efficiency;
  this.moderation=moderation; this.conductivity=conductivity; this.available=available; this.usable=usable; this.icon=icon;
 }
 public float metric(int column) {
  switch(column) { case 1:return absorption;case 2:return efficiency;case 3:return moderation;case 4:return conductivity;default:return 0; }
 }
}
