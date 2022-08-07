package com.fkp.netty.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class MyFrameDecoder extends LengthFieldBasedFrameDecoder {

    public MyFrameDecoder(){
        this(1024,12,4,0,0);
    }

    public MyFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
