package com.chaosthedude.notes.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigHandler {
	
	private static final ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

	public static final Client CLIENT = new Client(CLIENT_BUILDER);
	
	public static final ForgeConfigSpec CLIENT_SPEC = CLIENT_BUILDER.build();
	
	public static class Client {
		public final ForgeConfigSpec.ConfigValue<String> dateFormat;
		public final ForgeConfigSpec.ConfigValue<String> pinnedNotePosition;
		public final ForgeConfigSpec.DoubleValue pinnedWidthScale;
		public final ForgeConfigSpec.DoubleValue pinnedHeightScale;
		public final ForgeConfigSpec.BooleanValue wrapNote;
        public final ForgeConfigSpec.ConfigValue<String> apiKey;
		
		Client(ForgeConfigSpec.Builder builder) {
			String desc;
			builder.push("Client");
			
			desc = "The date format used in timestamps. Uses Java SimpleDateFormat conventions.";
			dateFormat = builder.comment(desc).define("dateFormat", "M/d/yy h:mm a");
	
			desc = "The HUD position of a pinned note. Values: top_left, top_right, center_left, center_right, bottom_left, bottom_right";
			pinnedNotePosition = builder.comment(desc).define("pinnedNotePosition", "center_right");
	
			desc = "The maximum width of a pinned note relative to the screen's width.";
			pinnedWidthScale = builder.comment(desc).defineInRange("pinnedWidthScale", 0.2, 0.05, 1.0);
	
			desc = "The maximum percentage of the screen's display height that a pinned note can take up.";
			pinnedHeightScale = builder.comment(desc).defineInRange("pinnedHeightScale", 1.0, 0.05, 1.0);
	
			desc = "Determines whether displayed notes will be word wrapped.";
			wrapNote = builder.comment(desc).define("wrapNote", true);

            desc = "API Key for Supernotes";
            apiKey = builder.comment(desc).define("apiKey", "API_KEY_HERE");
			
			builder.pop();
		}

	}

}