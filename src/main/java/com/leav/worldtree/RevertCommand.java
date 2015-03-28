package com.leav.worldtree;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MovingObjectPosition;

public class RevertCommand implements ICommand
{
  private List aliases;
  public RevertCommand()
  {
    this.aliases = new ArrayList();
    this.aliases.add("revert");
  }

  @Override
  public String getName()
  {
    return "revert";
  }

  @Override
  public String getCommandUsage(ICommandSender icommandsender)
  {
    return "revert";
  }

  @Override
  public List getAliases()
  {
    return this.aliases;
  }

  @Override
  public void execute(ICommandSender icommandsender, String[] astring)
		  throws CommandException {
	Tree.revert(icommandsender.getEntityWorld());
  }

  @Override
  public boolean canCommandSenderUse(ICommandSender icommandsender)
  {
    return true;
  }

  @Override
  public List addTabCompletionOptions(ICommandSender sender, String[] args,
  		BlockPos pos) {
  	// TODO Auto-generated method stub
  	return null;
  }

  @Override
  public boolean isUsernameIndex(String[] astring, int i)
  {
    return false;
  }

  @Override
  public int compareTo(Object o)
  {
    return 0;
  }

}