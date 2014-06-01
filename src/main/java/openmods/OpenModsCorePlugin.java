package openmods;

import java.util.Map;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;

//must be lower than all dependent ones
@SortingIndex(16)
@TransformerExclusions({ "openmods.asm", "openmods.include" })
public class OpenModsCorePlugin implements IFMLLoadingPlugin {

	public static Logger log = LogManager.getLogger("OpenModsCore");

  // TODO: Cannot set logger parent to FML without serious gymnastics

	@Override
	public String[] getASMTransformerClass() {
		return new String[] { "openmods.OpenModsClassTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return "openmods.OpenModsCore";
	}

	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {}

  @Override
  public String getAccessTransformerClass() {
    return null;
  }

  @Deprecated
	public String[] getLibraryRequestClass() {
		return null;
	}

}
