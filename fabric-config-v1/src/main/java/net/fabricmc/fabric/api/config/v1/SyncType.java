package net.fabricmc.fabric.api.config.v1;

import net.fabricmc.loader.api.config.SaveType;

import java.util.function.Predicate;

public enum SyncType {
	/**
	 * Does not do any syncing
	 */
	NONE(),

	/**
	 * Used to inform users of each other's config settings
	 */
	P2P(FabricSaveTypes.USER),

	/**
	 * Used to keep either logical side informed of the appropriate setting on the other
	 */
	INFO(FabricSaveTypes.USER, FabricSaveTypes.LEVEL);

	private final Predicate<SaveType> saveTypePredicate;

	SyncType(SaveType... saveTypes) {
		if (saveTypes.length == 0) {
			saveTypePredicate = t -> true;
		} else {
			saveTypePredicate = saveType -> {
				for (SaveType type : saveTypes) {
					if (type == saveType) return true;
				}

				return false;
			};
		}
	}

	public boolean matches(SaveType saveType) {
		return this.saveTypePredicate.test(saveType);
	}
}
