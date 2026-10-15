package dev.compactreactors;
import java.io.File;
import java.net.URL;
import java.util.*;
import net.minecraft.launchwrapper.LaunchClassLoader;
/** Forge 1.7 registries require LaunchClassLoader even in a headless test JVM. */
public class LiquidTest {
 public static void main(String[] args) throws Exception {
  String[] paths=System.getProperty("java.class.path").split(File.pathSeparator);
  URL[] urls=new URL[paths.length]; for(int i=0;i<paths.length;i++) urls[i]=new File(paths[i]).toURI().toURL();
  LaunchClassLoader loader=new LaunchClassLoader(urls);
  loader.registerTransformer("dev.compactreactors.HeadlessOptionalTransformer");
  net.minecraft.launchwrapper.Launch.classLoader=loader;
  Thread.currentThread().setContextClassLoader(loader);
  Class<?> fml=Class.forName("cpw.mods.fml.common.Loader",true,loader);
  fml.getMethod("injectData",Object[].class).invoke(null,(Object)new Object[]{"10","13","4","1614","1.7.10","9.05",new File("build/fluid-test"),Collections.emptyList()});
  try { Class.forName("dev.compactreactors.LiquidIntegrationChecks",true,loader).getMethod("run").invoke(null); }
  catch(java.lang.reflect.InvocationTargetException e) { throw new RuntimeException(e.getCause()); }
  finally { loader.close(); }
 }
}
