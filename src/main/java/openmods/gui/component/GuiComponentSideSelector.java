package openmods.gui.component;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import openmods.gui.misc.*;
import openmods.gui.misc.SidePicker.HitCoord;
import openmods.gui.misc.SidePicker.Side;
import openmods.gui.misc.Trackball.TrackballWrapper;
import openmods.sync.SyncableFlags;
import openmods.utils.MathUtils;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiComponentSideSelector extends BaseComponent {

	RenderBlocks blockRender = new RenderBlocks();

	private TrackballWrapper trackball = new TrackballWrapper(1, 40);

	public double scale;
	private ForgeDirection lastSideHovered;
	private int movedTicks = 0;
	public SyncableFlags enabledDirections;
	private Block block;
	private boolean isInitialized;
	private int meta = 0;
	private TileEntity te;
	private boolean highlightSelectedSides = false;

	public GuiComponentSideSelector(int x, int y, double scale, TileEntity te, int meta, Block block, SyncableFlags directions, boolean highlightSelectedSides) {
		super(x, y);
		this.scale = scale;
		this.enabledDirections = directions;
		this.block = block;
		this.meta = meta;
		this.te = te;
		this.highlightSelectedSides = highlightSelectedSides;
	}

	@Override
	public void render(Minecraft minecraft, int offsetX, int offsetY, int mouseX, int mouseY) {
		if (!isInitialized || Mouse.isButtonDown(2)) {
			trackball.setTransform(MathUtils.createEntityRotateMatrix(minecraft.renderViewEntity));
			isInitialized = true;
		}
		GL11.glPushMatrix();
		Tessellator t = Tessellator.instance;
		GL11.glTranslated(offsetX + x + (scale / 2), offsetY + y + (scale / 2), scale);
		GL11.glScaled(scale, -scale, scale);
		// TODO: replace with proper width,height
		// TODO: Get Mikee to check that I did this right -- NeverCast
		trackball.update(mouseX - getWidth(), -(mouseY - getHeight()));
		if (te != null) TileEntityRendererDispatcher.instance.renderTileEntityAt(te, -0.5, -0.5, -0.5, 0.0F);
		else drawBlock(minecraft.renderEngine, t);

		SidePicker picker = new SidePicker(0.5);

		HitCoord coord = picker.getNearestHit();

		if (coord != null) drawHighlight(t, coord.side, 0x444444);

		if (highlightSelectedSides) {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				if (enabledDirections.get(dir.ordinal())) {
					drawHighlight(t, Side.fromForgeDirection(dir), 0xCC0000);
				}
			}
		}

		lastSideHovered = coord == null? ForgeDirection.UNKNOWN : coord.side.toForgeDirection();

		GL11.glPopMatrix();
	}

	private static void drawHighlight(Tessellator t, SidePicker.Side side, int color) {

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		t.startDrawingQuads();
		t.setColorRGBA_I(color, 64);
		switch (side) {
			case XPos:
				t.addVertex(0.5, -0.5, -0.5);
				t.addVertex(0.5, 0.5, -0.5);
				t.addVertex(0.5, 0.5, 0.5);
				t.addVertex(0.5, -0.5, 0.5);
				break;
			case YPos:
				t.addVertex(-0.5, 0.5, -0.5);
				t.addVertex(-0.5, 0.5, 0.5);
				t.addVertex(0.5, 0.5, 0.5);
				t.addVertex(0.5, 0.5, -0.5);
				break;
			case ZPos:
				t.addVertex(-0.5, -0.5, 0.5);
				t.addVertex(0.5, -0.5, 0.5);
				t.addVertex(0.5, 0.5, 0.5);
				t.addVertex(-0.5, 0.5, 0.5);
				break;
			case XNeg:
				t.addVertex(-0.5, -0.5, -0.5);
				t.addVertex(-0.5, -0.5, 0.5);
				t.addVertex(-0.5, 0.5, 0.5);
				t.addVertex(-0.5, 0.5, -0.5);
				break;
			case YNeg:
				t.addVertex(-0.5, -0.5, -0.5);
				t.addVertex(0.5, -0.5, -0.5);
				t.addVertex(0.5, -0.5, 0.5);
				t.addVertex(-0.5, -0.5, 0.5);
				break;
			case ZNeg:
				t.addVertex(-0.5, -0.5, -0.5);
				t.addVertex(-0.5, 0.5, -0.5);
				t.addVertex(0.5, 0.5, -0.5);
				t.addVertex(0.5, -0.5, -0.5);
				break;
			default:
				break;
		}
		t.draw();
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void drawBlock(TextureManager manager, Tessellator t) {
		GL11.glColor4f(1, 1, 1, 1);
		manager.bindTexture(TextureMap.locationBlocksTexture);
		blockRender.setRenderBounds(0, 0, 0, 1, 1, 1);
		t.startDrawingQuads();

		blockRender.renderFaceXNeg(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(4, meta));

		blockRender.renderFaceXPos(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(5, meta));

		blockRender.renderFaceYPos(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(1, meta));

		blockRender.renderFaceYNeg(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(0, meta));

		blockRender.renderFaceZNeg(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(2, meta));

		blockRender.renderFaceZPos(Blocks.stone, -0.5, -0.5, -0.5, block.getIcon(3, meta));

		t.draw();
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int button, long time) {
		super.mouseClickMove(mouseX, mouseY, button, time);
		movedTicks++;
	}

	@Override
	public void mouseMovedOrUp(int mouseX, int mouseY, int button) {
		super.mouseMovedOrUp(mouseX, mouseY, button);
		if (button == 0 && movedTicks < 5 && lastSideHovered != null
				&& lastSideHovered != ForgeDirection.UNKNOWN) {
			this.enabledDirections.toggle(lastSideHovered.ordinal());
			movedTicks = 5;
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		super.mouseClicked(mouseX, mouseY, button);
		movedTicks = 0;
		lastSideHovered = null;
	}

	@Override
	public int getWidth() {
		return 50;
	}

	@Override
	public int getHeight() {
		return 50;
	}
}
