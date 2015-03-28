package com.leav.worldtree;

import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

public class LoginEvent {
	
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {

    	event.player.addChatMessage(new ChatComponentText(event.player.getDisplayName() + " is testing chat messages"));
        
    }

}