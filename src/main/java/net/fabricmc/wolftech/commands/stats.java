package net.fabricmc.wolftech.commands;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.network.MessageType;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.level.ServerWorldProperties;

public class stats {
	

	public static void Command(CommandDispatcher<ServerCommandSource> dispatcher) {

		dispatcher.register(literal("stats")
			    .then(argument("type", StringArgumentType.string()).suggests((a, b) -> CommandSource.suggestMatching(types(), b))
			    		.executes((com.mojang.brigadier.Command<ServerCommandSource>)context ->{
			    			
			    			String type = StringArgumentType.getString(context, "type");
			    			try {
				    			if(type.equalsIgnoreCase("none")){
						    		Scoreboard scoreboard = context.getSource().getMinecraftServer().getScoreboard();
						    		Team team = scoreboard.getPlayerTeam(context.getSource().getPlayer().getEntityName());
						    		scoreboard.addPlayerToTeam(context.getSource().getPlayer().getEntityName(), scoreboard.getTeam(team.getName().replace("2", "")));
						    	}else if (type.equalsIgnoreCase("show")) {
						    		Scoreboard scoreboard = context.getSource().getMinecraftServer().getScoreboard();
						    		Team team = scoreboard.getPlayerTeam(context.getSource().getPlayer().getEntityName());
						    		if (team.getName().endsWith("2")) {
						    			context.getSource().sendFeedback(new TranslatableText("Ya esta siendo mostrado el scoreboard!!").formatted(Formatting.GREEN), false);
						    		}else {
						    			scoreboard.addPlayerToTeam(context.getSource().getPlayer().getEntityName(), scoreboard.getTeam(team.getName()+"2"));
						    		}
						    	}
			    			}catch (Exception e){
			    				context.getSource().sendFeedback(new TranslatableText("error de operacion! puede ser que ya estes en el team correcto").formatted(Formatting.GREEN), false);
			    			}
			    			return 1;
			    		})
			    		.then(argument("item", ItemStackArgumentType.itemStack())
			    .executes((com.mojang.brigadier.Command<ServerCommandSource>)context ->{
			    	String type = StringArgumentType.getString(context, "type");
			    	if(type.equalsIgnoreCase("mined")) {
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "mined");
			    	}else if(type.equalsIgnoreCase("used")){
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "used");
			    	}else if(type.equalsIgnoreCase("broken")){
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "broken");
			    	}else if(type.equalsIgnoreCase("crafted")){
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "crafted");
			    	}else if(type.equalsIgnoreCase("none")){
			    		Scoreboard scoreboard = context.getSource().getMinecraftServer().getScoreboard();
			    		Team team = scoreboard.getPlayerTeam(context.getSource().getPlayer().getEntityName());
			    		scoreboard.addPlayerToTeam(context.getSource().getPlayer().getEntityName(), scoreboard.getTeam(team.getName().replace("2", "")));
			    	}else {
			    		context.getSource().sendFeedback(new TranslatableText("Syntaxys erronea! porfavor revisala").formatted(Formatting.GREEN), false);
			    		return 1;
			    	}
			    	return 1;
			    	
			    })))
			    
				
				);


	}
	
	public static Collection<String> types() {
		
		Set<String> types = Sets.newLinkedHashSet(Arrays.asList("used", "mined", "broken", "crafted", "show", "none"));
		return types;
	}
	
	
	//all this code bellow is copy of kahzerx in mod of bastion (https://github.com/Kahzerx/BastionSMP/blob/f43e65d275fc75ca72a55cb2f86221c673b0c78f/src/main/java/bastion/commands/SBCommand.java#L57)
	
	public static int showSidebar(ServerCommandSource source, ItemStackArgument item, String type) {
		Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
		Item minecraftItem = item.getItem();
		String objectiveName = type + "." + Item.getRawId(minecraftItem);
		ScoreboardObjective scoreboardObjective = scoreboard.getNullableObjective(objectiveName);

		Entity entity = source.getEntity();
		Text text;

		if (scoreboardObjective != null) {
			if (scoreboard.getObjectiveForSlot(18) == scoreboardObjective) {
				text = new LiteralText("El scoreboard esta mostrado!");
			} else {
				assert entity != null;
				text = new LiteralText(entity.getEntityName() + " ha seleccionado el scoreboard " + Formatting.YELLOW + "[" +Formatting.RESET+ scoreboardObjective.getDisplayName().asString() +Formatting.YELLOW+ "]");
				scoreboard.setObjectiveSlot(18, scoreboardObjective);
			}
		} else {
			String criteriaName = "minecraft." + type + ":minecraft." + item.getItem().toString();
			String capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
			String displayName = Formatting.AQUA+capitalize + " " +Formatting.GREEN+ minecraftItem.toString().replaceAll("_", " ");
			ScoreboardCriterion criteria = ScoreboardCriterion.createStatCriterion(criteriaName).get();

			scoreboard.addObjective(objectiveName, criteria, new LiteralText(displayName), criteria.getCriterionType());

			ScoreboardObjective newScoreboardObjective = scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
			try {
				initialize(source, newScoreboardObjective, minecraftItem, type);
			} catch (Exception e) {
				scoreboard.removeObjective(newScoreboardObjective);
				text = new LiteralText("Ha ocurrido un error al momento de seleccionar un scoreboard, intde nuevo.").formatted(Formatting.RED);
				assert entity != null;
				source.getMinecraftServer().getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, entity.getUuid());

				return 1;

			}
			scoreboard.setObjectiveSlot(18, newScoreboardObjective);
			assert entity != null;
			assert scoreboardObjective != null;
			text = new LiteralText(entity.getEntityName() + " ha seleccionado el scoreboard " + Formatting.YELLOW + "[" +Formatting.RESET+ scoreboardObjective.getDisplayName().asString() +Formatting.YELLOW+ "]");

		}
		assert entity != null;
		source.getMinecraftServer().getPlayerManager().broadcastChatMessage(text, MessageType.CHAT, entity.getUuid());
		return 1;
	}
	
	public static void initialize(ServerCommandSource source, ScoreboardObjective scoreboardObjective, Item item, String type) {
		Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
		MinecraftServer server = source.getMinecraftServer();

		File file = new File(((ServerWorldProperties)server.getOverworld().getLevelProperties()).getLevelName(), "stats");
		File[] stats = file.listFiles();
		System.out.println(stats.length);
		for (File stat: stats) {
			String fileName = stat.getName();
			String uuidString = fileName.substring(0, fileName.lastIndexOf(".json"));

			UUID uuid = UUID.fromString(uuidString);
			ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);

			Stat<?> finalStat = null;

			if (type.equalsIgnoreCase("broken")) {
				finalStat = Stats.BROKEN.getOrCreateStat(item);
			} else if (type.equalsIgnoreCase("crafted")) {
				finalStat = Stats.CRAFTED.getOrCreateStat(item);
			} else if (type.equalsIgnoreCase("mined")) {
				finalStat = Stats.MINED.getOrCreateStat(Block.getBlockFromItem(item));
			} else if (type.equalsIgnoreCase("used")) {
				finalStat = Stats.USED.getOrCreateStat(item);
			}

			String playerName;
			int value;
			if (player != null) {
				value = player.getStatHandler().getStat(finalStat);
				playerName = player.getEntityName();
			} else {
				ServerStatHandler serverStatHandler = new ServerStatHandler(server, stat);
				value = serverStatHandler.getStat(finalStat);

				GameProfile gameProfile = server.getUserCache().getByUuid(uuid);

				if (gameProfile != null) {
					playerName = gameProfile.getName();
				} else {
					continue;
				}
			}
			if (value == 0) {
				continue;
			}
			ScoreboardPlayerScore scoreboardPlayerScore = scoreboard.getPlayerScore(playerName, scoreboardObjective);
			scoreboardPlayerScore.setScore(value);
		}
	}
}
