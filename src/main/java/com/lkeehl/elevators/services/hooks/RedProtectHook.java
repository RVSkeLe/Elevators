package com.lkeehl.elevators.services.hooks;

import br.net.fabiozumbi12.RedProtect.Bukkit.API.RedProtectAPI;
import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.lkeehl.elevators.helpers.ItemStackHelper;
import com.lkeehl.elevators.models.Elevator;
import com.lkeehl.elevators.models.hooks.ProtectionHook;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RedProtectHook extends ProtectionHook {
    //TODO: Code cleanup
    private final RedProtectAPI redProtect;

    private final String flagName = "outsiders-use-elevators";
    private final String nameFlagName = "edit-name-elevators";
    private final String settingsFlagName = "edit-settings-elevators";

    public RedProtectHook() {
        super("RedProtect");
        this.redProtect = RedProtect.get().getAPI();

        this.redProtect.addFlag(flagName, true, false);
        this.redProtect.addFlag(nameFlagName, true, false);
        this.redProtect.addFlag(settingsFlagName, false, false);
    }

    @Override
    public boolean canPlayerUseElevator(Player player, Elevator elevator, boolean sendMessage) {
        if(!this.isCheckEnabled(elevator))
            return true;

        Region region = redProtect.getRegion(elevator.getShulkerBox().getLocation());
        if(region == null || region.getFlagBool(flagName))
            return true;

        if(region.isLeader(player) || region.isAdmin(player) || region.isMember(player) || player.hasPermission("redprotect.flag.bypass." + this.flagName))
            return true;

        if(sendMessage)
            player.sendMessage(ChatColor.RED + "You can't interact with this here!");
        return false;
    }

    @Override
    public boolean canEditName(Player player, Elevator elevator, boolean sendMessage) {
        Region region = redProtect.getRegion(elevator.getShulkerBox().getLocation());
        if(region == null || region.getFlagBool(nameFlagName))
            return true;

        if(region.isLeader(player) || region.isAdmin(player) || region.isMember(player) || player.hasPermission("redprotect.flag.bypass." + this.nameFlagName))
            return true;

        if(sendMessage)
            player.sendMessage(ChatColor.RED + "You can't interact with this here!");
        return false;
    }

    @Override
    public boolean canEditSettings(Player player, Elevator elevator, boolean sendMessage) {
        Region region = redProtect.getRegion(elevator.getShulkerBox().getLocation());
        if(region == null || region.getFlagBool(settingsFlagName))
            return true;

        if(region.isLeader(player) || region.isAdmin(player) || player.hasPermission("redprotect.flag.bypass." + this.settingsFlagName))
            return true;

        if(sendMessage)
            player.sendMessage(ChatColor.RED + "You can't interact with this here!");
        return false;
    }

    @Override
    public ItemStack createIconForElevator(Player player, Elevator elevator) {
        Region region = redProtect.getRegion(elevator.getLocation());
        if(region == null) return null;

        boolean flagEnabled = this.isCheckEnabled(elevator);

        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatColor.GRAY + "Controls whether claim");
        lore.add(ChatColor.GRAY + "guests are blocked from");
        lore.add(ChatColor.GRAY + "using this Elevator.");
        lore.add("");
        lore.add(ChatColor.GRAY + "Status: ");
        lore.add(flagEnabled ? (ChatColor.GREEN + "" + ChatColor.BOLD + "ENABLED") : (ChatColor.RED + "" + ChatColor.BOLD + "DISABLED") );

        return ItemStackHelper.createItem(ChatColor.RED + "" + ChatColor.BOLD + "Red Protect", Material.RED_DYE, 1, lore);
    }

    @Override
    public void onProtectionClick(Player player, Elevator elevator, Runnable onReturn) {
        this.toggleCheckEnabled(elevator);
        onReturn.run();
    }
}
