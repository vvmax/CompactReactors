package dev.compactreactors;
import java.io.ByteArrayInputStream;
import net.minecraft.launchwrapper.IClassTransformer;
import cpw.mods.fml.common.asm.transformers.ModAPITransformer;
import cpw.mods.fml.common.discovery.ASMDataTable;
import cpw.mods.fml.common.discovery.asm.ASMModParser;
/** Apply Forge's actual optional-API stripping without a full mod-discovery startup. */
public final class HeadlessOptionalTransformer implements IClassTransformer {
 @Override public byte[] transform(String name,String transformedName,byte[] bytes) {
  if(bytes==null || !name.startsWith("erogenousbeef.")) return bytes;
  try {
   ASMDataTable annotations=new ASMDataTable();
   new ASMModParser(new ByteArrayInputStream(bytes)).sendToTable(annotations,null);
   ModAPITransformer forge=new ModAPITransformer(); forge.initTable(annotations);
   return forge.transform(name,transformedName,bytes);
  } catch(java.io.IOException e) { throw new IllegalStateException(e); }
 }
}
