/*
 * CarbonChat
 *
 * Copyright (c) 2024 Josua Parks (Vicarious)
 *                    Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.draycia.carbon.common.command.commands;

import com.google.inject.Inject;
import java.util.Objects;
import net.draycia.carbon.api.channels.ChatChannel;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.channels.CarbonChannelRegistry;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import static org.incendo.cloud.minecraft.extras.RichDescription.richDescription;
import static org.incendo.cloud.parser.standard.StringParser.greedyStringParser;

@DefaultQualifier(NonNull.class)
public final class LeaveCommand extends CarbonCommand {

    private final CarbonChannelRegistry channelRegistry;
    private final CommandManager<Commander> commandManager;
    private final CarbonMessages carbonMessages;

    @Inject
    public LeaveCommand(
        final CarbonChannelRegistry channelRegistry,
        final CommandManager<Commander> commandManager,
        final CarbonMessages carbonMessages
    ) {
        this.channelRegistry = channelRegistry;
        this.commandManager = commandManager;
        this.carbonMessages = carbonMessages;
    }

    @Override
    public CommandSettings defaultCommandSettings() {
        return new CommandSettings("leave");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "leave");
    }

    @Override
    public void init() {
        final var command = this.commandManager.commandBuilder(this.commandSettings().name(), this.commandSettings().aliases())
            .required("channel", greedyStringParser(), SuggestionProvider.blocking((context, s) -> {
                final CarbonPlayer sender = ((PlayerCommander) context.sender()).carbonPlayer();
                return this.channelRegistry.keys().stream()
                    .map(this.channelRegistry::channel)
                    .filter(Objects::nonNull)
                    .filter(x -> !sender.leftChannels().contains(x.key())
                        && (x.permissions().joinPermitted(sender).permitted() || x.permissions().hearingPermitted(sender).permitted() || x.permissions().speechPermitted(sender).permitted()))
                    .map(x -> x.key().value())
                    .map(Suggestion::suggestion)
                    .toList();
            }))
            .permission("carbon.leave")
            .senderType(PlayerCommander.class)
            .commandDescription(richDescription(this.carbonMessages.commandLeaveDescription()))
            .handler(handler -> {
                final CarbonPlayer sender = handler.sender().carbonPlayer();
                final @Nullable ChatChannel channel = this.channelRegistry.channelByValue(handler.get("channel"));
                if (channel == null) {
                    this.carbonMessages.channelNotFound(sender);
                    return;
                }
                if (sender.leftChannels().contains(channel.key())) {
                    this.carbonMessages.channelAlreadyLeft(sender);
                    return;
                }
                sender.leaveChannel(channel);
                this.carbonMessages.channelLeft(sender);
            })
            .build();

        this.commandManager.command(command);
    }

}
