package co.insou.spigsh;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpigSh extends JavaPlugin implements Listener
{
    private final Map<UUID, Shell> shells = new ConcurrentHashMap<>();

    @Override
    public void onEnable()
    {
        this.getCommand("sh").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void on(AsyncPlayerChatEvent event)
    {
        Shell shell = this.shells.get(event.getPlayer().getUniqueId());

        if (shell == null)
        {
            return;
        }

        event.setCancelled(true);

        shell.execute(event.getMessage());
    }

    @EventHandler
    public void on(PlayerQuitEvent event)
    {
        this.destroyShell(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (!(sender instanceof Player))
        {
            sender.sendMessage("Must be player");
            return false;
        }

        Player player = (Player) sender;

        this.shells.computeIfAbsent(player.getUniqueId(), uuid -> new Shell(uuid, this.shellLocation(args)));

        return false;
    }

    private String shellLocation(String[] args)
    {
        String shellLoc = "/bin/bash";

        if (args.length > 0)
        {
            // todo: use /usr/bin/env

            if (!args[0].contains("/"))
            {
                args[0] = "/bin/" + args[0];
            }

            shellLoc = args[0];
        }

        return shellLoc;
    }

    public void destroyShell(UUID uuid)
    {
        this.shells.remove(uuid);
    }

}
