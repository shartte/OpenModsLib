package openmods;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.MinecraftForge;
import openmods.config.CommandConfig;
import openmods.config.ConfigProcessing;
import openmods.entity.DelayedEntityLoadManager;
import openmods.fakeplayer.FakePlayerPool;
import openmods.integration.Integration;
import openmods.integration.modules.BuildCraftPipes;
import openmods.network.EventPacket;
import openmods.network.events.TileEntityEventHandler;
import openmods.proxy.IOpenModsProxy;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;

@Mod(modid = "OpenMods", name = "OpenMods", version = "0.5", dependencies = "required-after:OpenModsCore")
public class OpenMods {

	@Instance(value = "OpenMods")
	public static OpenMods instance;

	@SidedProxy(clientSide = "openmods.proxy.OpenClientProxy", serverSide = "openmods.proxy.OpenServerProxy")
	public static IOpenModsProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
    Log.setLogger(evt.getModLog());

		EventPacket.registerCorePackets();

		final File configFile = evt.getSuggestedConfigurationFile();
		Configuration config = new Configuration(configFile);
		ConfigProcessing.processAnnotations(configFile, "OpenMods", config, LibConfig.class);
		if (config.hasChanged()) config.save();

		MinecraftForge.EVENT_BUS.register(new TileEntityEventHandler());

		MinecraftForge.EVENT_BUS.register(DelayedEntityLoadManager.instance);

		MinecraftForge.EVENT_BUS.register(FakePlayerPool.instance);

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		Integration.addModule(new BuildCraftPipes());
		Integration.loadModules();
		proxy.postInit();
	}

	@EventHandler
	public void severStart(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new CommandConfig("om_config_s", true));
	}
}
