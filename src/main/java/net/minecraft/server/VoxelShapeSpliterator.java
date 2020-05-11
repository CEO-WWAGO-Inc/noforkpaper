package net.minecraft.server;

import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class VoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {

    @Nullable
    private final Entity a; final Entity getEntity() { return this.a; } // Paper - OBFHELPER
    private final AxisAlignedBB b;
    private final VoxelShapeCollision c;
    private final CursorPosition d;
    private final BlockPosition.MutableBlockPosition e; final BlockPosition.MutableBlockPosition getMutablePos() { return this.e; } // Paper - OBFHELPER
    private final VoxelShape f;
    private final ICollisionAccess g; final ICollisionAccess getCollisionAccess() { return this.g; } // Paper - OBFHELPER
    private boolean h;
    private final BiPredicate<IBlockData, BlockPosition> i;

    public VoxelShapeSpliterator(ICollisionAccess icollisionaccess, @Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        this(icollisionaccess, entity, axisalignedbb, (iblockdata, blockposition) -> {
            return true;
        });
    }

    public VoxelShapeSpliterator(ICollisionAccess icollisionaccess, @Nullable Entity entity, AxisAlignedBB axisalignedbb, BiPredicate<IBlockData, BlockPosition> bipredicate) {
        super(Long.MAX_VALUE, 1280);
        this.c = entity == null ? VoxelShapeCollision.a() : VoxelShapeCollision.a(entity);
        this.e = new BlockPosition.MutableBlockPosition();
        this.f = VoxelShapes.a(axisalignedbb);
        this.g = icollisionaccess;
        this.h = entity != null;
        this.a = entity;
        this.b = axisalignedbb;
        this.i = bipredicate;
        int i = MathHelper.floor(axisalignedbb.minX - 1.0E-7D) - 1;
        int j = MathHelper.floor(axisalignedbb.maxX + 1.0E-7D) + 1;
        int k = MathHelper.floor(axisalignedbb.minY - 1.0E-7D) - 1;
        int l = MathHelper.floor(axisalignedbb.maxY + 1.0E-7D) + 1;
        int i1 = MathHelper.floor(axisalignedbb.minZ - 1.0E-7D) - 1;
        int j1 = MathHelper.floor(axisalignedbb.maxZ + 1.0E-7D) + 1;

        this.d = new CursorPosition(i, k, i1, j, l, j1);
    }

    public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
        return this.h && this.b(consumer) || this.a(consumer);
    }

    boolean a(Consumer<? super VoxelShape> consumer) {
        while (true) {
            if (this.d.a()) {
                int i = this.d.b(); final int x = i;
                int j = this.d.c(); final int y = j;
                int k = this.d.d(); final int z = k;
                int l = this.d.e();

                if (l == 3) {
                    continue;
                }

                // Paper start - ensure we don't load chunks
                Entity entity = this.getEntity();
                BlockPosition.MutableBlockPosition blockposition_mutableblockposition = this.getMutablePos();
                boolean far = entity != null && MCUtil.distanceSq(entity.locX(), y, entity.locZ(), x, y, z) > 14;
                blockposition_mutableblockposition.setValues(x, y, z);

                boolean isRegionLimited = this.getCollisionAccess() instanceof RegionLimitedWorldAccess;
                IBlockData iblockdata = isRegionLimited ? Blocks.VOID_AIR.getBlockData() : ((!far && entity instanceof EntityPlayer) || (entity != null && entity.collisionLoadChunks)
                    ? this.getCollisionAccess().getType(blockposition_mutableblockposition)
                    : this.getCollisionAccess().getTypeIfLoaded(blockposition_mutableblockposition)
                );

                if (iblockdata == null) {
                    if (!(entity instanceof EntityPlayer) || entity.world.paperConfig.preventMovingIntoUnloadedChunks) {
                        VoxelShape voxelshape3 = VoxelShapes.of(far ? entity.getBoundingBox() : new AxisAlignedBB(new BlockPosition(x, y, z)));
                        consumer.accept(voxelshape3);
                        return true;
                    }
                    continue;
                }
                // Paper - moved up
                // Paper end

                if (!this.i.test(iblockdata, this.e) || l == 1 && !iblockdata.d() || l == 2 && !iblockdata.a(Blocks.MOVING_PISTON)) {
                    continue;
                }

                VoxelShape voxelshape = iblockdata.b((IBlockAccess) this.g, this.e, this.c);

                if (voxelshape == VoxelShapes.b()) {
                    if (!this.b.a((double) i, (double) j, (double) k, (double) i + 1.0D, (double) j + 1.0D, (double) k + 1.0D)) {
                        continue;
                    }

                    consumer.accept(voxelshape.a((double) i, (double) j, (double) k));
                    return true;
                }

                VoxelShape voxelshape1 = voxelshape.a((double) i, (double) j, (double) k);

                if (!VoxelShapes.c(voxelshape1, this.f, OperatorBoolean.AND)) {
                    continue;
                }

                consumer.accept(voxelshape1);
                return true;
            }

            return false;
        }
    }

    @Nullable
    private IBlockAccess a(int i, int j) {
        int k = i >> 4;
        int l = j >> 4;

        return this.g.c(k, l);
    }

    boolean b(Consumer<? super VoxelShape> consumer) {
        Objects.requireNonNull(this.a);
        this.h = false;
        WorldBorder worldborder = this.g.getWorldBorder();
        AxisAlignedBB axisalignedbb = this.a.getBoundingBox();

        if (!a(worldborder, axisalignedbb)) {
            // Paper start
            if (worldborder.isInBounds(axisalignedbb.shrink(1.0E-7D)) && !worldborder.isInBounds(axisalignedbb.grow(1.0E-7D))) {
                consumer.accept(worldborder.asVoxelShape());
            // Paper end
                return true;
            }
        }

        return false;
    }

    private static boolean a(VoxelShape voxelshape, AxisAlignedBB axisalignedbb) {
        return VoxelShapes.c(voxelshape, VoxelShapes.a(axisalignedbb.g(1.0E-7D)), OperatorBoolean.AND);
    }

    private static boolean b(VoxelShape voxelshape, AxisAlignedBB axisalignedbb) {
        return VoxelShapes.c(voxelshape, VoxelShapes.a(axisalignedbb.shrink(1.0E-7D)), OperatorBoolean.AND);
    }

    public static boolean a(WorldBorder worldborder, AxisAlignedBB axisalignedbb) {
        double d0 = (double) MathHelper.floor(worldborder.e());
        double d1 = (double) MathHelper.floor(worldborder.f());
        double d2 = (double) MathHelper.f(worldborder.g());
        double d3 = (double) MathHelper.f(worldborder.h());

        return axisalignedbb.minX > d0 && axisalignedbb.minX < d2 && axisalignedbb.minZ > d1 && axisalignedbb.minZ < d3 && axisalignedbb.maxX > d0 && axisalignedbb.maxX < d2 && axisalignedbb.maxZ > d1 && axisalignedbb.maxZ < d3;
    }
}
