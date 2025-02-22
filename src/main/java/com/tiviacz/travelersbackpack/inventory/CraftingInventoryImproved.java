package com.tiviacz.travelersbackpack.inventory;

import com.tiviacz.travelersbackpack.util.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.ItemStackHandler;

public class CraftingInventoryImproved extends CraftingInventory
{
    private final ITravelersBackpackInventory inventory;
    private final ItemStackHandler craftingInventory;
    private final Container eventHandler;
    public boolean checkChanges = true;

    public CraftingInventoryImproved(ITravelersBackpackInventory inventory, Container eventHandlerIn)
    {
        super(eventHandlerIn, 3, 3);
        this.inventory = inventory;
        this.craftingInventory = inventory.getCraftingGridInventory();
        this.eventHandler = eventHandlerIn;
    }

    public int getSizeInventory()
    {
        return this.craftingInventory.getSlots();
    }

    public NonNullList<ItemStack> getStackList()
    {
        NonNullList<ItemStack> stacks = NonNullList.create();
        for(int i = 0; i < craftingInventory.getSlots(); i++)
        {
            stacks.add(i, getItem(i));
        }
        return stacks;
    }

    @Override
    public boolean isEmpty()
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            if(!getItem(i).isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index)
    {
        return index >= this.getSizeInventory() ? ItemStack.EMPTY : this.craftingInventory.getStackInSlot(index);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index)
    {
        if(index >= 0 && index < this.getSizeInventory())
        {
            ItemStack stack = getItem(index).copy();
            setItem(index, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count)
    {
        ItemStack itemstack = index >= 0 && index < getSizeInventory() && !getItem(index).isEmpty() && count > 0 ? getItem(index).split(count) : ItemStack.EMPTY;

        if(!itemstack.isEmpty())
        {
            if(checkChanges)
            {
                this.eventHandler.slotsChanged(this);
            }
            setChanged();
        }
        return itemstack;
    }

    @Override
    public void setItem(int index, ItemStack stack)
    {
        this.craftingInventory.setStackInSlot(index, stack);
        if(checkChanges)this.eventHandler.slotsChanged(this);
    }

    @Override
    public void setChanged()
    {
        if(this.inventory.getScreenID() != Reference.TRAVELERS_BACKPACK_TILE_SCREEN_ID)
        {
            this.inventory.setChanged();
        }
    }

    public boolean isUsableByPlayer(PlayerEntity player)
    {
        return true;
    }

    public void clear() { }

    @Override
    public int getHeight()
    {
        return 3;
    }

    @Override
    public int getWidth()
    {
        return 3;
    }

    @Override
    public void fillStackedContents(RecipeItemHelper helper)
    {
        for(int i = 0; i < getSizeInventory(); i++)
        {
            helper.accountSimpleStack(getItem(i));
        }
    }
}