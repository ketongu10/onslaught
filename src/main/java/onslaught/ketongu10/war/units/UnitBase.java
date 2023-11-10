package onslaught.ketongu10.war.units;


import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import onslaught.ketongu10.Onslaught;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.units.UnitCapability;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.util.BlockUtils;
import onslaught.ketongu10.util.NBTHelpers;
import onslaught.ketongu10.war.AI.SiegeAIHurtByTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.shadowmage.ancientwarfare.npc.ai.faction.NpcAIFactionHurt;
import net.shadowmage.ancientwarfare.npc.entity.faction.NpcFaction;
import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import onslaught.ketongu10.war.WarsManager;

import java.util.*;

import static onslaught.ketongu10.util.handlers.ConfigHandler.ANCIENT_WARFARE;

public abstract class UnitBase implements INBTSerializable<NBTTagCompound> {

    public List<EntityLiving> entities = new ArrayList<EntityLiving>();
    protected int alive;
    public UUID unitID;
    protected UUID leader;
    public boolean isWarlord;
    protected String faction;
    protected String subfaction;
    public World world;
    protected BlockPos position;
    public Battle battle;
    public FactionUnits.UnitType type;

    public UnitBase(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        this.world = w;
        this.battle = bat;
        this.position = pos;
        this.alive = 0;
        this.isWarlord = warlord;
        this.faction = fac;
        this.subfaction = subfac;
        this.type = typ;
        this.unitID = getNewID();

        createMembersByType(this.type);
    }
    public UnitBase(FactionUnits.UnitType typ, Battle battle) {
        this.battle = battle;
        this.type = typ;
    }

