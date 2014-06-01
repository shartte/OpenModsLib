package openmods.sync;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class SyncableDirection extends SyncableObjectBase {

	private ForgeDirection value = ForgeDirection.UNKNOWN;

	public SyncableDirection(ForgeDirection dir) {
		super();
		setValue(dir);
	}

	public ForgeDirection getValue() {
		return value;
	}

	public void setValue(ForgeDirection direction) {
		if (direction != value) {
			markDirty();
			value = direction;
		}
	}

	@Override
	public void readFromStream(DataInput stream) throws IOException {
		value = ForgeDirection.getOrientation(stream.readByte());
	}

	@Override
	public void writeToStream(DataOutput stream, boolean fullData) throws IOException {
		stream.writeByte(value.ordinal());
	}

	@Override
	public void writeToNBT(NBTTagCompound tag, String name) {
		tag.setByte(name, (byte)value.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound tag, String name) {
		value = ForgeDirection.getOrientation(tag.getByte(name));
	}

}
