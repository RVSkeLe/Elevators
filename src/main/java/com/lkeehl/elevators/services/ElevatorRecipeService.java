package com.lkeehl.elevators.services;

import com.lkeehl.elevators.helpers.VersionHelper;
import com.lkeehl.elevators.models.ElevatorRecipeGroup;
import com.lkeehl.elevators.models.ElevatorType;
import com.lkeehl.elevators.services.configs.versions.configv5.ConfigRoot;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.permissions.Permissible;

import java.util.*;

public class ElevatorRecipeService {

    private static boolean initialized = false;

    private static final Map<ElevatorType, ElevatorRecipeGroup> elevatorRecipeGroupMap = new HashMap<>();

    public static void init() {
        if(ElevatorRecipeService.initialized)
            return;

        ElevatorConfigService.addConfigCallback(root -> refreshRecipes());

        ElevatorRecipeService.initialized = true;
    }

    public static void refreshRecipes() {

        ConfigRoot root = ElevatorConfigService.getRootConfig();

        Iterator<Recipe> it = Bukkit.getServer().recipeIterator();

        List<NamespacedKey> recipesToUnlearn = new ArrayList<>();

        while (it.hasNext()) {
            Recipe recipe = it.next();
            if(!(recipe instanceof ShapedRecipe shapedRecipe))
                continue;
            if(shapedRecipe.getKey().getNamespace().equalsIgnoreCase("elevators")) {
                it.remove();
                recipesToUnlearn.add(shapedRecipe.getKey());
            }
        }

        ElevatorRecipeService.elevatorRecipeGroupMap.clear();

        if(VersionHelper.doesVersionSupportRemoveRecipe())
            recipesToUnlearn.forEach(Bukkit.getServer()::removeRecipe);

        Bukkit.getOnlinePlayers().forEach(i -> i.undiscoverRecipes(recipesToUnlearn));

        List<NamespacedKey> recipeKeys = new ArrayList<>();

        for(ElevatorType elevatorType : root.elevators.values()) {
            for(ElevatorRecipeGroup recipeGroup : elevatorType.getRecipeGroups()) {
                recipeGroup.load(elevatorType);
                ElevatorRecipeService.elevatorRecipeGroupMap.put(elevatorType, recipeGroup);
                recipeKeys.addAll(recipeGroup.getNameSpacedKeys());
            }
        }

        Bukkit.getOnlinePlayers().forEach(i -> i.discoverRecipes(recipeKeys));
    }

    public static void discoverRecipesForPlayer(Player player) {
        for(ElevatorRecipeGroup recipeGroup : ElevatorRecipeService.elevatorRecipeGroupMap.values()) {
            player.discoverRecipes(recipeGroup.getNameSpacedKeys());
        }
    }

    public static boolean doesPermissibleHaveCraftPermission(Permissible permissible, ShapedRecipe recipe) {
        return elevatorRecipeGroupMap.values().stream().anyMatch(i -> i.doesPermissibleHavePermissionForRecipe(permissible, recipe));
    }



}
