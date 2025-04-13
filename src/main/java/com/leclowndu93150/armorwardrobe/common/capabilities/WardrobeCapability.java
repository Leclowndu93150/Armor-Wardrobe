package com.leclowndu93150.armorwardrobe.common.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WardrobeCapability {
    public static final Capability<WardrobeCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    // Three armor sets, each with 4 slots (head, chest, legs, feet)
    private final ItemStackHandler[] armorSets = new ItemStackHandler[3];

    public WardrobeCapability() {
        for (int i = 0; i < 3; i++) {
            armorSets[i] = new ItemStackHandler(4);
        }
    }

    public ItemStackHandler getArmorSet(int index) {
        if (index < 0 || index >= armorSets.length) {
            return null;
        }
        return armorSets[index];
    }

    public boolean r(Player player, int setIndex) {
        if (setIndex < 0 || setIndex >= armorSets.length) {
            return false;
        }

        // Temporary storage for current armor
        ItemStack[] currentArmor = new ItemStack[4];
        for (int i = 0; i < 4; i++) {
            currentArmor[i] = player.getInventory().armor.get(3 - i).copy(); // Invert index because armor is stored head-first
        }

        // Apply armor from the set
        for (int i = 0; i < 4; i++) {
            ItemStack setArmor = armorSets[setIndex].getStackInSlot(i).copy();
            player.getInventory().armor.set(3 - i, setArmor);
            armorSets[setIndex].setStackInSlot(i, currentArmor[i]);
        }

        return true;
    }

    // NBT handling for saving/loading
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag setsList = new ListTag();

        for (int i = 0; i < armorSets.length; i++) {
            CompoundTag setTag = new CompoundTag();
            setTag.put("Inventory", armorSets[i].serializeNBT());
            setTag.putInt("Index", i);
            setsList.add(setTag);
        }

        tag.put("ArmorSets", setsList);
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("ArmorSets", Tag.TAG_LIST)) {
            ListTag setsList = nbt.getList("ArmorSets", Tag.TAG_COMPOUND);

            for (int i = 0; i < setsList.size(); i++) {
                CompoundTag setTag = setsList.getCompound(i);
                int index = setTag.getInt("Index");

                if (index >= 0 && index < armorSets.length && setTag.contains("Inventory")) {
                    armorSets[index].deserializeNBT(setTag.getCompound("Inventory"));
                }
            }
        }
    }

    // Provider implementation
    public static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final WardrobeCapability instance = new WardrobeCapability();
        private final LazyOptional<WardrobeCapability> lazyOptional = LazyOptional.of(() -> instance);

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return CAPABILITY.orEmpty(cap, lazyOptional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return instance.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            instance.deserializeNBT(nbt);
        }
    }
}