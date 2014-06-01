package openmods.integration.modules;

import static openmods.integration.Conditions.classExists;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import openmods.integration.IntegrationModule;
import buildcraft.api.transport.IPipeTile;

public class BuildCraftPipes extends IntegrationModule {

	public BuildCraftPipes() {
		super(classExists("buildcraft.api.transport.IPipeTile"));
	}

	public interface BuildCraftAccess {
		public int tryAcceptIntoPipe(TileEntity possiblePipe, ItemStack nextStack, boolean doInsert, ForgeDirection direction);

		public int tryAcceptIntoPipe(TileEntity possiblePipe, FluidStack nextStack, ForgeDirection direction);

		public boolean isPipe(TileEntity tile);
	}

	private static class Dummy implements BuildCraftAccess {
		@Override
		public int tryAcceptIntoPipe(TileEntity possiblePipe, ItemStack nextStack, boolean doInsert, ForgeDirection direction) {
			return 0;
		}

		@Override
		public int tryAcceptIntoPipe(TileEntity possiblePipe, FluidStack nextStack, ForgeDirection direction) {
			return 0;
		}

		@Override
		public boolean isPipe(TileEntity tile) {
			return false;
		}
	}

	private static class Live implements BuildCraftAccess {

		@Override
		public int tryAcceptIntoPipe(TileEntity possiblePipe, ItemStack nextStack, boolean doInsert, ForgeDirection direction) {
			if (possiblePipe instanceof IPipeTile) { return ((IPipeTile)possiblePipe).injectItem(nextStack, doInsert, direction.getOpposite()); }
			return 0;
		}

		@Override
		public int tryAcceptIntoPipe(TileEntity possiblePipe, FluidStack nextStack, ForgeDirection direction) {

      // TODO: Validate this. BC now seems to implement IFluidHandler, so IPipeTile for fluids seems not necessary
      if (possiblePipe instanceof IFluidHandler) {
        IFluidHandler fluidHandler = (IFluidHandler) possiblePipe;
        return fluidHandler.fill(direction.getOpposite(), nextStack, true);
      } else {
        return 0;
      }
		}

		@Override
		public boolean isPipe(TileEntity tile) {
			return tile instanceof IPipeTile;
		}
	}

	private static BuildCraftAccess access = new Dummy();

	public static BuildCraftAccess access() {
		return access;
	}

	@Override
	public void load() {
		access = new Live();
	}

	@Override
	public String name() {
		return "BuildCraft pipes";
	}

}
