package fr.badblock.common.shoplinker.api.objects;

public enum ShopType {

	VOTE,
	BUY,
	ANIMATION,
	WEBACTION_COMPLETE;

	public static ShopType getFrom(String type) {
		for (ShopType shopType : values())
			if (type.equalsIgnoreCase(shopType.name())) return shopType;
		return null;
	}
	
}
