package studio.detemkur.civiccraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/** A short-lived mount anchored to a seat block. */
public final class Seat extends Entity {
    public Seat(EntityType<? extends Seat> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override public void tick() {
        super.tick();
        if (level() instanceof ServerLevel) {
            var state = level().getBlockState(blockPosition());
            if (!isVehicle() || !(state.getBlock() instanceof FurnitureBlock furniture) || furniture.kind.seatHeight == 0) {
                ejectPassengers();
                discard();
            }
        }
    }

    @Override public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = blockPosition();
        var state = level().getBlockState(pos);
        Direction front = state.hasProperty(FurnitureBlock.FACING) ? state.getValue(FurnitureBlock.FACING) : Direction.NORTH;
        for (Direction direction : new Direction[]{front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite()}) {
            Vec3 safe = DismountHelper.findSafeDismountLocation(passenger.getType(), level(), pos.relative(direction), true);
            if (safe != null) return safe;
        }
        return Vec3.atBottomCenterOf(pos.above());
    }

    @Override protected boolean canAddPassenger(Entity entity) { return !isVehicle(); }
    @Override public boolean shouldBeSaved() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isAttackable() { return false; }
    @Override public boolean hurtServer(ServerLevel level, DamageSource source, float amount) { return false; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
    @Override protected void readAdditionalSaveData(ValueInput input) {}
    @Override protected void addAdditionalSaveData(ValueOutput output) {}
}
