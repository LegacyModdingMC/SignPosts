package harceroi.mc.signposts.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import harceroi.mc.signposts.SignPostsMod;
import harceroi.mc.signposts.item.SignPostMarkerItem;
import hunternif.mc.atlas.item.ItemAtlas;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SignPostBlock extends Block implements ITileEntityProvider {

    public SignPostBlock() {
        super(Material.plants);
        setStepSound(Block.soundTypeWood);
        setHardness(2.0f);
        setResistance(6.0f);
        setHarvestLevel("axe", 2);
        setBlockBounds(0.4f, 0f, 0.4f, 0.6f, 1f, 0.6f);
        isBlockContainer = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new SignPostTileEntity(meta);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float sideX, float sideY, float sideZ) {
        if (world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (!(te instanceof SignPostTileEntity)) {
                return false;
            }

            // what are we holding?
            ItemStack atlasStack = null;
            ItemStack currentItemStack = player.getCurrentEquippedItem();

            SignPostTileEntity signPost = (SignPostTileEntity) te;
            // is SignPost already activated, i.e. has a corresponding marker and is
            // the player holding a magic marker?
            if (currentItemStack != null && currentItemStack.getItem() instanceof SignPostMarkerItem) {
                if (signPost.getMarkerId() == 0) {
                    SignPostsMod.proxy.openSignPostLabelGui(x, y, z, player.posX, Math.floor(player.posY - 1), player.posZ);
                }
                return false;
            }
            // Check hotbar
            if (currentItemStack != null && currentItemStack.getItem() instanceof ItemAtlas) {
                atlasStack = currentItemStack;
            } else {
                for (int j = 8; j > 0; j--) {
                    ItemStack stack = player.inventory.getStackInSlot(j);

                    if (stack != null && stack.getItem() instanceof ItemAtlas) {
                        atlasStack = stack;
                    }
                }

            }

            if (atlasStack != null && signPost.getMarkerId() != 0) {
                SignPostsMod.proxy.openAtlasGui(atlasStack, SignPostsMod.ID);
            }
        }

        return false;
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
        if(meta >= 4) {
            return 4;
        }

        world.setBlock(x, y + 1, z, this, 4, 2);
        return meta;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int rotation = MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta = 0;
        switch(rotation) {
            case 0:
                meta = 2;
                break;
            case 1:
                meta = 3;
                break;
            case 2:
                meta = 0;
                break;
            case 3:
                meta = 1;
                break;
        }
        worldIn.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, int x, int y, int z) {
        return worldIn.getBlock(x, y, z).isReplaceable(worldIn, x, y, z) && worldIn.getBlock(x, y + 1, z).isReplaceable(worldIn, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (meta == 4) {
            world.setBlockToAir(x, y - 1, z);
        } else {
            SignPostsMod.removeJumpTarget(x, y, z, world);
            world.setBlockToAir(x, y + 1, z);
        }
        world.removeTileEntity(x, y, z);
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean onBlockEventReceived(World world, int x, int y, int z, int id, int type) {
        super.onBlockEventReceived(world, x, y, z, id, type);
        TileEntity tileentity = world.getTileEntity(x, y, z);
        return tileentity != null ? tileentity.receiveClientEvent(id, type) : false;
    }

    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    @Override
    public boolean isBlockSolid(IBlockAccess world, int x, int y, int z, int side) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if(side <= 1) {
            return blockIcon;
        }
        return Blocks.log.getIcon(side, 0);
    }

}
