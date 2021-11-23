package com.google.mariodeu.betterinvis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.concurrent.atomic.AtomicReference;

import static org.bukkit.event.entity.EntityPotionEffectEvent.*;


public class Events implements Listener {
    @EventHandler
    public void onEffectChange(EntityPotionEffectEvent e) {
        JavaPlugin plugin = Betterinvis.getPlugin(Betterinvis.class);
        PotionEffectType pt = e.getModifiedType();


        if (pt.getName().equals(PotionEffectType.INVISIBILITY.getName()) && e.getEntity() instanceof Player) {

            Player modifiedPlayer = (Player) e.getEntity();

            Bukkit.getOnlinePlayers().forEach((player -> {
                if (player != e.getEntity()) {
                    if (e.getAction() == Action.ADDED) {
                        player.hidePlayer(plugin, modifiedPlayer);
                    } else if (e.getAction() == Action.REMOVED || e.getAction() == Action.CLEARED) {
                        player.showPlayer(plugin, modifiedPlayer);
                    }
                }
            }));
        }
    }

    @EventHandler
    public void onPlayerClick(PlayerInteractEvent e) {
        JavaPlugin plugin = Betterinvis.getPlugin(Betterinvis.class);


        Entity entity = e.getPlayer().getTargetEntity(3);

        if (entity instanceof Player) {
            if (!e.getPlayer().canSee((Player) entity)) {
                // Show the player to everyone
                Bukkit.getOnlinePlayers().forEach((player -> {
                        player.showPlayer(plugin, e.getPlayer());
                    }));

                // Damage player
                ((Player) entity).damage(0.1, e.getPlayer());

                // Remove invisibility
                ((Player) entity).removePotionEffect(PotionEffectType.INVISIBILITY);

                AtomicReference<PotionEffect> effect = new AtomicReference<>();

                ((Player) entity).getActivePotionEffects().forEach((potionEffect -> {
                    if (potionEffect.getType().getName().equals(PotionEffectType.INVISIBILITY.getName())) {
                        effect.set(potionEffect);
                    }
                }));

                if (effect.get() != null) {

                    ItemStack item = new ItemStack(Material.POTION, 1);
                    ItemMeta itemMeta = item.getItemMeta();

                    ((PotionMeta) itemMeta).addCustomEffect(
                            PotionEffectType.INVISIBILITY.createEffect(
                                    effect.get().getDuration(),
                                    effect.get().getAmplifier()
                            ), true);

                    ((PotionMeta) itemMeta).setColor(PotionEffectType.INVISIBILITY.getColor());

                    item.setItemMeta(itemMeta);

                    if (((Player) entity).getInventory().contains(Material.GLASS_BOTTLE)) {
                        ((Player) entity).getInventory().addItem(item);
                    }
                }
            }
        }
    }
}
