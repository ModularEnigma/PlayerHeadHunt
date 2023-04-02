package com.modularenigma.EasterEggHunt.events;

import com.modularenigma.EasterEggHunt.*;
import com.modularenigma.EasterEggHunt.EasterEggHuntMain;
import com.modularenigma.EasterEggHunt.EggChatController;
import com.modularenigma.EasterEggHunt.EggWorldController;
import com.modularenigma.EasterEggHunt.EggHatController;
import com.modularenigma.EasterEggHunt.helpers.EggMileStone;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Map;

public class EggFindEvent implements Listener {
    private final EasterEggHuntMain plugin;
    private final EggWorldController eggWorldController;
    private final EggChatController eggChatController;
    private final EggHatController eggHatController;
    private final EggScoreboardController eggScoreboardController;
    private final Map<Integer, EggMileStone> milestones;

    public EggFindEvent(EasterEggHuntMain plugin, EggWorldController eggWorldController,
                        EggChatController eggChatController, EggHatController eggHatController,
                        EggScoreboardController eggScoreboardController) {
        this.plugin = plugin;
        this.eggWorldController = eggWorldController;
        this.eggChatController = eggChatController;
        this.eggHatController = eggHatController;
        this.eggScoreboardController = eggScoreboardController;
        this.milestones = plugin.config().getEggMilestones();
    }

    @EventHandler
    public void onEggFind(PlayerInteractEvent event) {
        if (!isFindEggEvent(event))
            return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        int x = block.getX(); // Can't be null. Would have been found by isFindEggEvent
        int y = block.getY();
        int z = block.getZ();
        if (EggQuery.hasAlreadyCollectedEgg(plugin, player, x, y, z)) {
            eggChatController.eggAlreadyFoundResponse(player);
            return;
        }

        eggWorldController.playerCollectedEgg(player, block, x, y, z);

        int foundEggs = EggQuery.foundEggsCount(plugin, player);
        eggScoreboardController.reloadScoreboard(player, foundEggs);

        if (foundEggs == 1) {
            eggChatController.eggMilestoneReachedEvent(player, false, foundEggs);
            return;
        }

        // Trigger any milestones if they are relevant. We'll use the milestone text if it's available
        // otherwise we'll draw the default text to the screen.
        if (milestones.containsKey(foundEggs)) {
            milestones.get(foundEggs).trigger(eggChatController, eggHatController, player, event);
        } else {
            eggChatController.eggFoundResponse(player, foundEggs);
        }
    }

    private boolean isFindEggEvent(PlayerInteractEvent event) {
        if (event == null)
            return false;

        Block block = event.getClickedBlock();
        if (block == null)
            return false;

        EquipmentSlot equipSlot = event.getHand();
        if (equipSlot == null)
            return false;

        // This stops the event from firing twice, since the event fires for each hand.
        if (equipSlot.equals(EquipmentSlot.OFF_HAND) ||
                event.getAction().equals(Action.LEFT_CLICK_BLOCK) ||
                event.getAction().equals(Action.LEFT_CLICK_AIR) ||
                event.getAction().equals(Action.RIGHT_CLICK_AIR))
            return false;

        // Only continue if we clicked on an egg
        String blockType = "" + block.getType();
        return plugin.config().getEggBlock().equals(blockType);
    }
}