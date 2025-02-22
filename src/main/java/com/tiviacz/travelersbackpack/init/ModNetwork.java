package com.tiviacz.travelersbackpack.init;

import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.network.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModNetwork
{
    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(TravelersBackpack.MODID, "network");
    public static final String NETWORK_VERSION = new ResourceLocation(TravelersBackpack.MODID, "1").toString();

    public static SimpleChannel getNetworkChannel() {
        final SimpleChannel channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
                .clientAcceptedVersions(version -> true)
                .serverAcceptedVersions(version -> true)
                .networkProtocolVersion(() -> NETWORK_VERSION)
                .simpleChannel();

        channel.messageBuilder(SyncBackpackCapabilityClient.class, 1)
                .decoder(SyncBackpackCapabilityClient::decode)
                .encoder(SyncBackpackCapabilityClient::encode)
                .consumer(SyncBackpackCapabilityClient::handle)
                .add();

        channel.messageBuilder(EquipBackpackPacket.class, 2)
                .decoder(EquipBackpackPacket::decode)
                .encoder(EquipBackpackPacket::encode)
                .consumer(EquipBackpackPacket::handle)
                .add();

        channel.messageBuilder(UnequipBackpackPacket.class, 3)
                .decoder(UnequipBackpackPacket::decode)
                .encoder(UnequipBackpackPacket::encode)
                .consumer(UnequipBackpackPacket::handle)
                .add();

        channel.messageBuilder(ScreenPacket.class, 4)
                .decoder(ScreenPacket::decode)
                .encoder(ScreenPacket::encode)
                .consumer(ScreenPacket::handle)
                .add();

        channel.messageBuilder(SleepingBagPacket.class, 5)
                .decoder(SleepingBagPacket::decode)
                .encoder(SleepingBagPacket::encode)
                .consumer(SleepingBagPacket::handle)
                .add();

        channel.messageBuilder(CycleToolPacket.class, 6)
                .decoder(CycleToolPacket::decode)
                .encoder(CycleToolPacket::encode)
                .consumer(CycleToolPacket::handle)
                .add();

        channel.messageBuilder(UpdateRecipePacket.class, 7)
                .decoder(UpdateRecipePacket::decode)
                .encoder(UpdateRecipePacket::encode)
                .consumer(UpdateRecipePacket::handle)
                .add();

        return channel;
    }
}
