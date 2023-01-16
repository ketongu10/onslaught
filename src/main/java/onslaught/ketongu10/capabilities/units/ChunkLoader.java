package onslaught.ketongu10.capabilities.units;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager.LoadingCallback;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.util.Reference;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public final class ChunkLoader implements LoadingCallback {

    public static final ChunkLoader INSTANCE = new ChunkLoader();
    private static final String ENTITY_LOADING_TAG = "entityLoader";

    private ChunkLoader() {

    }

    @Override
    public void ticketsLoaded(List<Ticket> tickets, World world) {
        for (Ticket tk : tickets) {
            if (!tk.isPlayerTicket() && tk.getModId().startsWith(Reference.MOD_ID) && tk.getModData().hasKey(ENTITY_LOADING_TAG)) {
                //this.getEntity(world, tk.getEntity(), IChunkLoaderEntity.class).ifPresent(t -> t.setTicket(tk));//for white villager
                /**ChunkLoaderCapability cap = tk.getEntity().getCapability(ModCapabilities.CL_CAPABILITY, null);
                if (cap != null) {
                    this.getEntityData(world, cap, IChunkLoaderEntity.class).ifPresent(t -> t.setTicket(tk));
                }**/
                UnitCapability cap = tk.getEntity().getCapability(ModCapabilities.UNIT_CAPABILITY, null);
                if (cap != null) {
                    this.getEntityData(world, cap, IChunkLoaderEntity.class).ifPresent(t -> t.setTicket(tk));
                    System.out.println("======================TICKET FOUND================");
                }

            }
        }
    }

    public void writeDataToTicket(Ticket tk, BlockPos pos, EntityLiving entityLiving) {
        tk.getModData().setLong(ENTITY_LOADING_TAG, pos.toLong());
        tk.bindEntity(entityLiving);
    }

    public static <T> Optional<T> getTile(@Nullable IBlockAccess world, @Nullable BlockPos pos, Class<T> teClass) {
        if (world == null || pos == null) {
            return Optional.empty();
        }

        TileEntity te = world.getTileEntity(pos);

        if (teClass.isInstance(te)) {
            return Optional.of(teClass.cast(te));
        }

        return Optional.empty();
    }

    public static <T> Optional<T> getEntity(@Nullable IBlockAccess world, @Nullable Entity entityLiving, Class<T> teClass) {
        if (world == null || entityLiving.getPosition() == null) {
            return Optional.empty();
        }


        if (teClass.isInstance(entityLiving)) {
            return Optional.of(teClass.cast(entityLiving));
        }

        return Optional.empty();
    }
    public static <T> Optional<T> getEntityData(@Nullable IBlockAccess world, @Nullable UnitCapability entityData, Class<T> teClass) {
        if (world == null || entityData == null) {
            return Optional.empty();
        }


        if (teClass.isInstance(entityData)) {
            return Optional.of(teClass.cast(entityData));
        }

        return Optional.empty();
    }

}
