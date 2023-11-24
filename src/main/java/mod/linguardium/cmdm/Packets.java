package mod.linguardium.cmdm;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static mod.linguardium.cmdm.CustomModelDataManager.LOGGER;
import static mod.linguardium.cmdm.CustomModelDataManager.MODID;

public class Packets {
    public static Identifier CONFIG_PACKET_ID = new Identifier(MODID, "config");
    private static final Map<Identifier, ArrayList<Integer>> cmdHolder = new HashMap<>();
    private static Identifier thisItemListId = null;
    private static UUID updating = Util.NIL_UUID;
    private static final List<Integer> intHolder = new ArrayList<>();
    public enum PACKET_PHASE {
        INITIAL,
        START_ITEM,
        ADD_ITEM,
        FINALIZE_ITEM,
        FINALIZE

    }
    private static boolean hasOperatorPermissions(ServerPlayerEntity player, MinecraftServer server) {
        return isOperator(player,server) || (server.isRemote() && server.isHost(player.getGameProfile()));
    }

    private static boolean isOperator(ServerPlayerEntity player, MinecraftServer server) {
        return server != null && server.getPlayerManager().isOperator(player.getGameProfile());
    }
    private static void sendSystemMessage(MinecraftServer server, Text message) {
        if (server != null) {
            server.getPlayerManager().broadcast(message, (p) -> {
                if (isOperator(p,server)) return message;
                return null;
            }, false);
        }
    }
    public static void initialPacket(ServerPlayerEntity player) {
        if (updating != Util.NIL_UUID) {
            String updatingSender = updating.toString();
            String newSender = player.getDisplayName().getString();
            if (player.getServer() != null) {
                ServerPlayerEntity oldPlayer = player.getServer().getPlayerManager().getPlayer(updating);
                if (oldPlayer != null) {
                    oldPlayer.sendMessage(Text.empty().append(player.getDisplayName()).append(Text.literal(" attempted to restart CMD updating process!")).formatted(Formatting.RED),false);
                    updatingSender = oldPlayer.getDisplayName().getString();
                }else{
                    player.sendMessage(Text.literal("CMD updating process is already running!"));
                }
            }
            LOGGER.warn("Started update cycle during existing update cycle.");
            LOGGER.warn("{} started update cycle, but {} attempting to start again", updatingSender,newSender);
            return;
        }
        try {
            Config.getInstance().load(); // reload config
        } catch (IOException e) {
            LOGGER.warn("Failed while attempting to reload config before generation");
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
        updating = player.getUuid();
        sendSystemMessage(player.getServer(), Text.literal("Starting CMD List regeneration").formatted(Formatting.YELLOW));
        ServerTimer.timers.add(new ServerTimer.Timer("cmdupdate",()->{
            LOGGER.warn("CMD Update Timeout");
            sendSystemMessage(player.getServer(),Text.literal("CMD Update Timeout").formatted(Formatting.RED));
            updating=Util.NIL_UUID;
        },300));
        cmdHolder.clear();
        intHolder.clear();
    }
    public static void finalizePacket(ServerPlayerEntity player) {
        if (!updating.equals(player.getUuid())) return;
        ServerTimer.timers.removeIf(t->t.identifier.equals("cmdupdate"));
        MinecraftServer server = player.getServer();
        sendSystemMessage(server, Text.literal("Saving generated CMD List").formatted(Formatting.YELLOW));
        Config.getInstance().setEntries(cmdHolder);
        if (server != null) {
            server.getPlayerManager().getPlayerList().forEach(p -> {
                List<ItemStack> stackList = Config.getInstance().getUniqueItems().computeIfAbsent(p.getUuid(), u -> new ArrayList<>());
                if (stackList.isEmpty()) {
                    stackList.add(new GuiElementBuilder(Items.PLAYER_HEAD).setSkullOwner(p.getGameProfile(), p.getServer()).asStack());
                }
            });
        }
        try {
            Config.getInstance().save();
            LOGGER.info("Saved Config");
            sendSystemMessage(player.getServer(), Text.literal("CMD List regeneration complete").formatted(Formatting.GREEN));
        } catch (IOException e) {
            sendSystemMessage(player.getServer(), Text.literal("Unable to save CMD list. See Logs").formatted(Formatting.RED));
            LOGGER.warn("Unable to save config");
            LOGGER.warn(e.getMessage());
            e.printStackTrace();
        }
        updating = Util.NIL_UUID;
        cmdHolder.clear();
        intHolder.clear();
    }
    public static void itemListPacket(ServerPlayerEntity player, PACKET_PHASE phase, Identifier id, List<Integer> cmdList) {
        if (!updating.equals(player.getUuid())) return;
        if (phase.equals(PACKET_PHASE.START_ITEM)) {
            intHolder.clear();
            thisItemListId = id;
        }
        if (id != null && thisItemListId!=null && !id.equals(thisItemListId)) {
            LOGGER.warn("Currently processing {} but was sent list for {}",thisItemListId, id);
            return;
        }
        intHolder.addAll(cmdList);
    }
    public static void finalizeItemList(ServerPlayerEntity player, Identifier id) {
        if (!updating.equals(player.getUuid())) return;
        thisItemListId = null;
        if (intHolder.isEmpty()) {
            LOGGER.warn("CMD list for {} was empty", id.toString());
            cmdHolder.remove(id);
        }else{
            LOGGER.info("{} entries added for {}",intHolder.size(),id.toString());
            Set<Integer> uniqueCMD = Config.getInstance().getAllUniqueItems().stream().filter(stack-> Registries.ITEM.getId(stack.getItem()).equals(id)).map(itemstack->
                            (
                                    itemstack.hasNbt() &&
                                            itemstack.getOrCreateNbt().contains("CustomModelData",NbtElement.NUMBER_TYPE)
                            ) ? itemstack.getOrCreateNbt().getInt("CustomModelData") : 0
                    ).collect(Collectors.toSet());
            cmdHolder.put(id, new ArrayList<>(intHolder.stream().filter(i->!uniqueCMD.contains(i)).toList()));
        }
        intHolder.clear();
    }
    public static void receivePacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler ignoredHandler, PacketByteBuf buf, PacketSender ignoredResponseSender) {
        PACKET_PHASE phase = buf.readEnumConstant(PACKET_PHASE.class);
        List<Integer> list;
        switch (phase) {
            case INITIAL -> server.execute(()->initialPacket(player));
            case START_ITEM, ADD_ITEM -> {
                Identifier itemId = buf.readIdentifier();
                list = Arrays.stream(buf.readIntArray()).boxed().toList();
                server.execute(() -> itemListPacket(player,phase,itemId, list));
            }
            case FINALIZE_ITEM -> {
                Identifier itemId = buf.readIdentifier();
                server.execute(() -> finalizeItemList(player,itemId));
            }
            case FINALIZE -> server.execute(()->finalizePacket(player));
        }
    }



}
