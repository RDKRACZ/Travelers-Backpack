package com.tiviacz.travelersbackpack.inventory.container;

import com.tiviacz.travelersbackpack.TravelersBackpack;
import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.inventory.CraftingInventoryImproved;
import com.tiviacz.travelersbackpack.inventory.ITravelersBackpackInventory;
import com.tiviacz.travelersbackpack.inventory.container.slot.BackpackSlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.container.slot.CraftResultSlotExt;
import com.tiviacz.travelersbackpack.inventory.container.slot.FluidSlotItemHandler;
import com.tiviacz.travelersbackpack.inventory.container.slot.ToolSlotItemHandler;
import com.tiviacz.travelersbackpack.items.TravelersBackpackItem;
import com.tiviacz.travelersbackpack.network.UpdateRecipePacket;
import com.tiviacz.travelersbackpack.util.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.wrapper.RecipeWrapper;

public class TravelersBackpackBaseContainer extends Container
{
    public PlayerInventory playerInventory;
    public ITravelersBackpackInventory inventory;
    public CraftingInventoryImproved craftMatrix;
    public CraftResultInventory craftResult = new CraftResultInventory();

    private final int CRAFTING_GRID_START = 1, CRAFTING_GRID_END = 9;
    private final int BACKPACK_INV_START = 10, BACKPACK_INV_END = 48;
    private final int TOOL_START = 49, TOOL_END = 50;
    private final int BUCKET_LEFT_IN = 51, BUCKET_LEFT_OUT = 52;
    private final int BUCKET_RIGHT_IN = 53, BUCKET_RIGHT_OUT = 54;
    private final int PLAYER_INV_START = 55, PLAYER_HOT_END = 90;

    public TravelersBackpackBaseContainer(final ContainerType<?> type, final int windowID, final PlayerInventory playerInventory, final ITravelersBackpackInventory inventory)
    {
        super(type, windowID);
        this.playerInventory = playerInventory;
        this.inventory = inventory;
        this.craftMatrix = new CraftingInventoryImproved(inventory, this);
        int currentItemIndex = playerInventory.selected;

        //Craft Result
        this.addCraftResult();

        //Crafting Grid, Result Slot
        this.addCraftMatrix();

        //Backpack Inventory
        this.addBackpackInventory(inventory);

        //Functional Slots
        this.addToolSlots(inventory);
        this.addFluidSlots(inventory);

        //Player Inventory
        this.addPlayerInventoryAndHotbar(playerInventory, currentItemIndex);

        this.slotsChanged(new RecipeWrapper(inventory.getCraftingGridInventory()));
    }

    public void addCraftMatrix()
    {
        for(int i = 0; i < 3; ++i)
        {
            for(int j = 0; j < 3; ++j)
            {
                this.addSlot(new Slot(this.craftMatrix, j + i * 3, 152 + j * 18, 61 + i * 18)
                {
                    @Override
                    public boolean mayPlace(ItemStack stack)
                    {
                        ResourceLocation blacklistedItems = new ResourceLocation(TravelersBackpack.MODID, "blacklisted_items");

                        return !(stack.getItem() instanceof TravelersBackpackItem) &&  !stack.getItem().is(ItemTags.getAllTags().getTag(blacklistedItems));
                    }
                });
            }
        }
    }

    public void addCraftResult()
    {
        this.addSlot(new CraftResultSlotExt(playerInventory.player, this.craftMatrix, this.craftResult, 0, 226, 97));
       /* this.addSlot(new CraftingResultSlot(playerInventory.player, this.craftMatrix, this.craftResult, 0, 226, 97)
        {
            @Override
            public ItemStack onTake(PlayerEntity player, ItemStack stack)
            {
                this.onCrafting(stack);
                net.minecraftforge.common.ForgeHooks.setCraftingPlayer(player);
                NonNullList<ItemStack> nonnulllist = player.world.getRecipeManager().getRecipeNonNull(IRecipeType.CRAFTING, craftMatrix, player.world);

                if (lastRecipe != null && lastRecipe.matches(craftMatrix, player.world)) {
                    nonnulllist = lastRecipe.getRemainingItems(craftMatrix);
                } else {
                    nonnulllist = craftMatrix.getStackList();
                }

                net.minecraftforge.common.ForgeHooks.setCraftingPlayer(null);
                for(int i = 0; i < nonnulllist.size(); ++i) {
                    ItemStack itemstack = craftMatrix.getStackInSlot(i);
                    ItemStack itemstack1 = nonnulllist.get(i);
                    if (!itemstack.isEmpty()) {
                        craftMatrix.decrStackSize(i, 1);
                        itemstack = craftMatrix.getStackInSlot(i);
                    }

                    if (!itemstack1.isEmpty()) {
                        if (itemstack.isEmpty()) {
                            craftMatrix.setInventorySlotContents(i, itemstack1);
                        } else if (ItemStack.areItemsEqual(itemstack, itemstack1) && ItemStack.areItemStackTagsEqual(itemstack, itemstack1)) {
                            itemstack1.grow(itemstack.getCount());
                            craftMatrix.setInventorySlotContents(i, itemstack1);
                        } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
                            player.dropItem(itemstack1, false);
                        }
                    }
                }

                return stack;
            }
        }); */
    }

