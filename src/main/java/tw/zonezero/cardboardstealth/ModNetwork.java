package tw.zonezero.cardboardstealth;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public record BoxPayload(int entityId, int variant) implements CustomPacketPayload {
        public static final Type<BoxPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "state"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BoxPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BoxPayload::entityId,
                ByteBufCodecs.VAR_INT, BoxPayload::variant, BoxPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(BoxPayload.TYPE, BoxPayload.STREAM_CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                if (FMLEnvironment.dist == Dist.CLIENT) ClientBoxState.receive(payload.entityId(), payload.variant());
            });
        });
    }
    public static void broadcast(ServerPlayer player, int variant) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new BoxPayload(player.getId(), variant));
    }
    public static void send(ServerPlayer viewer, ServerPlayer subject, int variant) {
        PacketDistributor.sendToPlayer(viewer, new BoxPayload(subject.getId(), variant));
    }
}
