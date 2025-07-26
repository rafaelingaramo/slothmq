package org.slothmq.queue.runner;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;

import static org.mockito.Mockito.*;

public class QueueProducerRunnerTest {
    @Test
    public void bla() {
        //given
        var address = "any";
        var helloWorld = "hello world";
        var letter = new ProtocolTransferObject();
        letter.setAddress(address);
        letter.setContents(helloWorld);
        var queueHandler = mock(QueueHandler.class);
        doNothing().when(queueHandler).pushToQueue(anyString(), any());
        var queueProducerRunner = new QueueProducerRunner(letter, queueHandler);

        //when
        queueProducerRunner.run();

        //then
        verify(queueHandler).pushToQueue(
                argThat((addressParameter) -> addressParameter.equals(address)),
                argThat(content -> content.equals(helloWorld)));
    }
}
