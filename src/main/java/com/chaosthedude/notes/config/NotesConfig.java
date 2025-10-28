package com.chaosthedude.notes.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.chaosthedude.notes.Supernotes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class NotesConfig {
	
	private static Path configFilePath;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static String dateFormat = "M/d/yy h:mm a";
	public static String pinnedNotePosition = "center_right";
	public static double pinnedWidthScale = 0.2;
	public static double pinnedHeightScale = 1.0;
	public static boolean wrapNote = true;
    public static String apiKey = "API_KEY_HERE";
	
	public static void load() {
		Reader reader;
		if(getFilePath().toFile().exists()) {
			try {
				reader = Files.newBufferedReader(getFilePath());
				
				Data data = gson.fromJson(reader, Data.class);
				
				dateFormat = data.dateFormat;
				pinnedNotePosition = data.pinnedNotePosition;
				pinnedWidthScale = data.pinnedWidthScale;
				pinnedHeightScale = data.pinnedHeightScale;
				wrapNote = data.wrapNote;
                apiKey = data.apiKey;
				
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		save();
	}
	
	public static void save() {
		try {
			Writer writer = Files.newBufferedWriter(getFilePath());
			Data data = new Data(dateFormat, pinnedNotePosition, pinnedWidthScale, pinnedHeightScale, wrapNote, apiKey);
			gson.toJson(data, writer);
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Path getFilePath() {
		if(configFilePath == null) {
			configFilePath = FabricLoader.getInstance().getConfigDir().resolve(Supernotes.MODID + ".json");
		}
		return configFilePath;
	}
	
	private static class Data {
		private final String dateFormatComment = "The date format used in timestamps. Uses Java SimpleDateFormat conventions.";
		private final String dateFormat;

		private final String pinnedNotePositionComment = "The HUD position of a pinned note. Values: top_left, top_right, center_left, center_right, bottom_left, bottom_right";
		private final String pinnedNotePosition;

		private final String pinnedWidthScaleComment = "The maximum width of a pinned note relative to the screen's width.";
		private final double pinnedWidthScale;

		private final String pinnedHeightScaleComment = "The maximum percentage of the screen's display height that a pinned note can take up.";
		private final double pinnedHeightScale;

		private final String wrapNoteComment = "Determines whether displayed notes will be word wrapped.";
		private final boolean wrapNote;

        private final String apiKeyComment = "Supernotes API key";
        private final String apiKey;
		
		private Data() {
			dateFormat = "M/d/yy h:mm a";
			pinnedNotePosition = "center_right";
			pinnedWidthScale = 0.2;
			pinnedHeightScale = 1.0;
			wrapNote = true;
            apiKey = "API_KEY_HERE";
		}
		
		private Data(String dateFormat, String pinnedNotePosition, double pinnedWidthScale, double pinnedHeightScale, boolean wrapNote, String apiKey) {
			this.dateFormat = dateFormat;
			this.pinnedNotePosition = pinnedNotePosition;
			this.pinnedWidthScale = pinnedWidthScale;
			this.pinnedHeightScale = pinnedHeightScale;
			this.wrapNote = wrapNote;
            this.apiKey = apiKey;
		}
	}

}