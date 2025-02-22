package com.tiviacz.travelersbackpack.common;

import com.tiviacz.travelersbackpack.capability.CapabilityUtils;
import com.tiviacz.travelersbackpack.capability.ITravelersBackpack;
import com.tiviacz.travelersbackpack.config.TravelersBackpackConfig;
import com.tiviacz.travelersbackpack.fluids.EffectFluidRegistry;
import com.tiviacz.travelersbackpack.inventory.TravelersBackpackInventory;
import com.tiviacz.travelersbackpack.inventory.container.TravelersBackpackItemContainer;
import com.tiviacz.travelersbackpack.items.HoseItem;
import com.tiviacz.travelersbackpack.tileentity.TravelersBackpackTileEntity;
import com.tiviacz.travelersbackpack.util.FluidUtils;
import com.tiviacz.travelersbackpack.util.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;

public class ServerActions
{
    public static void cycleTool(PlayerEntity player, double scrollDelta)
    {
        if(CapabilityUtils.isWearingBackpack(player))
        {
            TravelersBackpackInventory inventory = CapabilityUtils.getBackpackInv(player);
            ItemStackHandler inv = inventory.getInventory();
            ItemStack heldItem = player.getMainHandItem();

            if(!inv.getStackInSlot(Reference.TOOL_UPPER).isEmpty() && inv.getStackInSlot(Reference.TOOL_LOWER).isEmpty() || !inv.getStackInSlot(Reference.TOOL_LOWER).isEmpty() && inv.getStackInSlot(Reference.TOOL_UPPER).isEmpty())
            {
                boolean isUpperEmpty = inv.getStackInSlot(Reference.TOOL_UPPER).isEmpty();
                player.setItemInHand(Hand.MAIN_HAND, isUpperEmpty ? inv.getStackInSlot(Reference.TOOL_LOWER) : inv.getStackInSlot(Reference.TOOL_UPPER));
                inv.setStackInSlot(isUpperEmpty ? Reference.TOOL_LOWER : Reference.TOOL_UPPER, heldItem);
            }

            if(!inv.getStackInSlot(Reference.TOOL_UPPER).isEmpty() && !inv.getStackInSlot(Reference.TOOL_LOWER).isEmpty())
            {
                if(scrollDelta < 0)
                {
                    player.setItemInHand(Hand.MAIN_HAND, inv.getStackInSlot(Reference.TOOL_UPPER));
                    inv.setStackInSlot(Reference.TOOL_UPPER, inv.getStackInSlot(Reference.TOOL_LOWER));
                    inv.setStackInSlot(Reference.TOOL_LOWER, heldItem);
                }

                else if(scrollDelta > 0)
                {
                    player.setItemInHand(Hand.MAIN_HAND, inv.getStackInSlot(Reference.TOOL_LOWER));
                    inv.setStackInSlot(Reference.TOOL_LOWER, inv.getStackInSlot(Reference.TOOL_UPPER));
                    inv.setStackInSlot(Reference.TOOL_UPPER, heldItem);
                }
            }
            inventory.setChanged();
        }
    }

    public static void equipBackpack(PlayerEntity player)
    {
        LazyOptional<ITravelersBackpack> cap = CapabilityUtils.getCapability(player);
        World world = player.level;

        if(!world.isClientSide)
        {
            if(!cap.map(ITravelersBackpack::hasWearable).orElse(false))
            {
                if(player.containerMenu instanceof TravelersBackpackItemContainer) player.containerMenu.removed(player);

                ItemStack stack = player.getMainHandItem().copy();

                cap.ifPresent(inv -> inv.setWearable(stack));
                player.getMainHandItem().shrink(1);
                world.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.0F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

                //Sync
                CapabilityUtils.synchronise(player);
                CapabilityUtils.synchroniseToOthers(player);
            }
            player.closeContainer();
        }
    }

