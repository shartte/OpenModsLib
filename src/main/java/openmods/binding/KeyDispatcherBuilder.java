package openmods.binding;

import java.util.List;
import java.util.Map;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.settings.KeyBinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class KeyDispatcherBuilder {

	private final List<ActionBind> bindings = Lists.newArrayList();

	public KeyDispatcherBuilder addBinding(ActionBind binding) {
		bindings.add(binding);
		return this;
	}

	public void register() {
		Map<KeyBinding, ActionBind> bindingMap = Maps.newIdentityHashMap();
    for (ActionBind action : bindings) {
      KeyBinding binding = action.createBinding();
      bindingMap.put(binding, action);
      ClientRegistry.registerKeyBinding(binding);
    }

    KeyDispatcher dispatcher = new KeyDispatcher(bindingMap);
    FMLCommonHandler.instance().bus().register(dispatcher);
  }

}
