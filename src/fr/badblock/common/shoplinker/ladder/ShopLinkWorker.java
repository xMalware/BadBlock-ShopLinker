package fr.badblock.common.shoplinker.ladder;

import fr.badblock.api.common.tech.rabbitmq.packet.RabbitPacket;
import fr.badblock.api.common.tech.rabbitmq.packet.RabbitPacketEncoder;
import fr.badblock.api.common.tech.rabbitmq.packet.RabbitPacketMessage;
import fr.badblock.api.common.tech.rabbitmq.packet.RabbitPacketType;
import fr.badblock.common.shoplinker.api.objects.ShopData;
import fr.badblock.common.shoplinker.bukkit.ShopLinker;
import fr.badblock.ladder.api.Ladder;
import fr.badblock.ladder.api.chat.ChatColor;
import fr.badblock.ladder.api.entities.Player;

public class ShopLinkWorker
{

	public static void workCommand(ShopData shopData)
	{
		String playerName = shopData.getPlayerName();
		Player player = Ladder.getInstance().getPlayer(playerName);
		String[] commandSplitter = shopData.getCommand().split(";");
		for (String command : commandSplitter)
		{
			command = command.replace("%player%", shopData.getPlayerName());
			ShopLinkerLadder.getConsole().forceCommand(command);
			ShopLinkerLadder.getConsole().sendMessage(ChatColor.GOLD + "[ShopLinker] " + ChatColor.RESET + "Executed command to " + shopData.getPlayerName() + ".");
			ShopLinkerLadder.getConsole().sendMessage(ChatColor.GOLD + "[ShopLinker] " + ChatColor.RESET + "Command: " + command);
		}
		if (player != null)
		{
			broadcastCommand(player, shopData);
		}
	}

	public static void broadcastCommand(Player player, ShopData shopData)
	{
		System.out.println(ShopLinker.getInstance().getNotRestrictiveGson().toJson(shopData));
		String message = "";
		switch (shopData.getDataType())
		{
		case BUY:
			message = ShopLinkerLadder.getInstance().getBoughtMessage();
			break;
		case ANIMATION:
			message = ShopLinkerLadder.getInstance().getAnimationMessage();
			break;
		case VOTE:
			message = ShopLinkerLadder.getInstance().getRewardMessage();
			break;
		case WEBACTION_COMPLETE:
			message = ShopLinkerLadder.getInstance().getWebActionCompleteMessage();
			break;
		}
		message = message.replace("%0", shopData.getPlayerName()).replace("%1", shopData.getCommand()).replace("%2", shopData.getDisplayName());
		player.getBukkitServer().broadcast(message);

		// En jeu
		if (shopData.isIngame())
		{
			// &6[Info] &b'.$joueur['pseudo'].' &aa achet� l\'offre '.parseHTML($offer["displayname"]).' &acontre '.$offer["price"].' Crystals sur le site !
			for (String broadcastMessage : ShopLinker.getInstance().getBroadcastMessage())
			{
				broadcastMessage = broadcastMessage.replace("%player%", player.getName());
				broadcastMessage = broadcastMessage.replace("%displayName%", shopData.getDisplayName());
				broadcastMessage = broadcastMessage.replace("%price%", Double.toString(shopData.getPrice()));
				broadcastMessage = ChatColor.translateAlternateColorCodes('&', broadcastMessage);
				RabbitPacketMessage rabbitPacketMessage = new RabbitPacketMessage(8640000, broadcastMessage);
				RabbitPacket rabbitPacket = new RabbitPacket(rabbitPacketMessage, "guardian.broadcast", false, RabbitPacketEncoder.UTF8, RabbitPacketType.MESSAGE_BROKER);
				ShopLinker.getInstance().getRabbitService().sendPacket(rabbitPacket);
			}
		}

	}

}
