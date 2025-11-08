package com.chaosthedude.notes.event;

import org.lwjgl.glfw.GLFW;

import com.chaosthedude.notes.Supernotes;

import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Supernotes.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeybindHandler {

	public  static final KeyMapping OPEN_NOTES = new KeyMapping("key.openNotes", GLFW.GLFW_KEY_N, "key.category.supernotes");

    @SubscribeEvent
    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(OPEN_NOTES);
    }

}
