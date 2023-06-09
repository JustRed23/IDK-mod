package dev.JustRed23.paint;

import dev.JustRed23.paint.items.template.CreativeGetterItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = Paint.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModTabs {

    public static CreativeModeTab MAIN;

    @SubscribeEvent
    static void registerTabs(CreativeModeTabEvent.Register event) {
        MAIN = event.registerCreativeModeTab(new ResourceLocation(Paint.MOD_ID, "main"), builder ->
                builder.title(Component.translatable("tabs.main.title"))
                        .icon(ModItems.PAINTBRUSH.get()::getDefaultInstance)
        );
    }

    @SubscribeEvent
    static void fillTabs(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == MAIN) {
            ModItems.getAll()
                    .stream()
                    .map(RegistryObject::get)
                    .filter(item -> item instanceof CreativeGetterItem)
                    .map(item -> (CreativeGetterItem) item)
                    .forEach(item -> event.accept(item.getCreativeInstance()));
        }
    }
}
