package com.example.nettydemo.demo5.attribute;

import com.example.nettydemo.demo5.session.Session;
import io.netty.util.AttributeKey;

public interface Attributes {
    AttributeKey<Session> SESSION = AttributeKey.newInstance("session");
}
