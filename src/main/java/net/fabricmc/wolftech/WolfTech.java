package net.fabricmc.wolftech;

import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.wolftech.commands.enderchest;
import net.minecraft.server.network.ServerPlayerEntity;

public class WolfTech implements ModInitializer {
	
	
	public List<ServerPlayerEntity> playerlist;
	
	
	@Override
	public void onInitialize() {
		
		System.out.println("WolfTech mod activado");
		
	}
	
	
	
}
