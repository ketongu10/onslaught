package onslaught.ketongu10.commands.stopWar;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.FactionUnits;
import onslaught.ketongu10.war.War;
import onslaught.ketongu10.war.WarsManager;

import java.util.Collections;
import java.util.List;

public class StartWarCommand extends CommandBase {
    private final List<String> aliases = Lists.newArrayList("startwar");

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.GOLD + "Enter parameters!!"));
            return;
        }
        String wartype = args[0];
        if (wartype.equals("longmarch") && args.length >= 7) {
            War.WarType type = War.WarType.LONGMARCH;
            int x1, z1, x2, z2;
            try {
                x1 = Integer.parseInt(args[1]);
                z1 = Integer.parseInt(args[2]);
                x2 = Integer.parseInt(args[3]);
                z2 = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. Enter correct path"));
                return;
            }
            String faction = args[5];
            String subfaction = null;
            int yes = 0;
            if (FactionUnits.FactionSoldiers.containsKey(faction)) {
                if (faction.equals("AW") && args.length >= 8) {
                    subfaction = args[6];
                    yes++;
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. There is no such faction"));
                return;
            }
            int delay;
            try {
                delay = Integer.parseInt(args[6 + yes]);
                if (delay < 0) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. Enter correct delay"));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. Enter correct delay"));
                return;
            }
            EntityPlayerMP player;
            if (args.length == 8 + yes) {
                player = getPlayer(server, sender, args[7 + yes]);
            } else {
                player = (EntityPlayerMP) sender.getCommandSenderEntity();
            }

            sender.sendMessage(new TextComponentString(TextFormatting.RED + "INTO THE MOTHERLAND THE GERMAN ARMY MARCH!!!"));
            WarData cap = sender.getEntityWorld().getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null) {
                ((WarsManager) cap).startLongMarch(player,  faction, subfaction, delay, x1, z1, x2, z2);
            }

        } else if (War.WarType.findByName(wartype.toUpperCase()) != null && !wartype.equals("longmarch") && args.length >= 3) {

            War.WarType type = War.WarType.findByName(wartype.toUpperCase());

            String faction = args[1];
            String subfaction = null;
            int yes = 0;
            if (FactionUnits.FactionSoldiers.containsKey(faction)) {
                if (faction.equals("AW") && args.length >= 4) {
                    subfaction = args[2]; //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    yes++;
                }
            } else {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. There is no such faction"));
                return;
            }

            int delay;
            try {
                delay = Integer.parseInt(args[2 + yes]);
                if (delay < 0) {
                    sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. Enter correct delay"));
                    return;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentString(TextFormatting.RED + "Error. Enter correct delay"));
                return;
            }
            EntityPlayerMP player;
            if (args.length == 4 + yes) {
                player = getPlayer(server, sender, args[3 + yes]);
            } else {
                player = (EntityPlayerMP) sender.getCommandSenderEntity();
            }

            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "INTO THE MOTHERLAND THE GERMAN ARMY MARCH!!!"));
            WarData cap = sender.getEntityWorld().getCapability(ModCapabilities.WAR_DATA, null);
            if (cap != null) {
                ((WarsManager) cap).startWarWithParameters(player, type, faction, subfaction, delay);
            }
        } else {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "There is no war with such parameters. Take one of these: \npatrol\nambush\nsiege\napocalypse\nlongmarch"));
            return;
        }

    }

    @Override
    public String getName()
    {
        return "startwar";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "startwar [warType] [faction] <subfaction> [delay] <player>\n" +
                "startwar longmarch [x_from] [z_from] [x_to] [z_to] [faction] <subfaction> [delay] <player>";
    }

    @Override
    public List<String> getAliases()
    {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        return true;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos targetPos)
    {
        return Collections.emptyList();
    }
}
