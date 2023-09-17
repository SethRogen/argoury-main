package com.ziotic.link.network;

import com.ziotic.Static;
import com.ziotic.content.cc.Clan;
import com.ziotic.content.cc.ClanManager;
import com.ziotic.link.LinkServer;
import com.ziotic.link.WorldServerSession;
import com.ziotic.logic.player.PlayerType;
import com.ziotic.logic.player.RemotePlayer;
import com.ziotic.network.Frame;
import com.ziotic.network.FrameBuilder;
import com.ziotic.network.StandardFrameEncoder;
import com.ziotic.utility.Logging;
import com.ziotic.utility.Streams;
import org.apache.log4j.Logger;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

/**
 * @author Lazaro
 */
public class LinkCodec extends CumulativeProtocolDecoder implements ProtocolCodecFactory {
    public static final int[] FRAME_LENGTHS = new int[256];

    static {
        FRAME_LENGTHS[1] = -1; // Register player
        FRAME_LENGTHS[2] = -1; // Un-register player
        FRAME_LENGTHS[3] = -1; // Load & verify player
        FRAME_LENGTHS[4] = -2; // Save player
        FRAME_LENGTHS[5] = -1; // Send PM
        FRAME_LENGTHS[6] = -1; // Load clan
        FRAME_LENGTHS[7] = -1; // Join clan
        FRAME_LENGTHS[8] = -1; // Leave clan
        FRAME_LENGTHS[9] = -1; // Save clan
        FRAME_LENGTHS[10] = -1; // Send clan message
        FRAME_LENGTHS[11] = -1; // Kick player from clan
        FRAME_LENGTHS[12] = -2; // Synchronize player list
        FRAME_LENGTHS[20] = -1; // Player muting
    }

    public static final ProtocolCodecFilter FILTER = new ProtocolCodecFilter(new LinkCodec());

    private static Logger logger = Logging.log();

    @Override
    protected boolean doDecode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        WorldServerSession world = (WorldServerSession) session.getAttribute("world");
        if (world == null) {
            // Check if there is enough data
            if (buffer.remaining() < 2) {
                return false;
            }
            int loginSize = buffer.getShort() & 0xffff;
            if (buffer.remaining() < loginSize) {
                buffer.rewind();
                return false;
            }
            // Register the session with a world.
            String password = Streams.readString(buffer);
            if (!password.equals(Static.conf.getString("link_pass"))) {
                logger.warn("Connection from " + session.getRemoteAddress().toString() + " attempted to connect with an invalid password!");
                session.write(new FrameBuilder(1).writeByte(LinkServer.INVALID_PASSWORD_RESP).toFrame()).addListener(IoFutureListener.CLOSE);
                return false;
            }
            int worldId = buffer.getShort();
            int serverType = buffer.get() & 0xff;
            String address = Streams.readString(buffer);
            if (serverType == 1) {
                world = Static.currentLink().getLobby(worldId);
            } else {
                world = Static.currentLink().getGame(worldId);
            }
            boolean newWorld = world == null;
            if (newWorld) {
                world = new WorldServerSession(worldId, serverType, address);
                Static.currentLink().registerWorld(world);
            }
            world.registerSession(session);
            session.setAttribute("world", world);
            session.write(new FrameBuilder(1).writeByte(LinkServer.OK_RESP).toFrame());
            if (newWorld) {
                for (RemotePlayer player : Static.currentLink().getPlayers().values()) {
                    session.write(Static.currentLink().generateRegisterPlayerFrame(player));
                }
                for (Clan clan : ClanManager.getClans().values()) {
                    session.write(Static.currentLink().generateRegisterClanFrame(clan));
                    for (PlayerType player : clan.getPlayers().values()) {
                        session.write(Static.currentLink().generateRegisterPlayerToClanFrame(player, clan));
                    }
                }
            }
            return false;
        } else {
            if (buffer.hasRemaining()) {
                int opcode = buffer.get() & 0xff;
                int length = FRAME_LENGTHS[opcode];
                switch (length) {
                    case -1:
                        if (buffer.hasRemaining()) {
                            length = buffer.get() & 0xff;
                        } else {
                            buffer.rewind();
                            return false;
                        }
                        break;
                    case -2:
                        if (buffer.remaining() >= 2) {
                            length = buffer.getShort() & 0xffff;
                        } else {
                            buffer.rewind();
                            return false;
                        }
                        break;
                }
                if (buffer.remaining() >= length) {
                    byte[] frameBuffer = new byte[length];
                    buffer.get(frameBuffer);
                    output.write(new Frame(opcode, Frame.FrameType.RECIEVED, frameBuffer, length));
                    return true;
                } else {
                    buffer.rewind();
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return StandardFrameEncoder.INSTANCE;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return this;
    }
}
