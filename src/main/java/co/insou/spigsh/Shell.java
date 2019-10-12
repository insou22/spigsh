package co.insou.spigsh;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.UUID;

public class Shell implements Runnable
{

    private final UUID uuid;
    private final int schedulerId;

    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;

    public Shell(UUID uuid, String shell)
    {
        this.uuid = uuid;
        this.loadShell(shell);

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        this.schedulerId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, 0, 1);
    }

    private void loadShell(String shell)
    {
        Try.to(() ->
        {
            this.process = new ProcessBuilder(shell).redirectErrorStream(true).start();

            this.reader = new BufferedReader(new InputStreamReader(this.process.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.process.getOutputStream()));
        });
    }

    public void execute(String command)
    {
        // todo: add control codes, get pid then send SIGnals

        Try.to(() ->
        {
            this.writer.write(command);
            this.writer.newLine();
            this.writer.flush();
        });
    }

    @Override
    public void run()
    {
        Try.to(() ->
        {
            if (!this.process.isAlive())
            {
                this.destroy();
                return;
            }

            if (!this.reader.ready())
            {
                return;
            }

            Player player = Bukkit.getPlayer(this.uuid);

            String line;
            while (this.reader.ready() && (line = this.reader.readLine()) != null)
            {
                player.sendMessage(line);
            }
        });
    }

    private void destroy()
    {
        Try.to(() ->
        {
            Bukkit.getScheduler().cancelTask(this.schedulerId);
            JavaPlugin.getPlugin(SpigSh.class).destroyShell(this.uuid);
            this.writer.close();
            this.reader.close();
            this.process.destroy();
        });
    }

}
