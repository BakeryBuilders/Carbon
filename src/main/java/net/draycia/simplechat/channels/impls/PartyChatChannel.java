package net.draycia.simplechat.channels.impls;

import com.gmail.nossr50.api.PartyAPI;
import net.draycia.simplechat.SimpleChat;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class PartyChatChannel extends SimpleChatChannel {

    PartyChatChannel(TextColor color, long id, Map<String, String> formats, String webhook, boolean isDefault, boolean ignorable, String name, double distance, String switchMessage, String toggleOffMessage, String toggleOnMessage, boolean forwardFormatting, SimpleChat simpleChat) {
        super(color, id, formats, webhook, isDefault, ignorable, name, distance, switchMessage, toggleOffMessage, toggleOnMessage, forwardFormatting, simpleChat);
    }

    @Override
    public boolean canPlayerSee(OfflinePlayer offlinePlayer, Player player) {
        if (super.canPlayerSee(offlinePlayer, player) && offlinePlayer != null) {
            return PartyAPI.inSameParty((Player)offlinePlayer, player);
        }

        return false;
    }

    public static PartyChatChannel.Builder partyBuilder(String name) {
        return new PartyChatChannel.Builder(name);
    }

    public static class Builder extends SimpleChatChannel.Builder {
        private Builder(String name) {
            super(name);
        }
    }

}