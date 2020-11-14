package org.spongepowered.test.stream;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Plugin(StreamTest.PLUGIN_ID)
public class StreamTest implements LoadableModule {

    @Inject private PluginContainer plugin;
    @Inject private Logger logger;

    public static final String PLUGIN_ID = "streamtest";

    private final CopyPastaListener listener = new CopyPastaListener();
    private static final Map<UUID, PlayerData> player_data = new HashMap<>();

    private static PlayerData get(final Player pl) {
        PlayerData data = StreamTest.player_data.get(pl.getUniqueId());
        if (data == null) {
            data = new PlayerData(pl.getUniqueId());
            StreamTest.player_data.put(pl.getUniqueId(), data);
        }
        return data;
    }
    @Override
    public void enable(final CommandContext ctx) {
        Sponge.getEventManager().registerListeners(this.plugin, this.listener);
    }

    @Listener
    public void onGamePreInitialization(final RegisterCommandEvent<Command.Parameterized> event) {

        event.register(
            this.plugin,
            Command.builder()
                .setShortDescription(Component.text("Copies a region of the world to your clipboard"))
                .setPermission(StreamTest.PLUGIN_ID + ".command.copy")
                .setExecutor(src -> {
                    if (!(src.getCause().root() instanceof Player)) {
                        src.sendMessage(Identity.nil(), Component.text("Player only.", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final Player player = (Player) src.getCause().root();
                    final PlayerData data = StreamTest.get(player);
                    if (data.getPos1() == null || data.getPos2() == null) {
                        player.sendMessage(Identity.nil(), Component.text("You must set both positions before copying", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final Vector3i min = data.getPos1().min(data.getPos2());
                    final Vector3i max = data.getPos1().max(data.getPos2());
                    data.setOrigin(player.getBlockPosition());
                    final VolumeStream<?, BlockState> stream = player.getWorld().getBlockStateStream(
                        min,
                        max,
                        StreamOptions.lazily()
                    );
                    data.setClipboard(stream);
                    player.sendMessage(Identity.nil(), Component.text("Saved to clipboard.", NamedTextColor.GREEN));
                    return CommandResult.success();
                }).build(),
            "copy"
        );
        event.register(this.plugin,
            Command.builder()
                .setShortDescription(Component.text("Pastes your clipboard to where you are standing"))
                .setPermission(StreamTest.PLUGIN_ID + ".command.paste")
                .setExecutor(src -> {
                    if (!(src.getCause().root()  instanceof ServerPlayer)) {
                        src.sendMessage(Identity.nil(), Component.text("Player only.", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    final ServerPlayer player = (ServerPlayer) src.getCause().root();
                    final PlayerData data = StreamTest.get(player);
                    final VolumeStream<?, BlockState> volume = data.getClipboard();
                    if (volume == null) {
                        player.sendMessage(Identity.nil(), Component.text("You must copy something before pasting", NamedTextColor.RED));
                        return CommandResult.success();
                    }
                    Sponge.getServer().getCauseStackManager().pushCause(this.plugin);
                    volume
                        .apply(VolumeCollectors.of(player.getWorld(),
                            VolumePositionTranslators.offsetPosition(player.getBlockPosition(), data.getOrigin()),
                            VolumeApplicators.applyBlocks(BlockChangeFlags.ALL))
                        );
                    Sponge.getServer().getCauseStackManager().popCause();
                    src.sendMessage(Identity.nil(), Component.text("Pasted clipboard into world.", NamedTextColor.GREEN));
                    return CommandResult.success();
                }).build(),
            "paste"
        );
    }


    public static class CopyPastaListener {

        @Listener
        public void onInteract(final InteractBlockEvent.Secondary event, @Root final Player player) {
            final HandType handUsed = event.getContext().require(EventContextKeys.USED_HAND);
            event.getContext().get(EventContextKeys.USED_ITEM).ifPresent(snapshot -> {
                final BlockSnapshot block = event.getBlock();
                if (snapshot.getType().equals(ItemTypes.WOODEN_AXE.get()) && block != BlockSnapshot.empty()) {
                    if (HandTypes.MAIN_HAND.get().equals(handUsed)) {
                        StreamTest.get(player).setPos1(block.getPosition());
                        player.sendMessage(Component.text("Position 1 set to " + block.getPosition(), NamedTextColor.LIGHT_PURPLE));
                    } else if (HandTypes.OFF_HAND.get().equals(handUsed)) {
                        StreamTest.get(player).setPos2(block.getPosition());
                        player.sendMessage(Component.text("Position 2 set to " + block.getPosition(), NamedTextColor.LIGHT_PURPLE));
                    }
                    event.setCancelled(true);
                }
            });
        }
    }

    public static class PlayerData {

        private final UUID uid;
        private Vector3i pos1;
        private Vector3i pos2;
        private Vector3i origin;
        private VolumeStream<?, BlockState> clipboard;

        public PlayerData(final UUID uid) {
            this.uid = uid;
        }

        public UUID getUid() {
            return this.uid;
        }

        public Vector3i getPos1() {
            return this.pos1;
        }

        public void setPos1(final Vector3i pos) {
            this.pos1 = pos;
        }

        public Vector3i getPos2() {
            return this.pos2;
        }

        public void setPos2(final Vector3i pos) {
            this.pos2 = pos;
        }

        public VolumeStream<?, BlockState> getClipboard() {
            return this.clipboard;
        }

        public void setClipboard(final VolumeStream<?, BlockState> volume) {
            this.clipboard = volume;
        }

        public Vector3i getOrigin() {
            return this.origin;
        }

        public void setOrigin(final Vector3i origin) {
            this.origin = origin;
        }
    }
}
