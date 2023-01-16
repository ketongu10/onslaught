package onslaught.ketongu10.war.units;

import onslaught.ketongu10.war.Battle;
import onslaught.ketongu10.war.FactionUnits;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shadowmage.ancientwarfare.vehicle.config.AWVehicleStatics;
import net.shadowmage.ancientwarfare.vehicle.entity.VehicleBase;
import net.shadowmage.ancientwarfare.vehicle.entity.types.VehicleType;

import java.util.*;

import static onslaught.ketongu10.war.FactionUnits.Civilised;

/**ONLY AW**/
public class UnitHS extends UnitBase implements IRangeUnit {

    public UnitHS(World w, FactionUnits.UnitType typ, String fac, String subfac, BlockPos pos, boolean warlord, Battle bat) {
        super(w, typ, fac, subfac, pos, warlord, bat);
    }
    public UnitHS(FactionUnits.UnitType typ,Battle bat) {
        super(typ, bat);
    }

    protected void createMembersByType(FactionUnits.UnitType type) {
        if (FactionUnits.FactionSoldiers.containsKey(faction)) {
            EntityLiving ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).hs);
            if (ent != null) {entities.add(ent);}
            for (int i = 0; i < 2; i++) {
                ent = (EntityLiving) createWarrior(FactionUnits.FactionSoldiers.get(faction).hs);
                if (ent != null) {entities.add(ent);}
            }
        }
        setNewLeader(this.isWarlord);
    }
    @Override
    protected void modifyAI(EntityLiving entityIn) {
        super.modifyAI(entityIn);
    }

    /**
     * CATAPULT_STAND_FIXED = 0
     * CATAPULT_MOBILE_FIXED = 2
     * CANNON_MOBILE_FIXED = 11
     * HWACHA = 12
     * TREBUCHET_LARGE = 16
     */
    @Override
    public void spawnNoAI() {
        if (!this.world.isRemote) {
            if (faction == "AW" && Civilised.containsKey(subfaction)) {
                BlockPos where1 = findFreeSpace(this.position);
                for (int i = 0; i < 2; i++) {
                    spawnVehicle(world, where1, Civilised.get(subfaction).light, Civilised.get(subfaction).level, i, 0);
                }
                spawnVehicle(world, where1, Civilised.get(subfaction).heavy, Civilised.get(subfaction).level, 0.5f, -1);
            }
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
                this.world.spawnEntity(e);
                e.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(e)), (IEntityLivingData)null);
            }
        }
    }

    public void spawnVehicle(World w, BlockPos where, int vehicleType, int level, float shiftOrt, float shiftNorm) {
        Optional<VehicleBase> v = VehicleType.getVehicleForType(world, vehicleType, level);
        if (!v.isPresent()) {
            return;
        }
        VehicleBase vehicle = v.get();
        vehicle.setHealth(vehicle.baseHealth);
        Vec3d vec3d = new Vec3d(this.battle.partOfWar.player.getBedLocation().subtract(where));
        Vec3d norm = vec3d.normalize().scale(9);
        Vec3d ort = vec3d.crossProduct(new Vec3d(0, 1, 0)).normalize().scale(8);
        double x = where.getX() + ort.x*shiftOrt + norm.x*shiftNorm;
        double y = where.getY();
        double z = where.getZ() + ort.z*shiftOrt + norm.z*shiftNorm;
        vehicle.setPosition(x, y, z);
        vehicle.prevRotationYaw = vehicle.rotationYaw = (float) Math.toDegrees(Math.atan(-vec3d.z/vec3d.x));
        vehicle.localTurretDestRot = vehicle.localTurretRotation = vehicle.localTurretRotationHome = vehicle.rotationYaw;
        if (AWVehicleStatics.generalSettings.useVehicleSetupTime) {
            vehicle.setSetupState(true, 100);
        }
        w.spawnEntity(vehicle);
    }

}