    private UUID getNewID() {
        UUID newID = UUID.randomUUID();
        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null) {
            while (((WarsManager)cap).unitIDs.containsKey(newID)){
                newID = UUID.randomUUID();
            }
        }
        ((WarsManager) cap).unitIDs.put(newID, this);
        return newID;
    }

    public void spawnNoAI() {
        if (!this.world.isRemote) {
            WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);

            for(EntityLiving e: this.entities) {
                writeUnitData(e, this);
                modifyAI(e);
                e.setNoAI(true);
                e.enablePersistence();
                //e.getDataManager().
                BlockPos where = findFreeSpace(this.position);
                double x = where.getX();
                double y = where.getY();
                double z = where.getZ();
                e.setPositionAndRotation(x, y, z, e.rotationYaw, e.rotationPitch);
                if (!MinecraftForge.EVENT_BUS.post(new LivingSpawnEvent(e, world, (float) x, (float) y, (float) z))) {
                    e.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(e)), (IEntityLivingData) null);
                    this.world.spawnEntity(e);

                } else {
                    alive--;
                }

            }
        }
    }

    protected BlockPos findFreeSpace(BlockPos pos) {
        BlockPos newpos = BlockUtils.findNearbyFloorSpace(world, pos, 3, 3, false);
        return newpos != null ? newpos : pos;
    }

    public void onLeaderDeath(EntityLiving entityLeader) {
        entities.remove(entityLeader);
        leader = entities.get(0).getUniqueID();
        setNewLeader(entities.get(0), false);
        alive--;

    }

    protected void setNewLeader(EntityLiving entityLeader, boolean warlord) {
        if (!entityLeader.hasCustomName() && !warlord) {
            entityLeader.setCustomNameTag("Leader");
        }
        if (this.isWarlord && warlord) {
            entityLeader.setCustomNameTag(battle.partOfWar.bossName);
        }
        entityLeader.setAlwaysRenderNameTag(true);
    }


    protected void setNewLeader(boolean warlord) {
        if (!entities.isEmpty()) {
            setNewLeader(entities.get(0), warlord);
            this.leader = this.entities.get(0).getUniqueID();
        }
    }

    public boolean isLeader(EntityLiving entity) {
        if (entity.getUniqueID().equals(this.leader)) {
            return true;
        }
        return false;
    }

    public void onWarriorDeath(EntityLiving entityLiving) {
        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null && cap instanceof WarsManager) {
            ((WarsManager) cap).test++;
        }
        if (alive > 1) {
            UUID deadUUID = entityLiving.getUniqueID();
            if (deadUUID.equals(this.leader)) {
                onLeaderDeath(entityLiving);
                return;
            } else {
                entities.remove(entityLiving);
                alive--;
            }
        } else {
            entities.remove(entityLiving);
            this.leader = null;
            alive--;
            this.battle.UNITS.remove(this);
        }
    }

    protected abstract void createMembersByType(FactionUnits.UnitType type);

    protected void modifyAI(EntityLiving entityIn) {
        Iterator<EntityAITasks.EntityAITaskEntry> iterator = entityIn.targetTasks.taskEntries.iterator();
        List<EntityAIBase> removeTargets = new ArrayList<EntityAIBase>();

        while (iterator.hasNext()) {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = iterator.next();
            EntityAIBase entityAI = entityaitasks$entityaitaskentry.action;

            if (entityAI instanceof EntityAITarget) {
                removeTargets.add(entityAI);
            }
        }

        for (EntityAIBase AI : removeTargets) {
            if (ANCIENT_WARFARE) {
                if (!(AI instanceof NpcAIFactionHurt)) {
                    entityIn.targetTasks.removeTask(AI);
                }
            } else {
                entityIn.targetTasks.removeTask(AI);
            }
        }
        /**Iterator<EntityAITasks.EntityAITaskEntry> iterator2 = entityIn.tasks.taskEntries.iterator();
         List<EntityAIBase> removeTasks = new ArrayList<EntityAIBase>();
         while (iterator2.hasNext()) {
         EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = iterator2.next();
         EntityAIBase entityAI = entityaitasks$entityaitaskentry.action;

         if (entityAI instanceof ESM_EntityAIPillarUp) {
         removeTasks.add(entityAI);
         }

         }

         for (EntityAIBase AI : removeTasks) {
         entityIn.tasks.removeTask(AI);
         }**/

        if (ANCIENT_WARFARE) {
            if (!(entityIn instanceof NpcFaction)) {
                entityIn.targetTasks.addTask(1, new SiegeAIHurtByTarget((EntityCreature) entityIn, true, new Class[]{EntityPlayer.class}));
            }
        } else {
            entityIn.targetTasks.addTask(1, new SiegeAIHurtByTarget((EntityCreature) entityIn, true, new Class[]{EntityPlayer.class}));
        }
        //entityIn.targetTasks.addTask(1, new SiegeAIHurtByTarget((EntityCreature) entityIn, true, new Class[]{EntityPlayer.class}));
        /**for (Class e : FactionUnits.PlayerSoldiers) {
            if (e.isAssignableFrom(EntityPlayer.class)) {
                entityIn.targetTasks.addTask(1, new EntityAINearestAttackableTarget((EntityCreature) entityIn, e, true));
            } else {
                entityIn.targetTasks.addTask(2, new EntityAINearestAttackableTarget((EntityCreature) entityIn, e, true));
            }
        }**/
    }

    public void lastOrder() {
        this.battle = null;
        this.leader = null;
        for (EntityLiving e: entities) {
            UnitCapability cap = e.getCapability(ModCapabilities.UNIT_CAPABILITY, null);
            if (cap != null) {
                cap.releaseTicket();
                cap.leaveUnit();

            }
        }
    }



    public void startSiege() {
        for(EntityLiving e: this.entities) {
            e.setNoAI(false);
        }
    }

    protected void writeUnitData(EntityLiving entityIn, UnitBase unit) {
        if (entityIn.getCapability(ModCapabilities.UNIT_CAPABILITY, null)!=null) {
            entityIn.getCapability(ModCapabilities.UNIT_CAPABILITY, null).onEntityJoinUnit(unit, unitID);
        }
    }
    public void addWarrior(EntityLiving warrior) {
        modifyAI(warrior);
        this.entities.add(warrior);
        this.alive++;

    }

    protected Entity createWarrior(FactionUnits.Warrior warrior) {
        String regname = warrior.name;
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        if (warrior.tags != null) {
            try {
                nbttagcompound = JsonToNBT.getTagFromJson(warrior.tags);
            } catch (NBTException e)
            {
                Onslaught.LOGGER.warn("Failed to read NBT tags for " + regname);
                System.err.println(e);
            }
        }
        nbttagcompound.setString("id", regname);


        if (subfaction != null) {
            nbttagcompound.setString("factionName", subfaction);
        }
        Entity entity = AnvilChunkLoader.readWorldEntity(nbttagcompound, world,  false);
        if (entity != null) {
            this.alive++;
        }
        return entity;
    }


    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setUniqueId("leader", this.leader);
        tag.setBoolean("isWarlord", this.isWarlord);
        tag.setTag("position", NBTHelpers.writeBlockPosToNBT(new NBTTagCompound(), this.position));
        tag.setUniqueId("unitID", this.unitID);
        tag.setInteger("unitType", this.type.getId());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag) {
        this.leader = tag.getUniqueId("leader");
        this.isWarlord = tag.getBoolean("isWarlord");
        this.world = this.battle.partOfWar.world;
        this.faction = this.battle.partOfWar.faction;
        this.subfaction = this.battle.partOfWar.subfaction;
        this.position = NBTHelpers.readBlockPosFromNBT(tag.getCompoundTag("position"));
        this.unitID = tag.getUniqueId("unitID");
        WarData cap = world.getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null) {
            ((WarsManager)cap).unitIDs.put(this.unitID, this);
        }


    }


}
