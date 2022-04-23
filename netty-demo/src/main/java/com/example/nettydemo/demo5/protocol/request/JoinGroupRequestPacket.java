package com.example.nettydemo.demo5.protocol.request;

import com.example.nettydemo.demo5.protocol.Packet;
import lombok.Data;
import static com.example.nettydemo.demo5.protocol.command.Command.*;
@Data
public class JoinGroupRequestPacket extends Packet {

    private String groupId;

    @Override
    public Byte getCommand() {

        return JOIN_GROUP_REQUEST;
    }
}
