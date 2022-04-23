package com.example.nettydemo.demo5.protocol.request;

import com.example.nettydemo.demo5.protocol.Packet;
import lombok.Data;
import static com.example.nettydemo.demo5.protocol.command.Command.*;
@Data
public class ListGroupMembersRequestPacket extends Packet {

    private String groupId;

    @Override
    public Byte getCommand() {

        return LIST_GROUP_MEMBERS_REQUEST;
    }
}
