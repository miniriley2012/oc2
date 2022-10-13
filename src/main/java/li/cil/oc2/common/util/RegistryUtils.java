/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.util;

import li.cil.oc2.api.API;
import li.cil.oc2.api.util.IRegistryItem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class RegistryUtils {
    private enum Phase {
        PRE_INIT,
        INIT,
        POST_INIT,
    }

    private static final List<DeferredRegister<?>> ENTRIES = new ArrayList<>();
    private static Phase phase = Phase.PRE_INIT;

    public static <T> DeferredRegister<T> getInitializerFor(final ResourceKey<Registry<T>> key) {
        if (phase != Phase.INIT) throw new IllegalStateException();

        final DeferredRegister<T> entry = DeferredRegister.create(key, API.MOD_ID);
        ENTRIES.add(entry);
        return entry;
    }

    public static <T> DeferredRegister<T> getInitializerFor(final IForgeRegistry<T> registry) {
        if (phase != Phase.INIT) throw new IllegalStateException();

        final DeferredRegister<T> entry = DeferredRegister.create(registry, API.MOD_ID);
        ENTRIES.add(entry);
        return entry;
    }

    public static void begin() {
        if (phase != Phase.PRE_INIT) throw new IllegalStateException();
        phase = Phase.INIT;
    }

    public static void finish() {
        if (phase != Phase.INIT) throw new IllegalStateException();
        phase = Phase.POST_INIT;

        for (final DeferredRegister<?> register : ENTRIES) {
            register.register(FMLJavaModLoadingContext.get().getModEventBus());
        }

        ENTRIES.clear();
    }

    public static <T extends IRegistryItem> String key(final T item) {
        return item.getRegistryName().toString();
    }

    public static <T extends IRegistryItem> Optional<String> optionalKey(@Nullable final T item) {
        if (item == null) {
            return Optional.empty();
        }

        return Optional.of(item.getRegistryName().toString());
    }

    private RegistryUtils() {
    }
}
