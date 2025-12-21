package me.ghosttypes.reaper.util.player;

import me.ghosttypes.reaper.util.services.ResourceLoaderService;
import meteordevelopment.meteorclient.utils.misc.Names;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Interactions {

    public static int lastSlot = -1;

    // Setting velocity
    public static void setHVelocity(double x, double z) {
        mc.player.setVelocity(x, mc.player.getVelocity().getY(), z);
    }

    public static void setYVelocity(double y) {
        Vec3d velocity = mc.player.getVelocity();
        mc.player.setVelocity(velocity.x, y, velocity.z);
    }

    // Misc item methods
    public static int getSlot() {
        return mc.player.getInventory().getSelectedSlot();
    }

    public static Item getMainHandItem() {
        return mc.player.getMainHandStack().getItem();
    }

    public static Item getOffHandItem() {
        return mc.player.getOffHandStack().getItem();
    }

    public static String getItemName(Item item) {
        return Names.get(item);
    }

    public static void transfer(int from, int to, boolean hotbar) {
        if (from == -1 || to == -1) return;
        if (hotbar) InvUtils.move().from(from).toHotbar(to);
        else InvUtils.move().from(from).to(to);
    }

    public static String getCommonName(Item item) {
        if (item instanceof BedItem) return "Beds";
        if (item instanceof ExperienceBottleItem) return "XP";
        if (item instanceof EndCrystalItem) return "Crystals";
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return "EGaps";
        if (item instanceof EnderPearlItem) return "Pearls";
        if (item.equals(Items.TOTEM_OF_UNDYING)) return "Totems";
        if (Block.getBlockFromItem(item) == Blocks.OBSIDIAN) return "Obby";
        if (Block.getBlockFromItem(item) instanceof EnderChestBlock) return "Echests";
        return Names.get(item);
    }

    public static boolean isHolding(Item item) {
        return getMainHandItem().equals(item);
    }

    public static boolean isHolding(FindItemResult itemResult) {
        return isHolding(getItemFromSlot(itemResult.slot()));
    }

    public static boolean isHoldingBed() {
        return mc.player.getMainHandStack().getItem() instanceof BedItem;
    }

    // Condensed item count methods
    public static int cryCount() {
        return InvUtils.find(Items.END_CRYSTAL).count();
    }

    public static int gapCount() {
        return InvUtils.find(Items.ENCHANTED_GOLDEN_APPLE).count();
    }

    public static int xpCount() {
        return InvUtils.find(Items.EXPERIENCE_BOTTLE).count();
    }

    public static int totemCount() {
        return InvUtils.find(Items.TOTEM_OF_UNDYING).count();
    }

    public static int bedCount() {
        return InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem).count();
    }

    // Condensed find item methods
    public static FindItemResult findShulker(boolean inventory) {
        if (inventory) return InvUtils.find(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
        return InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }

    public static FindItemResult findPick() {
        return InvUtils.findInHotbar(itemStack -> itemStack.isIn(ItemTags.PICKAXES));
    }

    public static FindItemResult findSword() {
        return InvUtils.findInHotbar(itemStack -> itemStack.isIn(ItemTags.SWORDS));
    }

    public static FindItemResult findAxe() {
        return InvUtils.findInHotbar(itemStack -> itemStack.isIn(ItemTags.AXES));
    }

    public static FindItemResult findAnvil() {
        return InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock);
    }

    public static FindItemResult findButton() {
        return InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AbstractPressurePlateBlock || Block.getBlockFromItem(itemStack.getItem()) instanceof ButtonBlock);
    }

    public static FindItemResult findChorus() {
        return InvUtils.findInHotbar(Items.CHORUS_FRUIT);
    }

    public static FindItemResult findEgap() {
        return InvUtils.findInHotbar(Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static FindItemResult findObby() {
        return InvUtils.findInHotbar(Blocks.OBSIDIAN.asItem());
    }

    public static FindItemResult findEchest() {
        return InvUtils.findInHotbar(Blocks.ENDER_CHEST.asItem());
    }

    public static FindItemResult findCraftTable() {
        return InvUtils.findInHotbar(Blocks.CRAFTING_TABLE.asItem());
    }

    public static FindItemResult findXP() {
        return InvUtils.findInHotbar(Items.EXPERIENCE_BOTTLE);
    }

    public static FindItemResult findXPinAll() {
        return InvUtils.find(Items.EXPERIENCE_BOTTLE);
    }

    public static FindItemResult findBed() {
        return InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BedItem);
    }

    public static FindItemResult findBedInAll() {
        return InvUtils.find(itemStack -> itemStack.getItem() instanceof BedItem);
    }

    // Slot management
    public static void setSlot(int slot, boolean packet) {
        if (slot < 0) return;
        lastSlot = mc.player.getInventory().getSelectedSlot();
        InvUtils.swap(slot, false);
    }

    public static void swapBack() {
        setSlot(lastSlot, false);
    }

    public static void windowClick(ScreenHandler handler, int slot, SlotActionType action, int clickData) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(handler.syncId, slot, clickData, action, mc.player);
    }

    public static boolean isCrafting() {
        return mc.player.currentScreenHandler instanceof CraftingScreenHandler;
    }

    public static Integer getEmptySlots() {
        int emptySlots = 0;
        for (int i = 0; i < 36; i++) if (isSlotEmpty(i)) emptySlots++;
        return emptySlots;
    }

    public static Integer getEmptySlot() {
        for (int i = 0; i < 36; i++) if (isSlotEmpty(i)) return i;
        return -1;
    }

    public static boolean isInventoryFull() {
        for (int i = 0; i < 36; i++) if (isSlotEmpty(i)) return false;
        return true;
    }

    public static boolean isSlotEmpty(Integer slot) {
        ItemStack itemStack = getStackFromSlot(slot);
        if (itemStack == null) return true;
        return itemStack.isEmpty();
    }

    public static ItemStack getStackFromSlot(Integer slot) {
        if (slot == -1) return null;
        return mc.player.getInventory().getStack(slot);
    }

    public static Item getItemFromSlot(Integer slot) {
        if (slot == -1) return null;
        if (slot == 45) return mc.player.getOffHandStack().getItem();
        return mc.player.getInventory().getStack(slot).getItem();
    }

    // Armor stuff
    public static boolean checkThreshold(ItemStack i, double threshold) {
        return getDamage(i) <= threshold;
    }

    public static double getDamage(ItemStack i) {
        return (((double) (i.getMaxDamage() - i.getDamage()) / i.getMaxDamage()) * 100);
    }

    public static ItemStack getArmor(int slot) {
        return switch (slot) {
            case 0 -> mc.player.getEquippedStack(EquipmentSlot.FEET);
            case 1 -> mc.player.getEquippedStack(EquipmentSlot.LEGS);
            case 2 -> mc.player.getEquippedStack(EquipmentSlot.CHEST);
            case 3 -> mc.player.getEquippedStack(EquipmentSlot.HEAD);
            default -> ItemStack.EMPTY;
        };
    }

    public static Item getArmorItem(int slot) {
        return getArmor(slot).getItem();
    }

    public static boolean isInElytra() {
        return mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA && mc.player.isGliding();
    }

    public static boolean isHelm(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_HELMET || i == Items.DIAMOND_HELMET || i == Items.GOLDEN_HELMET || i == Items.IRON_HELMET || i == Items.CHAINMAIL_HELMET || i == Items.LEATHER_HELMET;
    }

    public static boolean isChest(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_CHESTPLATE || i == Items.DIAMOND_CHESTPLATE || i == Items.GOLDEN_CHESTPLATE || i == Items.IRON_CHESTPLATE || i == Items.CHAINMAIL_CHESTPLATE || i == Items.LEATHER_CHESTPLATE;
    }

    public static boolean isLegs(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_LEGGINGS || i == Items.DIAMOND_LEGGINGS || i == Items.GOLDEN_LEGGINGS || i == Items.IRON_LEGGINGS || i == Items.CHAINMAIL_LEGGINGS || i == Items.LEATHER_LEGGINGS;
    }

    public static boolean isBoots(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return false;
        Item i = itemStack.getItem();
        return i == Items.NETHERITE_BOOTS || i == Items.DIAMOND_BOOTS || i == Items.GOLDEN_BOOTS || i == Items.IRON_BOOTS || i == Items.CHAINMAIL_BOOTS || i == Items.LEATHER_BOOTS;
    }

    // Player parsing
    public static PlayerEntity getPlayerByUUID(String uuid) {
        return mc.world.getPlayerByUuid(UUID.fromString(uuid));
    }

    public static PlayerEntity getPlayerByName(String name) {
        PlayerEntity p = null;
        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity.getName().getString().equalsIgnoreCase(name)) {
                p = entity;
                break;
            }
        }
        return p;
    }

    public static String getCurrentIGN() {
        return mc.getSession().getUsername();
    }

    public static String getIGNSafe() {
        return MinecraftClient.getInstance().getSession().getUsername();
    }

    public static boolean isDeveloper() {
        return ResourceLoaderService.DEVELOPERS.contains(getIGNSafe());
    }

    public static boolean isBetaUser() {
        return false;
    }

    public static boolean isDev(String ign) {
        return ResourceLoaderService.DEVELOPERS.contains(ign);
    }

    public static boolean isBeta(String ign) {
        return ResourceLoaderService.BETA.contains(ign);
    }

    public static boolean isUser(String ign) {
        return ResourceLoaderService.USER.contains(ign);
    }

    public static String getCurrentPing() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry != null) return Integer.toString(playerListEntry.getLatency());
        return "0";
    }

    public static Integer getPing() {
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        if (playerListEntry != null) return playerListEntry.getLatency();
        return 0;
    }

    public static UUID getOwnerUUID(LivingEntity livingEntity) {
        if (livingEntity instanceof Ownable ownable) {
            Entity owner = ownable.getOwner();
            if (owner != null) return owner.getUuid();
        }
        return null;
    }

    public boolean doesPlayerOwn(Entity entity) {
        return doesPlayerOwn(entity, mc.player);
    }

    public boolean doesPlayerOwn(Entity entity, PlayerEntity playerEntity) {
        if (entity instanceof LivingEntity living) {
            UUID ownerUuid = getOwnerUUID(living);
            return ownerUuid != null && ownerUuid.equals(playerEntity.getUuid());
        }
        return false;
    }

    // Stats accessors
    public static int getKills() { return Stats.kills; }
    public static int getDeaths() { return Stats.deaths; }
    public static int getHighscore() { return Stats.highscore; }
    public static int getKillstreak() { return Stats.killStreak; }
    public static String getKD() { return Stats.getKD(); }
    public static String getPlaytime() { return Stats.getPlayTime(); }

    // Player state methods (uses Meteor's PlayerUtils)
    public static boolean isInHole() {
        return meteordevelopment.meteorclient.utils.player.PlayerUtils.isInHole(true);
    }

    public static boolean isMoving() {
        if (mc.player == null) return false;
        return mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0;
    }

    public static boolean isBurrowed() {
        if (mc.player == null) return false;
        // Check if player is standing inside a solid block
        return !mc.world.getBlockState(mc.player.getBlockPos()).isAir();
    }

    // TODO: Methods requiring BlockHelper/CombatHelper/PacketManager
    // - MineInstance class (requires PacketManager, BlockHelper)
    // - getBlockBreakingSpeed (requires enchantment API changes in 1.21)
    // - isCitied, isWebbed, isSelfTrapped, isTopTrapped (require CombatHelper/BlockHelper)
    // - mine methods (require PacketManager)
    // - findWool, findPlanks (require BlockHelper.wools, BlockHelper.planks)
}
