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
import net.minecraft.command.argument.IdentifierArgumentType;
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
import net.minecraft.stat.StatType;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
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
			    	switch(type){
			    		
			    	
			    	case ("mined") :
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "mined", null);
			    		break;
			    	case("used"):
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "used", null);
			    		break;
			    	case("broken"):
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "broken", null);
			    		break;
			    	case("crafted"):
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "crafted", null);
			    		break;
			    	case("dropped"):
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "dropped", null);
			    		break;
			    	case("picked_up"):
			    		showSidebar(context.getSource(), ItemStackArgumentType.getItemStackArgument(context, "item"), "picked_up", null);
			    		break;
			    	case("custom"):
			    		return 0;
			    	default: 
			    		context.getSource().sendFeedback(new TranslatableText("Syntaxys erronea! porfavor revisala").formatted(Formatting.GREEN), false);
			    		return 1;
			    	}
			    	return 1;
			    }))
			    		
			    		.then(argument("Statscustom", IdentifierArgumentType.identifier())
			    				.suggests((a, b) -> CommandSource.suggestMatching(Statscustom(), b)))
			    		.executes((com.mojang.brigadier.Command<ServerCommandSource>)context ->{
			    			context.getSource().sendFeedback(new TranslatableText("1").formatted(Formatting.GREEN), false);
							showSidebar(context.getSource(), null, "custom", IdentifierArgumentType.getIdentifier(context, "Statscustom"));
							context.getSource().sendFeedback(new TranslatableText("custom").formatted(Formatting.GREEN), false);
							return 0;
			    		})
			    		
			    		)
			    
				
				);


	}
	
	public static Collection<String> types() {
		
		Set<String> types = Sets.newLinkedHashSet(Arrays.asList("used", "mined", "broken","crafted" ,"dropped" ,"picked_up", "show", "none", "custom"));
		return types;
	}
	
	public static Collection<String> Statscustom() {
		
		Set<String> types = Sets.newLinkedHashSet(Arrays.asList());
		Stats.CUSTOM.forEach(stat ->{
			types.add(stat.getName().replace("minecraft.custom:", ""));
		});
		return types;
	}
	
	
	//all this code bellow is copy (and a little modify) of kahzerx in mod of bastion (https://github.com/Kahzerx/BastionSMP/blob/f43e65d275fc75ca72a55cb2f86221c673b0c78f/src/main/java/bastion/commands/SBCommand.java#L57)
	
	public static int showSidebar(ServerCommandSource source, ItemStackArgument item, String type, Identifier Stat) {
		source.sendFeedback(new TranslatableText("2").formatted(Formatting.GREEN), false);
		Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
		String objectiveName;
		Item minecraftItem = Item.byRawId(0);
		if(item!=null) {
			minecraftItem = item.getItem();
			objectiveName = type + "." + Item.getRawId(minecraftItem);
		}else {
			objectiveName = type + "." + Stat.toString().replace("minecraft:minecraft.", "");
		}
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
			String criteriaName = "minecraft.used:minecraft.air";
			String capitalize = "used";
			String displayName = "";
			if(item!=null) {
				source.sendFeedback(new TranslatableText("2.2").formatted(Formatting.GREEN), false);
				criteriaName = "minecraft." + type + ":minecraft." + item.getItem().toString();
				capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
				displayName = Formatting.AQUA+capitalize + " " +Formatting.GREEN+ minecraftItem.toString().replaceAll("_", " ");
			}else {
				source.sendFeedback(new TranslatableText("2.1").formatted(Formatting.GREEN), false);
				criteriaName = "minecraft." + type + ":minecraft."+Stat.toString().replace("minecraft:minecraft.", "");
				source.sendFeedback(new TranslatableText("2.1.1"+Stat.toString()).formatted(Formatting.GREEN), false);
				capitalize = type.substring(0, 1).toUpperCase() + type.substring(1);
				source.sendFeedback(new TranslatableText("2.1.2").formatted(Formatting.GREEN), false);
				displayName = Formatting.AQUA+capitalize + " " +Formatting.GREEN+ Stat.toString().replace("minecraft:minecraft.", "").replaceAll("_", " ");
				source.sendFeedback(new TranslatableText("2.1.3").formatted(Formatting.GREEN), false);
			}
			ScoreboardCriterion criteria = ScoreboardCriterion.createStatCriterion(criteriaName).get();
			source.sendFeedback(new TranslatableText("2.1.4 " + displayName).formatted(Formatting.GREEN), false);
			source.sendFeedback(new TranslatableText("2.1.4 " + objectiveName + criteria.getName()).formatted(Formatting.GREEN), false);
			scoreboard.addObjective(objectiveName, criteria, new LiteralText(displayName), criteria.getCriterionType());
			source.sendFeedback(new TranslatableText("2.1.5").formatted(Formatting.GREEN), false);
			ScoreboardObjective newScoreboardObjective = scoreboardObjective = scoreboard.getNullableObjective(objectiveName);
			try {
				source.sendFeedback(new TranslatableText("2.2").formatted(Formatting.GREEN), false);
				initialize(source, newScoreboardObjective, minecraftItem, type, Stat);
			} catch (Exception e) {
				scoreboard.removeObjective(newScoreboardObjective);
				text = new LiteralText("Ha ocurrido un error al momento de seleccionar un scoreboard, inténtelo de nuevo.").formatted(Formatting.RED);
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
	
	public static void initialize(ServerCommandSource source, ScoreboardObjective scoreboardObjective, Item item, String type, Identifier Stat) {
		Scoreboard scoreboard = source.getMinecraftServer().getScoreboard();
		MinecraftServer server = source.getMinecraftServer();
		source.sendFeedback(new TranslatableText("3").formatted(Formatting.GREEN), false);

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
			}else if (type.equalsIgnoreCase("crafted")) {
				finalStat = Stats.CRAFTED.getOrCreateStat(item);
			}else if (type.equalsIgnoreCase("dropped")) {
				finalStat = Stats.DROPPED.getOrCreateStat(item);
			}else if (type.equalsIgnoreCase("picked_up")) {
				finalStat = Stats.PICKED_UP.getOrCreateStat(item);
			}else if (type.equalsIgnoreCase("custom")) {
				finalStat = Stats.CUSTOM.getOrCreateStat(Stat);
				source.sendFeedback(new TranslatableText("4").formatted(Formatting.GREEN), false);
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