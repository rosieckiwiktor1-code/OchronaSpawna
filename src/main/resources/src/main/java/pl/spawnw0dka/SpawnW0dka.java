package pl.spawnw0dka;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class SpawnW0dka extends JavaPlugin implements Listener {

    private final Map<String, Region> regions = new HashMap<>();

    private final Map<Player, Point> firstPoint = new HashMap<>();
    private final Map<Player, Point> secondPoint = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("SpawnW0dka wlaczony!");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/sw wand");
            player.sendMessage(ChatColor.YELLOW + "/sw create <nazwa>");
            player.sendMessage(ChatColor.YELLOW + "/sw delete <nazwa>");
            player.sendMessage(ChatColor.YELLOW + "/sw list");
            return true;
        }

        if (args[0].equalsIgnoreCase("wand")) {

            ItemStack wand = new ItemStack(Material.STICK);

            ItemMeta meta = wand.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "SpawnW0dka Wand");
                wand.setItemMeta(meta);
            }

            player.getInventory().addItem(wand);

            player.sendMessage(
                    ChatColor.GREEN + "Otrzymales wand!"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {

            if (args.length < 2) {
                player.sendMessage(
                        ChatColor.RED + "/sw create <nazwa>"
                );
                return true;
            }

            Point first = firstPoint.get(player);
            Point second = secondPoint.get(player);

            if (first == null || second == null) {
                player.sendMessage(
                        ChatColor.RED + "Zaznacz najpierw dwa punkty!"
                );
                return true;
            }

            String name = args[1].toLowerCase();

            regions.put(
                    name,
                    new Region(
                            first.world,

                            Math.min(first.x, second.x),
                            Math.max(first.x, second.x),

                            Math.min(first.y, second.y),
                            Math.max(first.y, second.y),

                            Math.min(first.z, second.z),
                            Math.max(first.z, second.z)
                    )
            );

            player.sendMessage(
                    ChatColor.GREEN + "Utworzono region: " + name
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {

            if (args.length < 2) {
                return true;
            }

            regions.remove(args[1].toLowerCase());

            player.sendMessage(
                    ChatColor.GREEN + "Usunieto region!"
            );

            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {

            player.sendMessage(
                    ChatColor.GOLD + "Regiony: " + regions.keySet()
            );

            return true;
        }

        return true;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType()
                != Material.STICK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        event.setCancelled(true);

        Point point = new Point(
                block.getWorld(),
                block.getX(),
                block.getY(),
                block.getZ()
        );

        if (event.getAction().isLeftClick()) {

            firstPoint.put(player, point);

            player.sendMessage(
                    ChatColor.GREEN + "Pierwszy punkt zaznaczony!"
            );
        }

        if (event.getAction().isRightClick()) {

            secondPoint.put(player, point);

            player.sendMessage(
                    ChatColor.GREEN + "Drugi punkt zaznaczony!"
            );
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        if (isInRegion(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        if (isInRegion(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player player) {

            if (isInRegion(player.getLocation().getBlock())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {

        if (isInRegion(event.getLocation().getBlock())) {
            event.blockList().clear();
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {

        if (isInRegion(event.getBlock())) {
            event.blockList().clear();
        }
    }

    private boolean isInRegion(Block block) {

        for (Region region : regions.values()) {

            if (!region.world.equals(block.getWorld())) {
                continue;
            }

            if (

                    block.getX() >= region.minX
                            && block.getX() <= region.maxX

                            && block.getY() >= region.minY
                            && block.getY() <= region.maxY

                            && block.getZ() >= region.minZ
                            && block.getZ() <= region.maxZ

            ) {
                return true;
            }
        }

        return false;
    }

    private static class Point {

        World world;

        int x;
        int y;
        int z;

        Point(World world, int x, int y, int z) {

            this.world = world;

            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Region {

        World world;

        int minX;
        int maxX;

        int minY;
        int maxY;

        int minZ;
        int maxZ;

        Region(

                World world,

                int minX,
                int maxX,

                int minY,
                int maxY,

                int minZ,
                int maxZ

        ) {

            this.world = world;

            this.minX = minX;
            this.maxX = maxX;

            this.minY = minY;
            this.maxY = maxY;

            this.minZ = minZ;
            this.maxZ = maxZ;
        }
    }
}
