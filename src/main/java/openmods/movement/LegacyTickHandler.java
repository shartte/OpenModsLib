package openmods.movement;

import java.util.EnumSet;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;

public class LegacyTickHandler {

  @SubscribeEvent
  public void tickEvent(TickEvent tickEvent) {

    if (tickEvent.side != Side.CLIENT)
      return;

    if (tickEvent.phase == TickEvent.Phase.START) {
      EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
      if (player != null) {
        PlayerMovementManager.updateMovementState(player.movementInput, player);
      }
    }

  }

}
