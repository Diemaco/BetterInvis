package com.google.mariodeu.betterinvis;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.atomic.AtomicReference;

import static org.bukkit.event.entity.EntityPotionEffectEvent.Action;


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
                Bukkit.getOnlinePlayers().forEach((player -> player.showPlayer(plugin, e.getPlayer())));

                // Damage player
                ((Player) entity).damage(0.1, e.getPlayer());

                AtomicReference<PotionEffect> effect = new AtomicReference<>();

                ((Player) entity).getActivePotionEffects().forEach((potionEffect -> {
                    if (potionEffect.getType().getName().equals(PotionEffectType.INVISIBILITY.getName())) {
                        effect.set(potionEffect);
                    }
                }));

                // Remove invisibility
                ((Player) entity).removePotionEffect(PotionEffectType.INVISIBILITY);

                if (effect.get() != null) {

                    if (effect.get().getDuration() - 250 <= 0) {
                        return;
                    }

                    ItemStack item = new ItemStack(Material.POTION, 1);
                    ItemMeta itemMeta = item.getItemMeta();


                    PotionEffect eff = new PotionEffect(PotionEffectType.INVISIBILITY, effect.get().getDuration() - 250, effect.get().getAmplifier(), true, true, true);

                    ((PotionMeta) itemMeta).addCustomEffect(eff, true);

                    ((PotionMeta) itemMeta).setColor(PotionEffectType.INVISIBILITY.getColor());

                    item.setItemMeta(itemMeta);

                    ((Player) entity).getInventory().addItem(item);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        JavaPlugin plugin = Betterinvis.getPlugin(Betterinvis.class);

        if (e.getPlayer().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            Bukkit.getOnlinePlayers().forEach((player -> player.hidePlayer(plugin, e.getPlayer())));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                e.getPlayer().hidePlayer(plugin, player);
            }
        }
    }
}
