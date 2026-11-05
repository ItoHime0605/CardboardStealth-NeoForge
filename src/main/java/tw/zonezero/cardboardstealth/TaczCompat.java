package tw.zonezero.cardboardstealth;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.EnumSenseType;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Vector3d;

/**
 * Optional TaCZ r7 integration: this class is only loaded after checking ModList.
 * No TaCZ classes are required for players who don't install TaCZ.
 */
public final class TaczCompat {
    private TaczCompat() {}
    public static void register() {
        NeoForge.EVENT_BUS.addListener(TaczCompat::onFire);
    }

    public static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IGun;
    }

    /** Fail closed for invalid / missing gunpack index entries. */
    public static boolean isPistol(ItemStack stack) {
        if (!(stack.getItem() instanceof IGun gun)) return false;
        return TimelessAPI.getCommonGunIndex(gun.getGunId(stack))
                .map(index -> BoxPolicy.isPistolCategory(index.getType()))
                .orElse(false);
    }

    public static boolean isNonPistolGun(ItemStack stack) {
        return isGun(stack) && !isPistol(stack);
    }

    private static boolean suppressed(ServerPlayer player) {
        try {
            AttachmentCacheProperty cache = IGunOperator.fromLivingEntity(player).getCacheProperty();
            if (cache == null) return false;
            Pair<Integer, Boolean> silence = cache.getCache(SilenceModifier.ID);
            // Use TaCZ's *evaluated* attachment data, not a name containing "silencer".
            return silence != null && Boolean.TRUE.equals(silence.right());
        } catch (RuntimeException ignored) {
            // A missing cache must never bypass a gunshot sound trace.
            return false;
        }
    }

    public static void onFire(GunFireEvent event) {
        if (event.getLogicalSide() != LogicalSide.SERVER ||
                !(event.getShooter() instanceof ServerPlayer player) || !BoxState.active(player)) return;
        if (!isPistol(event.getGunItemStack())) {
            BoxState.interrupt(player, 20);
            return;
        }
        // Concealment remains active. Shot direction is not a new target lock,
        // but Zombie Awareness can still investigate the shot's sound position.
        if (ZAUtil.canSpawnTraceQuickCheck(player.level())) {
            int strength = BoxPolicy.gunshotSenseStrength(
                    ZAConfigGeneral.soundStrength, suppressed(player));
            ZAUtil.spawnOrBuffSenseAtPos(player.level(),
                    new Vector3d(player.getX(), player.getY(), player.getZ()),
                    EnumSenseType.SOUND, strength);
        }
    }
}
