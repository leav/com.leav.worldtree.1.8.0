package com.leav.worldtree;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class GetEyeTraceServerMessage implements IMessage {

	public String player;
	public int dimension;
	public boolean validPos;
	public BlockPos pos;
	public String cmd;
	public ArrayList<String> cmdArgs = new ArrayList<String>();
	
	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO Auto-generated method stub
		player = ByteBufUtils.readUTF8String(buf);
		dimension = ByteBufUtils.readVarShort(buf);
		validPos = ByteBufUtils.readVarShort(buf) == 1 ? true : false;
		pos = new BlockPos(ByteBufUtils.readVarInt(buf, 5),
				ByteBufUtils.readVarInt(buf, 5),
				ByteBufUtils.readVarInt(buf, 5));
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
		ByteBufUtils.writeUTF8String(buf, player);
		ByteBufUtils.writeVarShort(buf, dimension);
		ByteBufUtils.writeVarShort(buf, validPos ? 1 : 0);
		ByteBufUtils.writeVarInt(buf, pos.getX(), 5);
		ByteBufUtils.writeVarInt(buf, pos.getY(), 5);
		ByteBufUtils.writeVarInt(buf, pos.getZ(), 5);
		ByteBufUtils.writeUTF8String(buf, cmd);
		ByteBufUtils.writeVarInt(buf, cmdArgs.size(), 5);
		for (String s : cmdArgs)
		{
			ByteBufUtils.writeUTF8String(buf, s);
		}
	}
	
	public static class Handler implements IMessageHandler<GetEyeTraceServerMessage, IMessage> {
	
		@Override
		public IMessage onMessage(GetEyeTraceServerMessage message, MessageContext ctx) {
//			World world = DimensionManager.getWorld(message.dimension);
//			if (world == null)
//			{
//				return null;
//			}
			
			WorldTree.processGetEyeTraceCmd(message.player, message.cmd, message.cmdArgs, message.dimension,
					message.validPos, message.pos);
		    return null;
		}
	}
	
	public void setPos(int dimension, BlockPos pos)
	{
		this.dimension = dimension;
		this.pos = new BlockPos(pos);
	}
	
	public void setCmd(String sCmd, ArrayList<String> aArgs) {
		cmd = sCmd;
		cmdArgs = new ArrayList<String>(aArgs);
	}
}
