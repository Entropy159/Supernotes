package com.chaosthedude.notes.note;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.chaosthedude.notes.Supernotes;
import com.chaosthedude.notes.config.NotesConfig;
import com.chaosthedude.notes.util.FileUtils;
import com.chaosthedude.notes.util.RenderUtils;
import com.chaosthedude.notes.util.StringUtils;

import com.chaosthedude.notes.util.SupernotesUtils;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.json.JSONObject;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.resource.language.I18n;

public class Note {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(NotesConfig.dateFormat);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final TextRenderer TEXT_RENDERER = CLIENT.textRenderer;

	private String title;
	private String rawText;
	private Scope scope;
	private File prevSaveFile;
	private File saveFile;
	private File saveDir;
    private String cardId;
    private String lastModified;

	public Note(String title, String text, Scope scope) {
		this.title = title;
		this.rawText = text;
		this.scope = scope;

//		updateSaveFile();
//		prevSaveFile = saveFile;
	}

	public Note(File file) {
		update(file);
	}

	public Note setTitle(String newTitle) {
		title = newTitle;
//		updateSaveFile();
		return this;
	}

	public Note setText(String text) {
		rawText = text;
		return this;
	}

	public Note setScope(Scope newScope) {
		scope = newScope;
//		updateSaveFile();
		return this;
	}

	public String getTitle() {
		return title;
	}

	public String getRawText() {
		return rawText;
	}

	public String getFilteredText() {
		return StringUtils.filter(rawText);
	}

	public File getPrevSaveFile() {
		return prevSaveFile;
	}

	public File getSaveFile() {
		return saveFile;
	}

	public Scope getScope() {
		return scope;
	}

	public void updateSaveFile() {
		updateSaveDir();
		saveFile = new File(saveDir, getSaveName());
	}

	public void updateSaveDir() {
		if (scope != null) {
			saveDir = scope.getCurrentSaveDirectory();
		} else {
			catchNullScope();
		}
	}

	public String getPreview(int width) {
		String preview = rawText;

		for (char c : StringUtils.FILTER_CHARS) {
			if (preview.contains(String.valueOf(c))) {
				preview = preview.substring(0, preview.indexOf(String.valueOf(c)));
			}
		}
		
		if (preview.indexOf('\n') >= 0) {
			preview = preview.substring(0, preview.indexOf('\n'));
		}

		if (TEXT_RENDERER.getWidth(preview) > width) {
			preview = RenderUtils.addEllipses(preview, width);
		}

		return preview;
	}

