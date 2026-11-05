package tw.zonezero.cardboardstealth;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Physics shared by server and local client to avoid jump rubber-banding. */
public final class BoxMovement {
    private BoxMovement() {}
    // Normal vanilla jump impulse is ~0.42. With 1.21.1 gravity/drag this reaches
    // approximately 0.48 blocks, enough for a low hop but not for a full block.
    public static final double JUMP_IMPULSE = 0.240D;

    public static void limitJump(LivingEntity entity) {
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y > JUMP_IMPULSE) {
            entity.setDeltaMovement(motion.x, JUMP_IMPULSE, motion.z);
        }
    }
}
