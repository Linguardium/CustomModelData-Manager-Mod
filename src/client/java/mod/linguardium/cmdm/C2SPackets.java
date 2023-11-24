package mod.linguardium.cmdm;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static mod.linguardium.cmdm.Packets.CONFIG_PACKET_ID;

public class C2SPackets {
    public static void sendInitializeListsPacket() {
        ClientPlayNetworking.send(CONFIG_PACKET_ID, PacketByteBufs.create().writeEnumConstant(Packets.PACKET_PHASE.INITIAL));
    }
    public static void sendFinalizeListsPacket() {
        ClientPlayNetworking.send(CONFIG_PACKET_ID,PacketByteBufs.create().writeEnumConstant(Packets.PACKET_PHASE.FINALIZE));
    }
    public static void sendItemList(Identifier itemId, List<Integer> cmdValues) {
        cmdListToPacketBuf(itemId,cmdValues).forEach(buf->ClientPlayNetworking.send(Packets.CONFIG_PACKET_ID,buf));
    }
    private static List<PacketByteBuf> cmdListToPacketBuf(Identifier itemId, List<Integer> cmdValues) {
        List<Integer> sendList;
        List<Integer> remainingValues = new ArrayList<>(cmdValues);
        List<PacketByteBuf> bufs = new ArrayList<>();
        while (remainingValues.size()>0) {
            sendList = remainingValues.subList(0,Math.min(200000,remainingValues.size()));
            bufs.add(PacketByteBufs.create().writeIntArray(sendList.stream().mapToInt(Integer::intValue).toArray()));
            remainingValues.removeIf(sendList::contains);
        }
        bufs.set(0,PacketByteBufs.create().writeEnumConstant(Packets.PACKET_PHASE.START_ITEM).writeIdentifier(itemId).writeBytes(bufs.get(0)));
        for (int i = 1; i<bufs.size(); i++) {
            bufs.set(i, PacketByteBufs.create().writeEnumConstant(Packets.PACKET_PHASE.ADD_ITEM).writeIdentifier(itemId).writeBytes(bufs.get(i)));
        }
        bufs.add(PacketByteBufs.create().writeEnumConstant(Packets.PACKET_PHASE.FINALIZE_ITEM).writeIdentifier(itemId));
        return bufs;
    }

}
