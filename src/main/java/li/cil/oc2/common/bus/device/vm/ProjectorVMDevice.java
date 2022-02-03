package li.cil.oc2.common.bus.device.vm;

import li.cil.oc2.api.bus.device.vm.VMDevice;
import li.cil.oc2.api.bus.device.vm.VMDeviceLoadResult;
import li.cil.oc2.api.bus.device.vm.context.VMContext;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.blockentity.ProjectorBlockEntity;
import li.cil.oc2.common.bus.device.util.IdentityProxy;
import li.cil.oc2.common.bus.device.util.OptionalAddress;
import li.cil.oc2.common.serialization.BlobStorage;
import li.cil.oc2.common.util.NBTTagIds;
import li.cil.oc2.common.vm.device.SimpleFramebufferDevice;
import li.cil.oc2.jcodec.common.model.Picture;
import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.UUID;

public final class ProjectorVMDevice extends IdentityProxy<ProjectorBlockEntity> implements VMDevice {
    private static final String ADDRESS_TAG_NAME = "address";
    private static final String BLOB_HANDLE_TAG_NAME = "blob";

    public static final int WIDTH = 640;
    public static final int HEIGHT = 480;

    ///////////////////////////////////////////////////////////////

    @Nullable private SimpleFramebufferDevice device;
    private final Object deviceLock = new Object();

    ///////////////////////////////////////////////////////////////

    private final OptionalAddress address = new OptionalAddress();
    @Nullable private UUID blobHandle;

    ///////////////////////////////////////////////////////////////

    public ProjectorVMDevice(final ProjectorBlockEntity identity) {
        super(identity);
    }

    ///////////////////////////////////////////////////////////////

    public boolean hasChanges() {
        synchronized (deviceLock) {
            return device != null && device.hasChanges();
        }
    }

    public boolean applyChanges(final Picture picture) {
        synchronized (deviceLock) {
            return device != null && device.applyChanges(picture);
        }
    }

    @Override
    public VMDeviceLoadResult mount(final VMContext context) {
        if (!allocateDevice(context)) {
            return VMDeviceLoadResult.fail();
        }

        assert device != null;
        if (!address.claim(context, device)) {
            return VMDeviceLoadResult.fail();
        }

        identity.setProjecting(true);

        return VMDeviceLoadResult.success();
    }

    @Override
    public void unmount() {
        synchronized (deviceLock) {
            if (device != null) {
                device.close();
                device = null;
            }
        }

        if (blobHandle != null) {
            BlobStorage.close(blobHandle);
        }

        identity.setProjecting(false);
    }

    @Override
    public void dispose() {
        if (blobHandle != null) {
            BlobStorage.delete(blobHandle);
            blobHandle = null;
        }

        address.clear();
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();

        if (blobHandle != null) {
            tag.putUUID(BLOB_HANDLE_TAG_NAME, blobHandle);
        }
        if (address.isPresent()) {
            tag.putLong(ADDRESS_TAG_NAME, address.getAsLong());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag tag) {
        if (tag.hasUUID(BLOB_HANDLE_TAG_NAME)) {
            blobHandle = tag.getUUID(BLOB_HANDLE_TAG_NAME);
        }
        if (tag.contains(ADDRESS_TAG_NAME, NBTTagIds.TAG_LONG)) {
            address.set(tag.getLong(ADDRESS_TAG_NAME));
        }
    }

    ///////////////////////////////////////////////////////////////

    private boolean allocateDevice(final VMContext context) {
        if (!context.getMemoryAllocator().claimMemory(Constants.PAGE_SIZE)) {
            return false;
        }

        try {
            device = createFrameBufferDevice();
        } catch (final IOException e) {
            return false;
        }

        return true;
    }

    private SimpleFramebufferDevice createFrameBufferDevice() throws IOException {
        blobHandle = BlobStorage.validateHandle(blobHandle);
        final FileChannel channel = BlobStorage.getOrOpen(blobHandle);
        final MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, WIDTH * HEIGHT * SimpleFramebufferDevice.STRIDE);
        return new SimpleFramebufferDevice(WIDTH, HEIGHT, buffer);
    }
}
