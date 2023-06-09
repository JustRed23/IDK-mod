package dev.JustRed23.idk.items;

import dev.JustRed23.idk.blocks.PaintBucketBlock;
import dev.JustRed23.idk.blocks.PaintedBlock;
import dev.JustRed23.idk.blocks.blockentities.PaintBucketBlockEntity;
import dev.JustRed23.idk.blocks.blockentities.PaintedBlockEntity;
import dev.JustRed23.idk.items.template.CreativeGetterItem;
import dev.JustRed23.idk.items.template.PaintControlsItem;
import dev.JustRed23.idk.utils.ColorUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PaintbrushItem extends PaintControlsItem implements CreativeGetterItem {

    public PaintbrushItem() {
        this(10, 0);
    }

    public PaintbrushItem(int maxUsages, int initialUsages) {
        super(maxUsages, initialUsages);
    }

    public @NotNull ItemStack getCreativeInstance() {
        final ItemStack creativeInstance = new ItemStack(this);
        final CompoundTag tag = new CompoundTag();
        tag.putInt("uses", maxUsages);
        tag.putInt("maxUses", maxUsages);
        tag.putInt("paintColor", 0xFFFFFF);
        creativeInstance.setTag(tag);
        return creativeInstance;
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.idk.paintbrush.tooltip.uses", stack.getOrCreateTag().getInt("uses"), maxUsages).withStyle(ChatFormatting.GRAY));
        if (stack.hasTag() && stack.getTag().contains("paintColor"))
            components.add(Component.translatable("item.idk.paintbrush.tooltip.color", ColorUtils.rgbToHex(stack.getOrCreateTag().getInt("paintColor")))
                    .withStyle(ChatFormatting.GRAY));
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, canUse(stack) ? ClipContext.Fluid.WATER : ClipContext.Fluid.NONE);
        if (hitResult.getType() == BlockHitResult.Type.MISS || hitResult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(stack);

        final BlockPos pos = hitResult.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (level.mayInteract(player, pos)) {
            if (canUse(stack) && state.getFluidState().is(FluidTags.WATER)) {
                depleteAll(stack);
                stack.getOrCreateTag().remove("paintColor");
                return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            } else if (state.getBlock() instanceof PaintBucketBlock) {
                PaintBucketBlockEntity blockEntity = (PaintBucketBlockEntity) level.getBlockEntity(pos);
                if (blockEntity != null) {
                    int usesNeeded = getMaxUses(stack) - getUses(stack);
                    if (usesNeeded > 0 && blockEntity.getColor() == getColor(stack)) {
                        if (blockEntity.getUses() >= usesNeeded) {
                            refillCompletely(stack, blockEntity, usesNeeded);
                            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                        } else if (blockEntity.getUses() > 0) {
                            if (blockEntity.getUses() > usesNeeded) refillPartially(stack, blockEntity, usesNeeded);
                            else refillPartially(stack, blockEntity);

                            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                        } else return InteractionResultHolder.fail(stack);
                    } else if (!canUse(stack)) {
                        if (blockEntity.getUses() >= usesNeeded) {
                            refillCompletely(stack, blockEntity, usesNeeded);
                            setColor(stack, blockEntity.getColor());
                            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                        } else if (blockEntity.getUses() > 0) {
                            if (blockEntity.getUses() > usesNeeded) refillPartially(stack, blockEntity, usesNeeded);
                            else refillPartially(stack, blockEntity);

                            setColor(stack, blockEntity.getColor());
                            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
                        } else return InteractionResultHolder.fail(stack);
                    }
                }
            } else if (state.getBlock() instanceof PaintedBlock) {
                PaintedBlockEntity blockEntity = (PaintedBlockEntity) level.getBlockEntity(pos);
                int color = getColor(stack);
                if (canUse(stack) && blockEntity != null && blockEntity.getColor() != color) {
                    blockEntity.setColor(color);
                    //FIXME: cannot call sound event on server side
                    level.playLocalSound(pos, blockEntity.getPaintSound(), SoundSource.BLOCKS, 0.2F, 1.0F, false);
                    deplete(stack);
                }
            }
        } else return InteractionResultHolder.fail(stack);

        return InteractionResultHolder.pass(stack);
    }

    /*public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide)
            return InteractionResult.SUCCESS;

        BlockState clicked = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack stack = context.getItemInHand();

        if (stack.getItem() instanceof PaintbrushItem) {
            if (clicked.getBlock() instanceof PaintBucketBlock) {
                PaintBucketBlockEntity blockEntity = (PaintBucketBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos());
                if (blockEntity != null) {
                    int usesNeeded = getMaxUses(stack) - getUses(stack);
                    if (usesNeeded > 0 && blockEntity.getColor() == getColor(stack)) {
                        if (blockEntity.getUses() >= usesNeeded) {
                            refillCompletely(stack, blockEntity, usesNeeded);
                            return InteractionResult.SUCCESS;
                        } else if (blockEntity.getUses() > 0) {
                            if (blockEntity.getUses() > usesNeeded) refillPartially(stack, blockEntity, usesNeeded);
                            else refillPartially(stack, blockEntity);

                            return InteractionResult.SUCCESS;
                        } else return InteractionResult.FAIL;
                    } else if (!canUse(stack)) {
                        if (blockEntity.getUses() >= usesNeeded) {
                            refillCompletely(stack, blockEntity, usesNeeded);
                            setColor(stack, blockEntity.getColor());
                            return InteractionResult.SUCCESS;
                        } else if (blockEntity.getUses() > 0) {
                            if (blockEntity.getUses() > usesNeeded) refillPartially(stack, blockEntity, usesNeeded);
                            else refillPartially(stack, blockEntity);

                            setColor(stack, blockEntity.getColor());
                            return InteractionResult.SUCCESS;
                        } else return InteractionResult.FAIL;
                    }
                }
            }

            if (clicked.getBlock() instanceof PaintedBlock) {
                PaintedBlockEntity blockEntity = (PaintedBlockEntity) context.getLevel().getBlockEntity(context.getClickedPos());
                int color = getColor(stack);
                if (canUse(stack) && blockEntity != null && blockEntity.getColor() != color) {
                    blockEntity.setColor(color);
                    //context.getPlayer().playSound(blockEntity.getPaintSound()); //FIXME: Sound doesn't play on server side
                    deplete(stack);
                }
            }
        }

        return InteractionResult.PASS;
    }*/

    private void refillCompletely(ItemStack stack, PaintBucketBlockEntity blockEntity, int usesNeeded) {
        refill(stack);
        blockEntity.deplete(usesNeeded);
    }

    private void refillPartially(ItemStack stack, PaintBucketBlockEntity blockEntity, int usesNeeded) {
        refill(stack, usesNeeded);
        blockEntity.deplete(usesNeeded);
    }

    private void refillPartially(ItemStack stack, PaintBucketBlockEntity blockEntity) {
        refill(stack, blockEntity.getUses());
        blockEntity.setUses(0);
    }

    private void setColor(ItemStack stack, int color) {
        stack.getOrCreateTag().putInt("paintColor", color);
    }

    private int getColor(ItemStack stack) {
        return stack.getOrCreateTag().getInt("paintColor");
    }
}
