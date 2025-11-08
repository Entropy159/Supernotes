package com.chaosthedude.notes;

import kong.unirest.core.Unirest;
import kong.unirest.modules.gson.GsonObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.chaosthedude.notes.config.ConfigHandler;
import com.chaosthedude.notes.event.TickHandler;
import com.chaosthedude.notes.note.Note;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(Supernotes.MODID)
public class Supernotes {

	public static final String MODID = "supernotes";

	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public static Note pinnedNote;

    public static final String baseUrl = "https://api.supernotes.app/v1/";
	
	public Supernotes() {
        Unirest.config().setObjectMapper(new GsonObjectMapper());

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->  () -> {
			MinecraftForge.EVENT_BUS.register(new TickHandler());
			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
		});
		
		ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (a, b) -> true));
	}

}
