package com.leclowndu93150.armorwardrobe;
import com.leclowndu93150.armorwardrobe.client.ClientEvents;
import com.leclowndu93150.armorwardrobe.common.ModCreativeTabs;
import com.leclowndu93150.armorwardrobe.common.capabilities.WardrobeCapability;
import com.leclowndu93150.armorwardrobe.common.container.WardrobeContainer;
import com.leclowndu93150.armorwardrobe.common.items.WardrobeItem;
import com.leclowndu93150.armorwardrobe.common.networking.PacketHandler;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(ArmorWardrobe.MOD_ID)
public class ArmorWardrobe {
    public static final String MOD_ID = "armorwardrobe";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(Registries.MENU, MOD_ID);

    public static final RegistryObject<Item> WARDROBE_ITEM = ITEMS.register("wardrobe", WardrobeItem::new);

    public static final RegistryObject<MenuType<WardrobeContainer>> WARDROBE_CONTAINER =
            CONTAINERS.register("wardrobe", () ->
                    IForgeMenuType.create((windowId, inv, data) -> new WardrobeContainer(windowId, inv, data))
            );

    public ArmorWardrobe() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        CONTAINERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::setup);
        //modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerCapabilities);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        PacketHandler.init();
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> SlotTypePreset.BACK.getMessageBuilder().build());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(ClientEvents::init);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(WardrobeCapability.class);
    }
}