    public void addBackpackInventory(ITravelersBackpackInventory inventory)
    {
        int slot = 0;

        //24 Slots

        for(int i = 0; i < 3; ++i)
        {
            for(int j = 0; j < 8; ++j)
            {
                this.addSlot(new BackpackSlotItemHandler(inventory.getInventory(), slot++, 62 + j * 18, 7 + i * 18));
            }
        }

        //15 Slots

        for(int i = 0; i < 3; ++i)
        {
            for(int j = 0; j < 5; ++j)
            {
                this.addSlot(new BackpackSlotItemHandler(inventory.getInventory(), slot++, 62 + j * 18, 61 + i * 18));
            }
        }
    }

    public void addPlayerInventoryAndHotbar(PlayerInventory playerInv, int currentItemIndex)
    {
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(playerInv, x + y * 9 + 9, 44 + x*18, 125 + y*18));
            }
        }

        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(playerInv, x, 44 + x*18, 183));
        }
    }

    public void addFluidSlots(ITravelersBackpackInventory inventory)
    {
        //Left In bucket
        this.addSlot(new FluidSlotItemHandler(inventory, Reference.BUCKET_IN_LEFT, 6, 7));

        //Left Out bucket
        this.addSlot(new FluidSlotItemHandler(inventory, Reference.BUCKET_OUT_LEFT, 6, 37));

        //Right In bucket
        this.addSlot(new FluidSlotItemHandler(inventory, Reference.BUCKET_IN_RIGHT, 226, 7));

        //Right Out bucket
        this.addSlot(new FluidSlotItemHandler(inventory, Reference.BUCKET_OUT_RIGHT, 226, 37));
    }

    public void addToolSlots(ITravelersBackpackInventory inventory)
    {
        //Upper Tool Slot
        this.addSlot(new ToolSlotItemHandler(playerInventory.player, inventory, Reference.TOOL_UPPER, 44, 79));

        //Lower Tool slot
        this.addSlot(new ToolSlotItemHandler(playerInventory.player, inventory, Reference.TOOL_LOWER, 44, 97));
    }

    //public void slotChangedCraftingGrid(World world, PlayerEntity player, CraftingInventoryImproved inv, CraftResultInventory result)
    /*{
        if(!world.isRemote && !TravelersBackpackConfig.SERVER.disableCrafting.get())
        {
            ItemStack itemstack = ItemStack.EMPTY;

            IRecipe<CraftingInventory> oldRecipe = (IRecipe<CraftingInventory>) result.getRecipeUsed();
            IRecipe<CraftingInventory> recipe = oldRecipe;
            if(recipe == null || !recipe.matches(inv, world)) recipe = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, inv, world).orElse(null);

            if(recipe != null) itemstack = recipe.getCraftingResult(inv);

            if(player instanceof ServerPlayerEntity)
                if(oldRecipe != recipe)
                {
                   // NetworkUtils.sendTo(FastBench.CHANNEL, new RecipeMessage(recipe, itemstack), player);
                    result.setInventorySlotContents(0, itemstack);
                    ((ServerPlayerEntity)player).connection.sendPacket(new SSetSlotPacket(windowId, 0, itemstack));
                    result.setRecipeUsed(recipe);
                }
                else if (recipe != null && recipe.isDynamic())
                {
                    //NetworkUtils.sendTo(FastBench.CHANNEL, new RecipeMessage(recipe, itemstack), player);
                    result.setInventorySlotContents(0, itemstack);
                    ((ServerPlayerEntity)player).connection.sendPacket(new SSetSlotPacket(windowId, 0, itemstack));
                    result.setRecipeUsed(recipe);
                }
        }
    } */

    @Override
    public void slotsChanged(IInventory inventory)
    {
        if(!TravelersBackpackConfig.SERVER.disableCrafting.get())
        {
            slotChangedCraftingGrid(playerInventory.player.level, playerInventory.player);
         /*   CraftingInventoryImproved craftMatrix = this.craftMatrix;
            CraftResultInventory craftResult = this.craftResult;
            World world = playerInventory.player.world;

            if(!world.isRemote)
            {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) playerInventory.player;
                ItemStack itemstack = ItemStack.EMPTY;

                if(lastRecipe != null && lastRecipe.matches(craftMatrix, world))
                {
                    itemstack = lastRecipe.getCraftingResult(craftMatrix);
                }
                else
                {
                    Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);
                    if(optional.isPresent())
                    {
                        ICraftingRecipe recipe = optional.get();
                        if(craftResult.canUseRecipe(world, serverPlayerEntity, recipe))
                        {
                            lastRecipe = recipe;
                            itemstack = lastRecipe.getCraftingResult(craftMatrix);
                        }
                        else
                        {
                            lastRecipe = null;
                        }
                    }
                }
                TravelersBackpack.NETWORK.send(PacketDistributor.PLAYER.with(() -> serverPlayerEntity), new UpdateRecipePacket(lastRecipe));
              /*  ServerPlayerEntity player = (ServerPlayerEntity) playerInventory.player;
                ItemStack itemstack = ItemStack.EMPTY;
                Optional<ICraftingRecipe> optional = world.getServer().getRecipeManager().getRecipe(IRecipeType.CRAFTING, craftMatrix, world);

                if(optional.isPresent())
                {
                    ICraftingRecipe icraftingrecipe = optional.get();

                    if(craftResult.canUseRecipe(world, player, icraftingrecipe))
                    {
                        itemstack = icraftingrecipe.getCraftingResult(craftMatrix);
                    }
                } */
          //      craftResult.setInventorySlotContents(0, itemstack);
          //      serverPlayerEntity.connection.sendPacket(new SSetSlotPacket(windowId, 0, itemstack));
         //   }
        }
    }
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slotIn)
    {
        return slotIn.container != this.craftResult && super.canTakeItemForPickAll(stack, slotIn);
    }

    public ItemStack handleShiftCraft(PlayerEntity player, Slot resultSlot)
    {
        ItemStack outputCopy = ItemStack.EMPTY;

        if(resultSlot != null && resultSlot.hasItem())
        {
            craftMatrix.checkChanges = false;
            IRecipe<CraftingInventory> recipe = (IRecipe<CraftingInventory>)craftResult.getRecipeUsed();
            while(recipe != null && recipe.matches(craftMatrix, player.level))
            {
                ItemStack recipeOutput = resultSlot.getItem().copy();
                outputCopy = recipeOutput.copy();

                recipeOutput.getItem().onCraftedBy(recipeOutput, player.level, player);

                if(!player.level.isClientSide && !moveItemStackTo(recipeOutput, PLAYER_INV_START, PLAYER_HOT_END + 1, true))
                {
                    craftMatrix.checkChanges = true;
                    return ItemStack.EMPTY;
                }

                resultSlot.onQuickCraft(recipeOutput, outputCopy);
                resultSlot.setChanged();

                if(!player.level.isClientSide && recipeOutput.getCount() == outputCopy.getCount())
                {
                    craftMatrix.checkChanges = true;
                    return ItemStack.EMPTY;
                }

                craftResult.setRecipeUsed(recipe);
                resultSlot.onTake(player, recipeOutput);
            }
            craftMatrix.checkChanges = true;
            slotChangedCraftingGrid(player.level, player);
        }
        craftMatrix.checkChanges = true;
        return craftResult.getRecipeUsed() == null ? ItemStack.EMPTY : outputCopy;
    }

    public void slotChangedCraftingGrid(World world, PlayerEntity player)
    {
        if(!world.isClientSide)
        {
            ItemStack itemstack = ItemStack.EMPTY;

            IRecipe<CraftingInventory> oldRecipe = (IRecipe<CraftingInventory>) craftResult.getRecipeUsed();
            IRecipe<CraftingInventory> recipe = oldRecipe;

            if(recipe == null || !recipe.matches(craftMatrix, world))
            {
                recipe = world.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftMatrix, world).orElse(null);
            }

            if(recipe != null)
            {
                itemstack = recipe.assemble(craftMatrix);
            }

            if(oldRecipe != recipe)
            {
                TravelersBackpack.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new UpdateRecipePacket(recipe, itemstack));
                craftResult.setItem(0, itemstack);
                craftResult.setRecipeUsed(recipe);
            }
            else if(recipe != null && recipe.isSpecial())
            {
                TravelersBackpack.NETWORK.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), new UpdateRecipePacket(recipe, itemstack));
                craftResult.setItem(0, itemstack);
                craftResult.setRecipeUsed(recipe);
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index)
    {
        Slot slot = getSlot(index);
        ItemStack result = ItemStack.EMPTY;

        if(slot != null && slot.hasItem())
        {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if(index >= 0 && index <= BUCKET_RIGHT_OUT)
            {
                if(index == 0)
                {
                    return handleShiftCraft(player, slot);
             /*       while(lastRecipe != null && lastRecipe.matches(craftMatrix, player.world))
                    {
                        ItemStack recipeOutput = slot.getStack().copy();
                        result = recipeOutput.copy();

                        recipeOutput.getItem().onCreated(recipeOutput, player.world, player);

                        if(!mergeItemStack(recipeOutput, PLAYER_INV_START, PLAYER_HOT_END + 1, true))
                        {
                            //this.craftMatrix.markDirty();
                            return ItemStack.EMPTY;
                        }

                        slot.onSlotChange(recipeOutput, result);
                        //slot.onSlotChanged();

                        if(recipeOutput.getCount() == result.getCount())
                        {
                            this.craftMatrix.markDirty();
                            return ItemStack.EMPTY;
                        }

                        craftResult.setRecipeUsed(lastRecipe);
                        slot.onTake(player, recipeOutput);
                    }
                    this.craftMatrix.markDirty();

                 /*   stack.getItem().onCreated(stack, player.world, player);

                    if(!mergeItemStack(stack, PLAYER_INV_START, PLAYER_HOT_END + 1, true))
                    {
                        return ItemStack.EMPTY;
                    }

                    slot.onSlotChange(stack, result);
                    this.craftMatrix.markDirty(); */
                }

                else if(!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_HOT_END + 1, true))
                {
                    return ItemStack.EMPTY;
                }
            }

            if(index >= PLAYER_INV_START)
            {
                if(ToolSlotItemHandler.isValid(stack))
                {
                    if(!moveItemStackTo(stack, TOOL_START, TOOL_END + 1, false))
                    {
                        if(!moveItemStackTo(stack, BACKPACK_INV_START, BACKPACK_INV_END + 1, false))
                        {
                            if(!moveItemStackTo(stack, CRAFTING_GRID_START, CRAFTING_GRID_END + 1, false))
                            {
                                return ItemStack.EMPTY;
                            }
                        }
                    }
                }

                if(!moveItemStackTo(stack, BACKPACK_INV_START, BACKPACK_INV_END + 1, false))
                {
                    if(!moveItemStackTo(stack, CRAFTING_GRID_START, CRAFTING_GRID_END + 1, false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if(stack.isEmpty())
            {
                slot.set(ItemStack.EMPTY);
            }

            else
            {
                slot.setChanged();
            }

            if(stack.getCount() == result.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn)
    {
        return true;
    }

    @Override
    public void removed(PlayerEntity playerIn)
    {
        super.removed(playerIn);

        if(inventory.getScreenID() != Reference.TRAVELERS_BACKPACK_TILE_SCREEN_ID)
        {
            this.inventory.setChanged();
        }

        playSound(playerIn, this.inventory);
        clearBucketSlots(playerIn, this.inventory);
    }

    public static void clearBucketSlots(PlayerEntity playerIn, ITravelersBackpackInventory inventoryIn)
    {
        if((inventoryIn.getScreenID() == Reference.TRAVELERS_BACKPACK_ITEM_SCREEN_ID && playerIn.getMainHandItem().getItem() instanceof TravelersBackpackItem) || (inventoryIn.getScreenID() == Reference.TRAVELERS_BACKPACK_WEARABLE_SCREEN_ID && CapabilityUtils.getWearingBackpack(playerIn).getItem() instanceof TravelersBackpackItem))
        {
            for(int i = Reference.BUCKET_IN_LEFT; i <= Reference.BUCKET_OUT_RIGHT; i++)
            {
                clearBucketSlot(playerIn, inventoryIn, i);
            }
        }
    }

    public static void clearBucketSlot(PlayerEntity playerIn, ITravelersBackpackInventory inventoryIn, int index)
    {
        if(!inventoryIn.getInventory().getStackInSlot(index).isEmpty())
        {
            if(!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity)playerIn).hasDisconnected())
            {
                ItemStack stack = inventoryIn.getInventory().getStackInSlot(index).copy();
                inventoryIn.getInventory().setStackInSlot(index, ItemStack.EMPTY);

                playerIn.drop(stack, false);
            }
            else
            {
                ItemStack stack = inventoryIn.getInventory().getStackInSlot(index);
                inventoryIn.getInventory().setStackInSlot(index, ItemStack.EMPTY);

                playerIn.inventory.placeItemBackInInventory(playerIn.level, stack);
            }
        }
    }

    public void playSound(PlayerEntity playerIn, ITravelersBackpackInventory inventoryIn)
    {
        for(int i = Reference.BUCKET_IN_LEFT; i <= Reference.BUCKET_OUT_RIGHT; i++)
        {
            if(!inventoryIn.getInventory().getStackInSlot(i).isEmpty())
            {
                playerIn.level.playSound(playerIn, playerIn.blockPosition(), SoundEvents.ITEM_PICKUP, SoundCategory.BLOCKS, 1.0F, (1.0F + (playerIn.level.random.nextFloat() - playerIn.level.random.nextFloat()) * 0.2F) * 0.7F);
                break;
            }
        }
    }
}
