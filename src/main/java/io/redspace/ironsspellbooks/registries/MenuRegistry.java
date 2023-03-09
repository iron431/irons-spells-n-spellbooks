package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.gui.inscription_table.InscriptionTableMenu;
import io.redspace.ironsspellbooks.gui.scroll_forge.ScrollForgeMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class MenuRegistry {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IronsSpellbooks.MODID);

    public static void register(IEventBus eventBus){
        MENUS.register(eventBus);
    }
    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> factory, String name) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }

    public static final RegistryObject<MenuType<InscriptionTableMenu>> INSCRIPTION_TABLE_MENU = registerMenuType(InscriptionTableMenu::new,"inscription_table_menu");
    public static final RegistryObject<MenuType<ScrollForgeMenu>> SCROLL_FORGE_MENU = registerMenuType(ScrollForgeMenu::new,"scroll_forge_menu");
    public static final RegistryObject<MenuType<ArcaneAnvilMenu>> ARCANE_ANVIL_MENU = registerMenuType(ArcaneAnvilMenu::new,"arcane_anvil_menu");

}
