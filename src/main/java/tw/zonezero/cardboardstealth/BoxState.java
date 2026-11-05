package tw.zonezero.cardboardstealth;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;

public final class BoxState {
    private BoxState() {}
    public static final int ACTIVATE_TICKS = 20;
    private static final String ACTIVE = "cardboardstealth_active";
    private static final String VARIANT = "cardboardstealth_variant";
    private static final String HOLD = "cardboardstealth_hold_ticks";
    private static final String BLOCK_UNTIL = "cardboardstealth_block_until";
    private static final String MOVE_TICK = "cardboardstealth_motion_tick";
    private static final String MOVE_X = "cardboardstealth_motion_x";
    private static final String MOVE_Y = "cardboardstealth_motion_y";
    private static final String MOVE_Z = "cardboardstealth_motion_z";
    private static final String MOVING_UNTIL = "cardboardstealth_moving_until";
    // Ignore extremely small numeric jitter, but count jumps and half-slab climbing as movement.
    private static final double MOTION_EPSILON_SQR = 0.0001D;
    private static final ResourceLocation STEP_ID = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "inside_cardboard_step_height");
    private static final ResourceLocation SLOW_ID = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "inside_cardboard_box");
    private static final TagKey<EntityType<?>> FOOLED = TagKey.create(
            Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "fooled_by_box"));

    public static int equippedVariant(Player player) {
        return CuriosApi.getCuriosInventory(player).flatMap(inv -> inv.getStacksHandler("cardboard"))
                .map(slot -> slot.getStacks().getSlots() > 0
                        ? ModItems.variant(slot.getStacks().getStackInSlot(0).getItem()) : 0)
                .orElse(0);
    }
    public static boolean active(Player player) {
        return player.getPersistentData().getBoolean(ACTIVE);
    }
    public static int variant(Player player) {
        return active(player) ? player.getPersistentData().getInt(VARIANT) : 0;
    }
    public static boolean fooled(Mob mob) {
        return mob instanceof Zombie || mob.getType().is(FOOLED);
    }
    /**
     * Called once per server player tick while disguised. Motion remains true for two
     * additional ticks after stopping, avoiding instant stealth from a tiny pause.
     * The key is current game time; no global entity scan is needed.
     */
    public static void updateMotion(ServerPlayer player) {
        var data = player.getPersistentData();
        long now = player.level().getGameTime();
        long priorTick = data.getLong(MOVE_TICK);
        boolean moving = !player.onGround() || priorTick != now - 1L;
        if (priorTick == now - 1L) {
            double dx = player.getX() - data.getDouble(MOVE_X);
            double dy = player.getY() - data.getDouble(MOVE_Y);
            double dz = player.getZ() - data.getDouble(MOVE_Z);
            moving |= dx * dx + dy * dy + dz * dz > MOTION_EPSILON_SQR;
        }
        if (moving) data.putLong(MOVING_UNTIL, now + 2L);
        data.putLong(MOVE_TICK, now);
        data.putDouble(MOVE_X, player.getX());
        data.putDouble(MOVE_Y, player.getY());
        data.putDouble(MOVE_Z, player.getZ());
    }

    /** Moving = detection up to 3.5 blocks, stationary = detection up to 1.25 blocks. */
    public static boolean moving(Player player) {
        if (!player.onGround()) return true;
        var data = player.getPersistentData();
        long now = player.level().getGameTime();
        // If a movement snapshot is missing/stale, err on the side of being detectable.
        if (data.getLong(MOVE_TICK) < now - 1L) return true;
        return data.getLong(MOVING_UNTIL) >= now;
    }

    /** An attacker already locked on the player is NEVER tricked by the box. */
    public static boolean concealedFrom(Player player, Mob observer) {
        return active(player) && fooled(observer) && observer.getTarget() != player
                && BoxDetectionRules.concealsNewTarget(observer.distanceToSqr(player), moving(player));
    }

    public static void set(ServerPlayer player, int newVariant) {
        int oldVariant = variant(player);
        player.getPersistentData().putBoolean(ACTIVE, newVariant != 0);
        player.getPersistentData().putInt(VARIANT, newVariant);
        applySpeed(player, newVariant != 0);
        if (newVariant != 0 && oldVariant == 0) updateMotion(player);
        if (newVariant == 0) {
            player.getPersistentData().remove(MOVE_TICK);
            player.getPersistentData().remove(MOVING_UNTIL);
        }
        if (oldVariant != newVariant) ModNetwork.broadcast(player, newVariant);
        // IMPORTANT: never clear an existing mob target or stop its navigation here.
    }

    /** -40% movement speed, restored as soon as the box is removed. */
    public static void applySpeed(ServerPlayer player, boolean active) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        if (!active) {
            speed.removeModifier(SLOW_ID);
        } else if (speed.getModifier(SLOW_ID) == null) {
            speed.addTransientModifier(new AttributeModifier(SLOW_ID, -0.40D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        // Vanilla step height is 0.6. Bring it down to exactly 0.5 during the
        // disguise so slabs stay traversable while higher ledges are not.
        AttributeInstance step = player.getAttribute(Attributes.STEP_HEIGHT);
        if (step != null) {
            if (!active) {
                step.removeModifier(STEP_ID);
            } else if (step.getModifier(STEP_ID) == null) {
                step.addTransientModifier(new AttributeModifier(STEP_ID, -0.10D,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
    public static int hold(Player player) { return player.getPersistentData().getInt(HOLD); }
    public static void hold(Player player, int ticks) { player.getPersistentData().putInt(HOLD, ticks); }
    public static long blockedUntil(Player player) { return player.getPersistentData().getLong(BLOCK_UNTIL); }
    public static void interrupt(ServerPlayer player, int cooldownTicks) {
        set(player, 0);
        hold(player, 0);
        player.getPersistentData().putLong(BLOCK_UNTIL, player.level().getGameTime() + cooldownTicks);
    }
}
