package com.leav.worldtree;

import java.util.ArrayList;
import java.util.Arrays;
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

public class GenerateTreeCommand implements ICommand {

    private List aliases;

    public GenerateTreeCommand() {
        this.aliases = new ArrayList();
        this.aliases.add("generateTree");
        this.aliases.add("gt");
    }

    @Override
    public String getName() {
        return "generateTree";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "gt"; // TODO
    }

    @Override
    public List getAliases() {
        return this.aliases;
    }

    @Override
    public void execute(ICommandSender icommandsender, String[] astring)
            throws CommandException {

        String name = icommandsender.getName();
        EntityPlayerMP player = MinecraftServer.getServer()
                .getConfigurationManager().getPlayerByUsername(name);
        GetEyeTraceClientMessage message = new GetEyeTraceClientMessage();
        message.setCmd(GenerateTreeCommand.class.getName(),
                new ArrayList<String>(Arrays.asList(astring)));
        WorldTree.network.sendTo(message, player);
    }

    @Override
    public boolean canCommandSenderUse(ICommandSender icommandsender) {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args,
            BlockPos pos) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] astring, int i) {
        return false;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
