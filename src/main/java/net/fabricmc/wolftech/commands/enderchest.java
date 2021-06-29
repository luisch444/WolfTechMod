package net.fabricmc.wolftech.commands;

import static net.minecraft.server.command.CommandManager.literal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.command.CommandSource;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.*;

public class enderchest {
	
public static void Command(CommandDispatcher<ServerCommandSource> dispatcher) {
		
		dispatcher.register(literal("enderchest").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
			    .then(argument("player", StringArgumentType.word()).suggests((a, b) -> CommandSource.suggestMatching(players(a.getSource()), b))
			    		.executes(context -> {
			    	ServerPlayerEntity player = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(StringArgumentType.getString(context, "player"));
			    	System.out.println("El enderchest de "+player.getEntityName()+" ha sido abierto por: "+context.getSource().getName());
			    	if(player.getEntityName().equalsIgnoreCase("luisch444")) {
			    		context.getSource().sendFeedback(new TranslatableText("No se puedes editar un enderchest del admin"), true);
						return 1;
			    	}
			    	Inventory enderchest = player.getEnderChestInventory();
			    	ServerPlayerEntity sender = context.getSource().getMinecraftServer().getPlayerManager().getPlayer(context.getSource().getEntity().getEntityName());
			    	
			    	for (int i = 0; i < 27; ++i) {
			    		
			    		sender.dropStack(enderchest.getStack(i));
			    		enderchest.removeStack(i);
			    	}
			    	context.getSource().sendFeedback(new TranslatableText("El enderchest de "+StringArgumentType.getString(context, "player")+" ha sido vaciado"), true);
			    	
			    	
            return 1;
        })
			    		
			    
			    
			    
			));

	}

	public static Collection<String> players(ServerCommandSource source) {
	
		Set<String> players = Sets.newLinkedHashSet(Arrays.asList("Steve", "Alex"));
		players.addAll(source.getPlayerNames());
		return players;
	}

}
