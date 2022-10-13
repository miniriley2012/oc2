/* SPDX-License-Identifier: MIT */

package li.cil.oc2.api.bus.device;

import net.minecraftforge.registries.ObjectHolder;

/**
 * Lists built-in device types for convenience.
 */
public final class DeviceTypes {
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "memory") public static DeviceType MEMORY = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "hard_drive") public static DeviceType HARD_DRIVE = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "flash_memory") public static DeviceType FLASH_MEMORY = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "card") public static DeviceType CARD = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "robot_module") public static DeviceType ROBOT_MODULE = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "floppy") public static DeviceType FLOPPY = null;
    @ObjectHolder(registryName = DeviceType.REGISTRY_NAME, value = "network_tunnel") public static DeviceType NETWORK_TUNNEL = null;
}