	public long getLastModified() {
//		return saveFile.lastModified();
        if (lastModified == null) {
            return 0;
        }
        try {
            return LocalDateTime.parse(lastModified, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (Exception e) {
            return 0;
        }
	}

	public String getLastModifiedString() {
		return I18n.translate("notes.lastModified") + ": " + DATE_FORMAT.format(getLastModified());
	}

	public String getUncollidingSaveName(String name) {
		name = name.replaceAll("[\\./\"]", "_");
		File file = new File(saveDir, addFileExtension(name));
		if (!file.equals(prevSaveFile)) {
			while (file.exists()) {
				name = name + "-";
				file = new File(saveDir, addFileExtension(name));
			}
		}

		return name;
	}

	public String getSaveName() {
		String saveDirName = title.trim();
		for (char c : SharedConstants.INVALID_CHARS_LEVEL_NAME) {
			saveDirName = saveDirName.replace(c, '_');
		}

		if (saveDirName == null || saveDirName.isEmpty()) {
			saveDirName = "New Note";
		}

		return addFileExtension(getUncollidingSaveName(saveDirName));
	}

	public String addFileExtension(String name) {
		return name + ".txt";
	}

	public void save() {
//		BufferedWriter writer = null;
//		try {
//			prevSaveFile.delete();
//
//			if (!saveFile.getParentFile().exists()) {
//				saveFile.getParentFile().mkdirs();
//			}
//
//			writer = new BufferedWriter(new FileWriter(saveFile));
//			writer.write(rawText);
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				writer.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
        String tags = "\"MC Notes\"";
        if (scope == Scope.LOCAL) {
            tags += ",\"" + Scope.getWorldName() + "\"";
        }
        if (scope == Scope.REMOTE) {
            tags += ",\"" + Scope.getServerIP() + "\"";
        }
        if (cardId == null) {
            HttpResponse<JsonNode> response = SupernotesUtils.post("cards/simple", "{\"name\":\"" + title + "\",\"markup\":\"" + rawText + "\",\"tags\":[" + tags + "]}");
            if (!response.isSuccess()) {
                Supernotes.LOGGER.info("Error {} creating note: {}", response.getStatus(), response.getBody());
            }
        } else {
            HttpResponse<JsonNode> response = SupernotesUtils.patch("cards", "{\"" + cardId + "\":{\"data\":{\"name\":\"" + title + "\",\"markup\":\"" + rawText + "\",\"tags\":[" + tags + "]}}}");
            if (!response.isSuccess()) {
                Supernotes.LOGGER.info("Error {} saving note: {}", response.getStatus(), response.getBody());
            }
        }
	}

	public boolean tryOpenExternal() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().edit(getSaveFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}

	public void copy() {
		final Note copy = new Note(title + " Copy", rawText, scope);
		copy.save();
	}

	public void delete() {
		if (isPinned()) {
			Supernotes.pinnedNote = null;
		}

//		saveFile.delete();
        HttpResponse<JsonNode> response1 = SupernotesUtils.patch("cards", "{\"" + cardId + "\":{\"membership\":{\"status\":-2}}}");
        if (response1.isSuccess()) {
            HttpResponse<JsonNode> response2 = SupernotesUtils.post("cards/delete", "[\"" + cardId + "\"]");
            if (!response2.isSuccess()) {
                Supernotes.LOGGER.error("Error {} deleting note: {}", response2.getStatus(), response2.getBody());
            }
        } else {
            Supernotes.LOGGER.error("Error {} patching note: {}", response1.getStatus(), response1.getBody());
        }
	}

	public void catchNullScope() {
		scope = Scope.GLOBAL;
		saveDir = scope.getCurrentSaveDirectory();
		Supernotes.LOGGER.error("No scope found for the following note:" + getTitle() + ". Setting scope to Global.");
	}

	public void update(File file) {
//		title = FileUtils.getFileName(file);
//		scope = Scope.getScopeFromParentFile(file.getParentFile());
//		BufferedReader reader = null;
//		try {
//			reader = new BufferedReader(new FileReader(file));
//			final StringBuilder builder = new StringBuilder();
//			String line = reader.readLine();
//			while (line != null) {
//				builder.append(line);
//				builder.append(System.lineSeparator());
//				line = reader.readLine();
//			}
//			rawText = builder.toString();
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				reader.close();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		saveFile = file;
//		prevSaveFile = file;
        if (cardId != null) {
            HttpResponse<JsonNode> response = SupernotesUtils.get("cards/" + cardId);
            JSONObject json = response.getBody().getObject();
            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                title = data.getString("name");
                rawText = data.getString("markup");
                scope = getScope(data.getJSONArray("tags").toList());
                if (isPinned()) {
                    Supernotes.pinnedNote = this;
                }
            } else {
                Supernotes.LOGGER.warn("Error code {} while downloading note!", json.getInt("status"));
            }
        } else {
            Supernotes.LOGGER.info("No note card ID for note {}!", title);
        }
	}

	public void update() {
		update(saveFile);
	}

    private static Scope getScope(List<?> tags) {
        return tags.size() < 2 ? Scope.GLOBAL : (tags.contains(Scope.getWorldName()) ? Scope.LOCAL : Scope.REMOTE);
    }

    private static boolean isValidScope(List<?> tags) {
        return (Scope.getCurrentScope() == Scope.REMOTE && tags.contains(Scope.getServerIP())) || (Scope.getCurrentScope() == Scope.LOCAL && tags.contains(Scope.getWorldName()));
    }

    public static boolean isJunked(JSONObject json) {
        return json.getJSONObject("membership").getInt("status") == -2;
    }

	public boolean equals(Note note) {
//		try {
//			return note != null && note.getSaveFile() != null && saveFile.getCanonicalPath().equals(note.getSaveFile().getCanonicalPath());
//		} catch (IOException e) {
//			return false;
//		}
        return note != null && Objects.equals(note.cardId, cardId);
	}

	public boolean isPinned() {
		return equals(Supernotes.pinnedNote);
	}

	public boolean isValidScope() {
		return scope == Scope.getCurrentScope() || scope == Scope.GLOBAL;
	}

	public static List<Note> getCurrentNotes() {
		final List<Note> notes = new ArrayList<Note>();
//		if (Scope.currentScopeIsValid()) {
//			for (final File file : Scope.getCurrentScope().getCurrentSaveDirectory().listFiles()) {
//				if (FileUtils.isNote(file)) {
//					notes.add(new Note(file));
//				}
//			}
//		}
//
//		for (final File file : Scope.GLOBAL.getCurrentSaveDirectory().listFiles()) {
//			if (FileUtils.isNote(file)) {
//				notes.add(new Note(file));
//			}
//		}

        if (SupernotesUtils.hasApiKey()) {
            HttpResponse<JsonNode> response = SupernotesUtils.post("cards/get/select", "{\"filter_group\":{\"type\":\"group\",\"op\":\"and\",\"filters\":[{\"type\":\"tag\",\"op\":\"contains\",\"arg\":\"MC Notes\"}]}}");
            if (response.isSuccess()) {
                JSONObject json = response.getBody().getObject();
                for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                    String id = it.next();
                    JSONObject obj = json.getJSONObject(id).getJSONObject("data");
                    boolean global = obj.getJSONArray("tags").length() < 2;
                    if ((global || isValidScope(obj.getJSONArray("tags").toList())) && !isJunked(json.getJSONObject(id))) {
                        Note note = new Note(obj.getString("name"), obj.getString("markup"), global ? Scope.GLOBAL : Scope.LOCAL);
                        note.cardId = id;
                        note.lastModified = obj.getString("modified_when");
                        notes.add(note);
                    }
                }
            } else {
                Supernotes.LOGGER.error("Error {} ({}) getting notes: {}", response.getStatus(), response.getStatusText(), response.getBody());
            }
        }

		Collections.sort(notes, Collections.reverseOrder(new Comparator<Note>() {
			@Override
			public int compare(Note n1, Note n2) {
				return Long.compare(n1.getLastModified(), n2.getLastModified());
			}
		}));

		return notes;
	}

}
