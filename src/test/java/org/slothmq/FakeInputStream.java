package org.slothmq;

import java.io.IOException;
import java.io.InputStream;

public class FakeInputStream extends InputStream {
    private final String contents;
    private int pointer = 0;

    public FakeInputStream(String contents) {
        this.contents = contents;
    }

    @Override
    public int read() throws IOException {
        if (contents == null || contents.isEmpty()) {
            return 0;
        }

        if (pointer >= contents.length()){
            return -1;
        }

        return contents.charAt(pointer++);
    }
}