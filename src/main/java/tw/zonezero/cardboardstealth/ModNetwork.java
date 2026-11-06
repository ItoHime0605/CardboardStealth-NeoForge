package tw.zonezero.cardboardstealth;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import tw.zonezero.cardboardstealth.client.ClientBoxState;

public final class ModNetwork {
    /** All decals are synced on activation, tracking and decoration changes. */
    public record BoxPayload(int entityId, int variant, boolean rested,
                             int decal0, int decal1, int decal2) implements CustomPacketPayload {
        public static final Type<BoxPayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoxPayload> STREAM_CODEC =
                StreamCodec.of((buf, payload) -> {
                    buf.writeVarInt(payload.entityId());
                    buf.writeVarInt(payload.variant());
                    buf.writeBoolean(payload.rested());
                    buf.writeVarInt(payload.decal0());
                    buf.writeVarInt(payload.decal1());
                    buf.writeVarInt(payload.decal2());
                }, buf -> new BoxPayload(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(),
                        buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public record MeleePayload() implements CustomPacketPayload {
        public static final Type<MeleePayload> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "melee_swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, MeleePayload> STREAM_CODEC =
                StreamCodec.unit(new MeleePayload());
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("4");
        registrar.playToClient(BoxPayload.TYPE, BoxPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (FMLEnvironment.dist == Dist.CLIENT)
                    ClientBoxState.receive(payload.entityId(), payload.variant(), payload.rested(),
                            payload.decal0(), payload.decal1(), payload.decal2());
            });
        });
        registrar.playToServer(MeleePayload.TYPE, MeleePayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player && BoxState.active(player) &&
                        !(net.neoforged.fml.ModList.get().isLoaded("tacz") &&
                                TaczCompat.isPistol(player.getMainHandItem())))
                    BoxState.interrupt(player, 20);
            });
        });
    }
    private static BoxPayload create(ServerPlayer subject, int variant) {
        int[] decals = variant != 0 ? DecalItemData.get(BoxState.equippedStack(subject)) : new int[0];
        return new BoxPayload(subject.getId(), variant,
                variant != 0 && BoxState.rested(subject),
                decals.length > 0 ? decals[0] : 0,
                decals.length > 1 ? decals[1] : 0,
                decals.length > 2 ? decals[2] : 0);
    }
    public static void broadcast(ServerPlayer player, int variant) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, create(player, variant));
    }
    public static void send(ServerPlayer viewer, ServerPlayer subject, int variant) {
        PacketDistributor.sendToPlayer(viewer, create(subject, variant));
    }
}
