/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network.message;

import li.cil.oc2.common.blockentity.ComputerBlockEntity;
import li.cil.oc2.common.network.MessageUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ComputerBootErrorMessage extends AbstractMessage {
    private BlockPos pos;
    private Component value;

    ///////////////////////////////////////////////////////////////////

    public ComputerBootErrorMessage(final ComputerBlockEntity computer, @Nullable final Component value) {
        this.pos = computer.getBlockPos();
        this.value = value;
    }

    public ComputerBootErrorMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        value = buffer.readOptional(FriendlyByteBuf::readComponent).orElse(null);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeOptional(Optional.ofNullable(value), FriendlyByteBuf::writeComponent);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        MessageUtils.withClientBlockEntityAt(pos, ComputerBlockEntity.class,
            computer -> computer.getVirtualMachine().setBootErrorClient(value));
    }
}
