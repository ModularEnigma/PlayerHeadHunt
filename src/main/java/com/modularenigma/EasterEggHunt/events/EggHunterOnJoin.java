package com.modularenigma.EasterEggHunt.events;

import com.modularenigma.EasterEggHunt.EasterEggHuntMain;
import com.modularenigma.EasterEggHunt.EggController;
import com.modularenigma.EasterEggHunt.EggScoreboardController;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EggHunterOnJoin implements Listener {
    private final EasterEggHuntMain plugin;
    private final String connectionError;

    public EggHunterOnJoin(EasterEggHuntMain instance) {
        plugin = instance;

        String blankConnectionError = plugin.getConfig().getString("LANG.DATABASE.CONNECTIONERROR");
        assert blankConnectionError != null;
        connectionError = ChatColor.translateAlternateColorCodes('&', blankConnectionError);
    }

    @EventHandler
    public void onEggHunterJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String userUUID = player.getUniqueId().toString();
        String username = player.getName();

        EggScoreboardController.loadSidebarScoreboard(player);
        EggController.calculateTotalEggs();

        //
        // Database Query
        // Check if a player has been added into the database already.
        //
        try {
            PreparedStatement findstatement = plugin.getConnection().prepareStatement("SELECT * FROM playerdata WHERE uuid=?");
            findstatement.setString(1, userUUID);
            ResultSet results = findstatement.executeQuery();

            // The player already exists
            if (results.next())
                return;

            plugin.getServer().getConsoleSender().sendMessage(username + " is a new player, creating a player profile.");

            PreparedStatement insertstatement = plugin.getConnection().prepareStatement("INSERT INTO playerdata (uuid, username) VALUES (?, ?)");
            insertstatement.setString(1, userUUID);
            insertstatement.setString(2, username);
            insertstatement.executeUpdate();

            plugin.getServer().getConsoleSender().sendMessage("Added a new hunter, " + username + ".");
            newHunterApproaches(player);
        } catch (SQLException e) {
            e.printStackTrace();
            player.sendMessage(connectionError);
        }
    }

    private void newHunterApproaches(Player player) {
        player.sendMessage("" + ChatColor.BOLD + ChatColor.GREEN + "Welcome Egg Hunter.");
        player.sendMessage("Welcome Egg Hunter to the Easter Egg Hunt. Explore our Hub and the fields outside and collect as many eggs as you can.");
        player.sendMessage("Right Click to collect an Easter Egg and you will hear a ding when it is collected.");
        player.sendMessage(" ");
        player.sendMessage(ChatColor.YELLOW + "Happy Easter and happy hunting.\nFrom Crafting For Christ");
    }
}
