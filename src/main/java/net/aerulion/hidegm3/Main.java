package net.aerulion.hidegm3;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.rits.cloning.Cloner;
import net.minecraft.server.v1_16_R2.EnumGamemode;
import net.minecraft.server.v1_16_R2.PacketPlayOutPlayerInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;


public class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent e) {
                Cloner cloner = new Cloner();
                PacketContainer packet = cloner.deepClone(e.getPacket());
                PacketPlayOutPlayerInfo.EnumPlayerInfoAction action = (PacketPlayOutPlayerInfo.EnumPlayerInfoAction) getDeclaredField(packet.getHandle(), "a");

                if (action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_GAME_MODE || action == PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER) {

                    Object playerInfoData = ((List<?>) getDeclaredField(packet.getHandle(), "b")).get(0);

                    String name = (String) getDeclaredField(getDeclaredField(playerInfoData, "d"), "name");
                    Player changer = Bukkit.getServer().getPlayerExact(name);
                    if (changer == null) {
                        return;
                    }
                    if (changer.getGameMode() != GameMode.SPECTATOR) {
                        return;
                    }
                    e.setPacket(packet);
                    if (e.getPlayer().getName().equals(name)) {
                        modifyFinalField(getField(playerInfoData, "c"), playerInfoData, EnumGamemode.SPECTATOR);
                    } else {
                        modifyFinalField(getField(playerInfoData, "c"), playerInfoData, EnumGamemode.SURVIVAL);

                    }
                }
            }
        });

    }

    public static Object getDeclaredField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Field getField(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void modifyFinalField(Field field, Object target, Object newValue) {
        try {
            field.setAccessible(true);
            Field modifierField = Field.class.getDeclaredField("modifiers");
            modifierField.setAccessible(true);
            modifierField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(target, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}