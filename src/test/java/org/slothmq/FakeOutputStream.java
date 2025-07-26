package org.slothmq;

import java.io.IOException;
import java.io.OutputStream;

public class FakeOutputStream extends OutputStream {
    StringBuilder internalBuffer = new StringBuilder();
    @Override
    public void write(int b) throws IOException {
        internalBuffer.append((char)b);
    }

    public String getContents() {
        return internalBuffer.toString();
    }
}