    public static void unequipBackpack(PlayerEntity player)
    {
        LazyOptional<ITravelersBackpack> cap = CapabilityUtils.getCapability(player);
        World world = player.level;

      //  CapabilityUtils.onUnequipped(world, player, cap.getWearable());

        if(!world.isClientSide)
        {
            if(player.containerMenu instanceof TravelersBackpackItemContainer) player.containerMenu.removed(player);

            ItemStack wearable = cap.map(ITravelersBackpack::getWearable).orElse(ItemStack.EMPTY).copy();

            if(!player.inventory.add(wearable))
            {
                player.sendMessage(new TranslationTextComponent(Reference.NO_SPACE), player.getUUID());
                player.closeContainer();

                return;
            }

            if(cap.map(ITravelersBackpack::hasWearable).orElse(false))
            {
                cap.ifPresent(ITravelersBackpack::removeWearable);
                world.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundCategory.PLAYERS, 1.05F, (1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F) * 0.7F);

                //Sync
                CapabilityUtils.synchronise(player);
                CapabilityUtils.synchroniseToOthers(player);
            }
            player.closeContainer();
        }
    }

    public static void toggleSleepingBag(PlayerEntity player, BlockPos pos)
    {
        World world = player.level;

        if(world.getBlockEntity(pos) instanceof TravelersBackpackTileEntity)
        {
            TravelersBackpackTileEntity te = (TravelersBackpackTileEntity)world.getBlockEntity(pos);

            if(!te.isSleepingBagDeployed())
            {
                if(te.deploySleepingBag(world, pos))
                {
                    player.closeContainer();
                }
                else
                {
                    player.sendMessage(new TranslationTextComponent(Reference.DEPLOY), player.getUUID());
                }
            }
            else
            {
                te.removeSleepingBag(world);
            }
            player.closeContainer();
        }
    }

    public static void emptyTank(double tankType, PlayerEntity player, World world)
    {
        TravelersBackpackInventory inv = CapabilityUtils.getBackpackInv(player);
        FluidTank tank = tankType == 1D ? inv.getLeftTank() : inv.getRightTank();
        world.playSound(null, player.blockPosition(), FluidUtils.getFluidEmptySound(tank.getFluid().getFluid()), SoundCategory.BLOCKS, 1.0F, 1.0F);
        tank.drain(TravelersBackpackConfig.SERVER.tanksCapacity.get(), IFluidHandler.FluidAction.EXECUTE);
        player.closeContainer();

        //Sync
        CapabilityUtils.synchronise(player);
        CapabilityUtils.synchroniseToOthers(player);
        inv.markTankDirty();
    }

    public static boolean setFluidEffect(World world, PlayerEntity player, FluidTank tank)
    {
        FluidStack fluidStack = tank.getFluid();
        boolean done = false;

        if(EffectFluidRegistry.hasFluidEffectAndCanExecute(fluidStack, world, player))
        {
            done = EffectFluidRegistry.executeFluidEffectsForFluid(fluidStack, player, world);
        }
        return done;
    }

    public static void switchHoseMode(PlayerEntity player, double scrollDelta)
    {
        ItemStack hose = player.getMainHandItem();

        if(hose.getItem() instanceof HoseItem)
        {
            if(hose.getTag() != null)
            {
                int mode = HoseItem.getHoseMode(hose);

                if(scrollDelta > 0)
                {
                    mode = mode + 1;

                    if(mode == 4)
                    {
                        mode = 1;
                    }
                }

                else if(scrollDelta < 0)
                {
                    mode = mode - 1;

                    if(mode == 0)
                    {
                        mode = 3;
                    }
                }
                hose.getTag().putInt("Mode", mode);
            }
        }
    }

    public static void toggleHoseTank(PlayerEntity player)
    {
        ItemStack hose = player.getMainHandItem();

        if(hose.getItem() instanceof HoseItem)
        {
            if(hose.getTag() != null)
            {
                int tank = HoseItem.getHoseTank(hose);

                if(tank == 1)
                {
                    tank = 2;
                }
                else
                {
                    tank = 1;
                }

                hose.getTag().putInt("Tank", tank);
            }
        }
    }
}
