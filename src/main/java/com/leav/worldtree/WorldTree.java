package com.leav.worldtree;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "leavworldtree", name = "Leav's World Tree Mod", version = "1.8.0-0.1")
// @NetworkMod(clientSideRequired=true) // not used in 1.7
public class WorldTree {

    public static SimpleNetworkWrapper network;

    // The instance of your mod that Forge uses.
    @Instance(value = "leavworldtree")
    public static WorldTree instance;

    // Says where the client and server 'proxy' code is loaded.
    @SidedProxy(clientSide = "com.leav.worldtree.client.ClientProxy", serverSide = "com.leav.worldtree.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    // used in 1.6.2
    // @PreInit // used in 1.5.2
    public void preInit(FMLPreInitializationEvent event) {
        network = NetworkRegistry.INSTANCE
                .newSimpleChannel("LeavWorldTreeChannel");
        network.registerMessage(GetEyeTraceClientMessage.Handler.class,
                GetEyeTraceClientMessage.class, 0, Side.CLIENT);
        network.registerMessage(GetEyeTraceServerMessage.Handler.class,
                GetEyeTraceServerMessage.class, 1, Side.SERVER);

        MathHelper.test();
    }

    @EventHandler
    // used in 1.6.2
    // @Init // used in 1.5.2
    public void load(FMLInitializationEvent event) {
        proxy.registerRenderers();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new GenerateTreeCommand());
        event.registerServerCommand(new RevertCommand());
    }

    @EventHandler
    // used in 1.6.2
    // @PostInit // used in 1.5.2
    public void postInit(FMLPostInitializationEvent event) {
        // Stub Method
    }

    
    public static class ParseArgsResult {
        TreeMap<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        ArrayList<String> anonymous = new ArrayList<String>();
    }
    
    public static ParseArgsResult parseArgs(Collection<String> args) {

        ParseArgsResult result = new ParseArgsResult();
        Pattern pattern = Pattern.compile("^(.*?)=(.*)$");
        for (String arg : args) {
            Matcher matcher = pattern.matcher(arg);
            if (matcher.matches()) {
                result.map.put(matcher.group(1), matcher.group(2));
            }
            else {
                result.anonymous.add(arg);
            }
        }
        return result;
    }

    public static void processAnonymousArg(Object o, String arg)
    {
        Class annotation = CommandLineAnonymous.class;
        for (Field field : o.getClass().getDeclaredFields()) {
            if (field.getAnnotation(annotation) != null) {
                try {
                    field.set(o, arg);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static void processArgs(Object o, Class annotation, Map<String, String> args) {
        for (Field field : o.getClass().getDeclaredFields()) {
            if (field.getAnnotation(annotation) != null) {
                String value = args.get(field.getName());
                if (value != null) {
                    if (field.getType().isAssignableFrom(Double.TYPE)) {
                        try {
                            field.set(o, Double.parseDouble(value));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (field.getType().isAssignableFrom(Integer.TYPE)) {
                        try {
                            field.set(o, Integer.parseInt(value));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (field.getType().isAssignableFrom(Vec3.class)) {
                        try {
                            field.set(o, parseVec3(value));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else if (field.getType().isAssignableFrom(String.class)) {
                        try {
                            field.set(o, value);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public static Vec3 parseVec3(String s) throws NumberFormatException {
        Pattern pattern = Pattern.compile("\\[(.+?),(.+?),(.+?)\\]");
        Matcher matcher = pattern.matcher(s);
        if (matcher.matches()) {
            Vec3 vec = new Vec3(Double.parseDouble(matcher.group(1)),
                    Double.parseDouble(matcher.group(2)),
                    Double.parseDouble(matcher.group(3)));
            return vec;
        } else {
            throw new NumberFormatException();
        }
    }

    // Custom static methods
    public static void processGetEyeTraceCmd(String playerName, String cmd,
            ArrayList<String> cmdArgs, int dimension, boolean validPos,
            BlockPos pos) {
        ParseArgsResult parsedArgs = parseArgs(cmdArgs);
//        Map<String, String> messageArgs = parseArgs(cmdArgs);
//        messageArgs.put("dimension", Integer.toString(dimension));
//        messageArgs.put("validPos", Boolean.toString(validPos));
//        messageArgs.put("pos", pos.toString());

        EntityPlayerMP player = MinecraftServer.getServer()
                .getConfigurationManager().getPlayerByUsername(playerName);
//        addChatMessage(player,
//                "received: " + cmd + " " + messageArgs.toString());
        if (cmd.equals(GenerateTreeCommand.class.getName())) {
            World world = DimensionManager.getWorld(dimension);
            if (world == null) {
                addChatMessage(player, "Failed to get dimension");
                return;
            }
            if (!validPos) {
                addChatMessage(player, "Failed to get eye trace position");
                return;
            }

            Tree tree = new Tree(world, pos);
            processArgs(tree, CommandLineEarly.class, parsedArgs.map);
            if (parsedArgs.anonymous.size() > 0)
            {
                processAnonymousArg(tree, parsedArgs.anonymous.get(0));
                tree.applyPreset();
            }
            processArgs(tree, CommandLine.class, parsedArgs.map);
            tree.generate();
        }
    }

    public static void addChatMessage(EntityPlayerMP player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

}