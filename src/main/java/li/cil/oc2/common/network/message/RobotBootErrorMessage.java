/* SPDX-License-Identifier: MIT */

package li.cil.oc2.common.network.message;

import li.cil.oc2.common.entity.Robot;
import li.cil.oc2.common.network.MessageUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.Optional;

public final class RobotBootErrorMessage extends AbstractMessage {
    private int entityId;
    private Component value;

    ///////////////////////////////////////////////////////////////////

    public RobotBootErrorMessage(final Robot robot, @Nullable final Component value) {
        this.entityId = robot.getId();
        this.value = value;
    }

    public RobotBootErrorMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        entityId = buffer.readVarInt();
        value = buffer.readOptional(FriendlyByteBuf::readComponent).orElse(null);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeOptional(Optional.ofNullable(value), FriendlyByteBuf::writeComponent);
        buffer.writeComponent(value);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void handleMessage(final NetworkEvent.Context context) {
        MessageUtils.withClientEntity(entityId, Robot.class,
            robot -> robot.getVirtualMachine().setBootErrorClient(value));
    }
}
