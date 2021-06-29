package net.fabricmc.wolftech.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.wolftech.commands.enderchest;
import net.fabricmc.wolftech.commands.stats;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandManager.class)
public class Command {
	
	
	@Shadow
    @Final
    private CommandDispatcher<ServerCommandSource> dispatcher;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void constructor(CommandManager.RegistrationEnvironment arg, CallbackInfo ci) {
        enderchest.Command(this.dispatcher);
        stats.Command(this.dispatcher);
        System.out.println("comando");

}
    
}
