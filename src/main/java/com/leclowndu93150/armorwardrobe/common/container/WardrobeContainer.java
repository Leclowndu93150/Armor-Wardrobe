package com.leclowndu93150.armorwardrobe.common.container;

import com.leclowndu93150.armorwardrobe.ArmorWardrobe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerArmorInvWrapper;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.concurrent.atomic.AtomicReference;


public class WardrobeContainer extends AbstractContainerMenu {
    private final ItemStack wardrobeItem;
    private final Player player;
    private final ItemStackHandler[] armorSets = new ItemStackHandler[3];
    private final PlayerArmorInvWrapper playerArmorInventory; // Wrapper for player armor

    public WardrobeContainer(int windowId, Inventory playerInventory, ItemStack wardrobeItem) {
        super(ArmorWardrobe.WARDROBE_CONTAINER.get(), windowId);
        this.wardrobeItem = wardrobeItem;
        this.player = playerInventory.player;
        this.playerArmorInventory = new PlayerArmorInvWrapper(playerInventory);

        for (int i = 0; i < 3; i++) {
            armorSets[i] = new ItemStackHandler(4) {
                @Override
                protected void onContentsChanged(int slot) {
                    super.onContentsChanged(slot);
                    saveToNBT(); // Save whenever a slot changes
                }
            };
        }

        loadFromNBT();

        int setSpacing = 64;
        int startX = 18;
        int startY = 18;

        for (int setIndex = 0; setIndex < 3; setIndex++) {
            for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
                final int finalArmorIndex = armorIndex;
                final EquipmentSlot requiredSlot = getEquipmentSlotForIndex(armorIndex);

                this.addSlot(new SlotItemHandler(armorSets[setIndex], armorIndex,
                        startX + setIndex * setSpacing,
                        startY + armorIndex * 18
                ) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        if (!(stack.getItem() instanceof ArmorItem armorItem)) {
                            return stack.isEmpty(); // Allow removing items
                        }
                        return armorItem.getEquipmentSlot() == requiredSlot;
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        int playerInvX = 8;
        int playerInvY = 118; // Moved down to avoid overlap
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        playerInvX + col * 18,
                        playerInvY + row * 18
                ));
            }
        }

        int hotbarY = 176; // Also move hotbar down
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col,
                    playerInvX + col * 18,
                    hotbarY
            ));
        }
    }

    public WardrobeContainer(int windowId, Inventory playerInventory, FriendlyByteBuf data) {
        this(windowId, playerInventory, findPlayerWardrobeItem(playerInventory.player));
    }

    public static ItemStack findPlayerWardrobeItem(Player player) {
        // Search main inventory first
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            ItemStack itemstack = player.getInventory().getItem(i);
            if (itemstack.getItem() == ArmorWardrobe.WARDROBE_ITEM.get()) {
                return itemstack;
            }
        }

        // If not found in inventory, try to find in curios slots
        AtomicReference<ItemStack> foundCurio = new AtomicReference<>(ItemStack.EMPTY);
        try {
            // Use the older CuriosApi approach
            if (CuriosApi.getCuriosHelper() != null) {
                CuriosApi.getCuriosHelper().getEquippedCurios(player).ifPresent(handler -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.getItem() == ArmorWardrobe.WARDROBE_ITEM.get()) {
                            foundCurio.set(stack);
                            // Cannot break from lambda, but setting it is enough
                        }
                    }
                });
            }
        } catch (Throwable e) {
        }

        return foundCurio.get(); // Return found curio or ItemStack.EMPTY
    }


    @Override
    public boolean stillValid(Player player) {
        // Check if the player still has *a* wardrobe item, not necessarily the exact instance
        // (though usually it will be). This prevents GUI closing during swaps if the item reference changes slightly.
        return !findPlayerWardrobeItem(player).isEmpty();
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemstack = slotStack.copy();

            int wardrobeSlots = 3 * 4; // 12 slots
            int playerInvStart = wardrobeSlots;
            int playerInvEnd = playerInvStart + 27; // 12 + 27 = 39
            int hotbarStart = playerInvEnd;
            int hotbarEnd = hotbarStart + 9; // 39 + 9 = 48

            if (index < wardrobeSlots) {
                if (!this.moveItemStackTo(slotStack, playerInvStart, hotbarEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= playerInvStart) {
                if (slotStack.getItem() instanceof ArmorItem armorItem) {
                    EquipmentSlot equipmentSlot = armorItem.getEquipmentSlot();
                    int targetArmorIndex = getIndexForEquipmentSlot(equipmentSlot);

                    if (targetArmorIndex != -1) {
                        boolean moved = false;
                        for (int setIndex = 0; setIndex < 3; setIndex++) {
                            int targetSlotIndex = setIndex * 4 + targetArmorIndex;
                            Slot targetSlot = this.slots.get(targetSlotIndex);
                            if (!targetSlot.hasItem() && targetSlot.mayPlace(slotStack)) {
                                if (this.moveItemStackTo(slotStack, targetSlotIndex, targetSlotIndex + 1, false)) {
                                    moved = true;
                                    break;
                                }
                            }
                        }
                        // If no empty slot found, try any valid slot (might overwrite if already occupied)
                        // Although moveItemStackTo usually doesn't overwrite unless slots can merge,
                        // armor slots typically have max size 1.
                        if (!moved) {
                            for (int setIndex = 0; setIndex < 3; setIndex++) {
                                int targetSlotIndex = setIndex * 4 + targetArmorIndex;
                                if (this.slots.get(targetSlotIndex).mayPlace(slotStack)) {
                                    if (this.moveItemStackTo(slotStack, targetSlotIndex, targetSlotIndex + 1, false)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!slotStack.isEmpty()) {
                    if (index >= playerInvStart && index < playerInvEnd) { // Player inventory -> hotbar
                        if (!this.moveItemStackTo(slotStack, hotbarStart, hotbarEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= hotbarStart && index < hotbarEnd) { // Hotbar -> player inventory
                        if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvEnd, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemstack;
    }


    @Override
    public void removed(Player player) {
        super.removed(player);
        // Saving is now handled by onContentsChanged in the ItemStackHandler
    }

    public void cycleArmor(int setIndex) {
        if (setIndex < 0 || setIndex >= armorSets.length || player.level().isClientSide) {
            return; // Only run on server
        }

        ItemStackHandler targetSet = armorSets[setIndex];

        // Perform the swap using copies
        for (int i = 0; i < 4; i++) {
            // Player armor slots are indexed differently (3=Head, 0=Feet)
            int playerArmorSlotIndex = 3 - i; // Maps 0->3, 1->2, 2->1, 3->0
            ItemStack playerArmor = player.getInventory().armor.get(playerArmorSlotIndex).copy();
            ItemStack setArmor = targetSet.getStackInSlot(i).copy();

            // Validate items before swapping - prevents swapping non-armor etc.
            boolean setArmorValid = setArmor.isEmpty() || (setArmor.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == getEquipmentSlotForIndex(i));
            boolean playerArmorValid = playerArmor.isEmpty() || (playerArmor.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == getEquipmentSlotForIndex(i));

            if (setArmorValid && playerArmorValid) {
                player.getInventory().armor.set(playerArmorSlotIndex, setArmor); // Player gets set's armor
                targetSet.setStackInSlot(i, playerArmor); // Set gets player's armor
            }
        }

        // Force sync of inventory to all clients
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        player.getInventory().setChanged();
    }

    private EquipmentSlot getEquipmentSlotForIndex(int index) {
        return switch (index) {
            case 0 -> EquipmentSlot.HEAD;
            case 1 -> EquipmentSlot.CHEST;
            case 2 -> EquipmentSlot.LEGS;
            case 3 -> EquipmentSlot.FEET;
            default -> throw new IllegalArgumentException("Invalid armor index: " + index);
        };
    }

    private int getIndexForEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 0;
            case CHEST -> 1;
            case LEGS -> 2;
            case FEET -> 3;
            default -> -1; // Not a valid armor slot
        };
    }


    private void saveToNBT() {
        if (!wardrobeItem.isEmpty()) {
            CompoundTag containerTag = wardrobeItem.getOrCreateTagElement("ArmorWardrobeData");
            ListTag setsList = new ListTag();
            for (int i = 0; i < armorSets.length; i++) {
                CompoundTag setTag = armorSets[i].serializeNBT();
                setTag.putInt("SetIndex", i); // Add index for safety, though order should be preserved
                setsList.add(setTag);
            }
            containerTag.put("ArmorSets", setsList);
        }
    }

    private void loadFromNBT() {
        if (wardrobeItem.hasTag() && wardrobeItem.getTag().contains("ArmorWardrobeData", Tag.TAG_COMPOUND)) {
            CompoundTag containerTag = wardrobeItem.getTag().getCompound("ArmorWardrobeData");
            if (containerTag.contains("ArmorSets", Tag.TAG_LIST)) {
                ListTag setsList = containerTag.getList("ArmorSets", Tag.TAG_COMPOUND);

                if (setsList.size() == armorSets.length) { // Basic validation
                    for (int i = 0; i < setsList.size(); i++) {
                        CompoundTag setTag = setsList.getCompound(i);
                        // int index = setTag.getInt("SetIndex"); // Optional check
                        armorSets[i].deserializeNBT(setTag);
                    }
                } else {
                    // Initialize empty if mismatch prevents partial loading
                    for (ItemStackHandler handler : armorSets) {
                        handler.deserializeNBT(new CompoundTag()); // Load empty state
                    }
                }
            }
        }
    }
}