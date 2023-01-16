package onslaught.ketongu10.capabilities.units;

import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.util.NBTHelpers;
import onslaught.ketongu10.war.WarsManager;
import onslaught.ketongu10.war.units.UnitBase;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.UUID;

public class UnitCapability <T extends EntityLiving> implements INBTSerializable<NBTTagCompound>, IUnitMember, IChunkLoaderEntity {
    public T orgEntity;
    protected UnitBase unit;
    protected UUID unitId;
    protected BlockPos target;
    ForgeChunkManager.Ticket chunkTicket = null;
    private int tick = 0;

    public BlockPos getTarget() {
        return target;
    }

    public void setTarget(@Nullable BlockPos newTarget) {
        target = newTarget;
    }


    public UnitBase getUnit() {
        return unit;
    }


    public boolean isTeam(EntityLivingBase entityLivingBase) {
        UnitCapability targetcap = entityLivingBase.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
        if (targetcap != null && targetcap.getUnit() != null && this.unit != null) {
            return targetcap.getUnit().battle.equals(this.unit.battle);
        }
        return false;
    }

    public void onEntityJoinWorld(World world) {
        if (unit != null && !unit.battle.finished && unit.isLeader(orgEntity)) {
            if (!world.isRemote) {
                this.setupInitialTicket();
                //this.orgEntity.enablePersistence();
            }
        }
    }


    public void onEntityConstructed(T entityIn) {
        this.orgEntity = entityIn;
    }

    public UnitCapability() {}


    /**CALLED FROM UNIT WHEN ENTITY SPAWNED**/
    public void onEntityJoinUnit(UnitBase u, UUID id) {
        this.unit = u;
        this.unitId = id;
    }

    public void onEntityDeath(EntityLiving entityIn, DamageSource source) {
        this.releaseTicket();
        if (this.unit != null) {
            this.unit.onWarriorDeath(entityIn);
        } else {
            if (source instanceof EntityDamageSource && source.getTrueSource() instanceof EntityPlayer) {
                WarData cap = source.getTrueSource().getServer().getWorld(0).getCapability(ModCapabilities.WAR_DATA, null);
                //WarData cap = entityIn.world.getCapability(ModCapabilities.WAR_DATA, null);
                if (cap != null && cap instanceof WarsManager) {
                    ((WarsManager) cap).tryStartNewWar((EntityPlayerMP) source.getTrueSource(), entityIn);
                }
            }
        }
    }

    public void releaseTicket() {
        ForgeChunkManager.releaseTicket(chunkTicket);
        chunkTicket = null;
    }

    public void setTicket(ForgeChunkManager.Ticket tk) {
        if (this.chunkTicket != tk) {
            releaseTicket();
            if (tk != null) {
                this.chunkTicket = tk;
                forceTicketChunks();
            }
        }
    }

    public void setupInitialTicket() {
        this.chunkTicket = ForgeChunkManager.requestTicket(Onslaught.instance, orgEntity.world, ForgeChunkManager.Type.ENTITY);
        if (this.chunkTicket != null) {
            writeDataToTicket();
            forceTicketChunks();
        }
    }


    protected void writeDataToTicket() {
        ChunkLoader.INSTANCE.writeDataToTicket(chunkTicket, this.orgEntity.getPosition(), this.orgEntity);
    }

    protected void forceTicketChunks() {
        int cx = this.orgEntity.getPosition().getX() >> 4;
        int cz = this.orgEntity.getPosition().getZ() >> 4;
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 1; z <= cz + 1; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);
                ForgeChunkManager.forceChunk(this.chunkTicket, chunkPos);
            }
        }
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        if (this.unit != null && this.unitId != null) {
            tag.setUniqueId("unitId", this.unitId);
            if (this.target != null) {
                tag.setTag("targetBlock", NBTHelpers.writeBlockPosToNBT(new NBTTagCompound(), this.target));
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        UUID id = tag.getUniqueId("unitId");
        if (id != null ) {
            WarData cap = orgEntity.world.getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null) {
                if (((WarsManager) cap).unitIDs.containsKey(id)) {
                    this.unit = ((WarsManager) cap).unitIDs.get(id);
                    this.unitId = id;
                    this.target = NBTHelpers.readBlockPosFromNBT(tag.getCompoundTag("position"));
                    this.unit.addWarrior(this.orgEntity);
                } else {
                }
            } else {
            }
        }
    }
}
