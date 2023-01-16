package onslaught.ketongu10.commands.stopWar;

import com.google.common.collect.Lists;
import onslaught.ketongu10.capabilities.ModCapabilities;
import onslaught.ketongu10.capabilities.world.WarData;
import onslaught.ketongu10.war.WarsManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

public class StopWarCommand extends CommandBase
{
    private final List<String> aliases = Lists.newArrayList("stopwars");

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Stopping all wars!"));
        WarData cap = sender.getEntityWorld().getCapability(ModCapabilities.WAR_DATA, null);
        if (cap != null) {
            ((WarsManager)cap).stopAllWars();
        }

    }

    @Override
    public String getName()
    {
        return "stopwars";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "stopwars <>";
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

