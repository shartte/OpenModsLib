package openmods.binding;

import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import net.minecraft.client.settings.KeyBinding;

class KeyDispatcher {

	private final Map<KeyBinding, ActionBind> bindings;

	KeyDispatcher(Map<KeyBinding, ActionBind> bindings) {
		this.bindings = bindings;
	}

 // TODO: Port this
//	@Override
//	public void keyDown(KeyBinding kb, boolean tickEnd, boolean isRepeat) {
//		ActionBind binding = bindings.get(kb);
//		if (binding != null) binding.keyDown(tickEnd, isRepeat);
//	}
//
//	@Override
//	public void keyUp(KeyBinding kb, boolean tickEnd) {
//		ActionBind binding = bindings.get(kb);
//		if (binding != null) binding.keyUp(tickEnd);
//	}

  @SubscribeEvent
  public void onInputEvent(InputEvent e) {
    // TODO: Handle the key event
  }

}