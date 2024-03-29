package fr.badblock.common.shoplinker.bukkit.events;

import org.bukkit.event.HandlerList;

import fr.badblock.common.shoplinker.api.objects.ShopData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xMalware
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper=false) public class ReceivedRemoteCommandEvent extends ShopDataEvent {

	private boolean cancelled;
	
	public ReceivedRemoteCommandEvent(ShopData shopData) {
		super(shopData);
	}

	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
