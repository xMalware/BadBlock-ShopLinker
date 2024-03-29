package fr.badblock.common.shoplinker.ladder;

import java.util.Arrays;
import java.util.List;

import fr.badblock.api.common.tech.rabbitmq.RabbitConnector;
import fr.badblock.api.common.tech.rabbitmq.RabbitService;
import fr.badblock.api.common.tech.rabbitmq.setting.RabbitSettings;
import fr.badblock.common.shoplinker.api.ShopLinkerAPI;
import fr.badblock.common.shoplinker.ladder.listeners.rabbitmq.ReceiveCommandListener;
import fr.badblock.ladder.api.Ladder;
import fr.badblock.ladder.api.chat.ChatColor;
import fr.badblock.ladder.api.config.Configuration;
import fr.badblock.ladder.api.entities.CommandSender;
import fr.badblock.ladder.api.plugins.Plugin;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter public class ShopLinkerLadder extends Plugin {

	@Getter private static ShopLinkerLadder			 	 instance;
	@Getter private static CommandSender			 	 console;
	
	private RabbitService								 rabbitService;

	private String										 boughtMessage;
	private String										 animationMessage;
	private String										 webActionCompleteMessage;
	private String										 rewardMessage;

	@Override
	public void onEnable() {
		instance = this;
		console = Ladder.getInstance().getConsoleCommandSender();
		Configuration configuration = this.getConfig();
		List<String> stockList = configuration.getStringList("rabbit.hostname");
		if (stockList == null | stockList.isEmpty()) configuration.set("rabbit.hostname", Arrays.asList("example.com"));
		String[] stockArr = new String[stockList.size()];
		stockArr = stockList.toArray(stockArr);
		RabbitSettings rabbitSettings = new RabbitSettings(stockArr, getInt(configuration, "rabbit.port"),
				getString(configuration, "rabbit.username"), getString(configuration, "rabbit.virtualhost"),
				getString(configuration, "rabbit.password"), true, 30000, 60, 32);
		setRabbitService(RabbitConnector.getInstance().registerService(new RabbitService("default", rabbitSettings)));
		String queueName = getString(configuration, "queueName");
		ShopLinkerAPI.CURRENT_SERVER_NAME = queueName;
		this.webActionCompleteMessage = translate(getString(configuration, "messages.webactioncomplete"));
		this.animationMessage = translate(getString(configuration, "messages.animation"));
		ReceiveCommandListener.enabledCommands = configuration.getBoolean("enabledCommands");
		this.boughtMessage = translate(getString(configuration, "messages.bought"));
		this.rewardMessage = translate(getString(configuration, "messages.reward"));
		new ReceiveCommandListener(rabbitService, queueName);
	}
	
	@Override
	public void onDisable() {
		if (getRabbitService() != null)
			getRabbitService().remove();
	}
	
	private String translate(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}
	
	private String getString(Configuration fileConfiguration, String key, String value) {
		if (!fileConfiguration.contains(key)) {
			fileConfiguration.set(key, value);
			saveConfig();
		}
		return fileConfiguration.getString(key);
	}
	
	private String getString(Configuration fileConfiguration, String key) {
		return getString(fileConfiguration, key, "");
	}
	
	private int getInt(Configuration fileConfiguration, String key, int value) {
		if (!fileConfiguration.contains(key)) {
			fileConfiguration.set(key, value);
			saveConfig();
		}
		return fileConfiguration.getInt(key);
	}
	
	private int getInt(Configuration fileConfiguration, String key) {
		return getInt(fileConfiguration, key, 0);
	}
	
}
