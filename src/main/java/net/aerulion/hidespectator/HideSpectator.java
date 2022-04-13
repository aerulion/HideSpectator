package net.aerulion.hidespectator;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import java.util.List;
import java.util.ListIterator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;


public class HideSpectator extends JavaPlugin {

  @Override
  public void onEnable() {
    ProtocolLibrary.getProtocolManager()
        .addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, Server.PLAYER_INFO) {
          @Override
          public void onPacketSending(final @NotNull PacketEvent event) {

            final PlayerInfoAction playerInfoAction = event.getPacket().getPlayerInfoAction()
                .read(0);

            if (!(playerInfoAction == PlayerInfoAction.ADD_PLAYER
                || playerInfoAction == PlayerInfoAction.UPDATE_GAME_MODE)) {
              return;
            }

            if (event.getPlayer().hasPermission("hidespectator.bypass")) {
              return;
            }

            final PacketContainer clonedPacket = event.getPacket().shallowClone();

            final List<PlayerInfoData> playerInfoDataList = clonedPacket.getPlayerInfoDataLists()
                .read(0);

            final @NotNull ListIterator<PlayerInfoData> listIterator = playerInfoDataList.listIterator();

            while (listIterator.hasNext()) {
              final PlayerInfoData data = listIterator.next();

              if (data.getGameMode() != NativeGameMode.SPECTATOR || data.getProfile().getName()
                  .equals(event.getPlayer().getName())) {
                continue;
              }

              final @NotNull PlayerInfoData modifiedInfoData = new PlayerInfoData(data.getProfile(),
                  data.getLatency(), NativeGameMode.SURVIVAL, data.getDisplayName());

              listIterator.set(modifiedInfoData);
            }
            clonedPacket.getPlayerInfoDataLists().write(0, playerInfoDataList);
            event.setPacket(clonedPacket);
          }
        });
  }
}