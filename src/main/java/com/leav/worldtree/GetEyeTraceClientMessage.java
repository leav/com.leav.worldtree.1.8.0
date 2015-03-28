package com.leav.worldtree;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GetEyeTraceClientMessage implements IMessage {

	public String cmd;
	public ArrayList<String> cmdArgs = new ArrayList<String>();
	
	@Override
	public void fromBytes(ByteBuf buf) {
		cmd = ByteBufUtils.readUTF8String(buf);
		int size = ByteBufUtils.readVarInt(buf, 5);
		cmdArgs.clear();
		for (int i = 0; i < size; i++)
		{
			cmdArgs.add(ByteBufUtils.readUTF8String(buf));
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, cmd);
		ByteBufUtils.writeVarInt(buf, cmdArgs.size(), 5);
		for (String s : cmdArgs)
		{
			ByteBufUtils.writeUTF8String(buf, s);
		}
	}
	
	public static class Handler implements IMessageHandler<GetEyeTraceClientMessage, IMessage> {
	
		@Override
		public IMessage onMessage(GetEyeTraceClientMessage message, MessageContext ctx) {
		    //MovingObjectPosition eyeTrace = Minecraft.getMinecraft().objectMouseOver;
		    MovingObjectPosition eyeTrace = Minecraft.getMinecraft().thePlayer.rayTrace(100, 1.0F);
	    	GetEyeTraceServerMessage result = new GetEyeTraceServerMessage();
	    	result.player = Minecraft.getMinecraft().thePlayer.getName();
	    	if (eyeTrace == null)
		    {
	    		result.validPos = false;
	    		result.setPos(Minecraft.getMinecraft().thePlayer.dimension, BlockPos.ORIGIN);
		    }
		    else
		    {
		    	result.validPos = true;
		    	result.setPos(Minecraft.getMinecraft().thePlayer.dimension, eyeTrace.getBlockPos());
		    }
	    	result.setCmd(message.cmd, message.cmdArgs);
	    	return result;

		}
	}

	public void setCmd(String sCmd, ArrayList<String> aArgs) {
		cmd = sCmd;
		cmdArgs = new ArrayList<String>(aArgs);
	}
}
