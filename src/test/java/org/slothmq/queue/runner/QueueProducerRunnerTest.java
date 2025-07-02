package org.slothmq.queue.runner;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slothmq.protocol.ProtocolTransferObject;
import org.slothmq.queue.QueueHandler;

public class QueueProducerRunnerTest {
    @Test
    public void bla() {
        //given
        var address = "any";
        var helloWorld = "hello world";
        var letter = new ProtocolTransferObject();
        letter.setAddress(address);
        letter.setContents(helloWorld);
        var queueHandler = Mockito.mock(QueueHandler.class);
        Mockito.doNothing().when(queueHandler).pushToQueue(Mockito.anyString(), Mockito.any());
        var queueProducerRunner = new QueueProducerRunner(letter, queueHandler);

        //when
        queueProducerRunner.run();

        //then
        Mockito.verify(queueHandler).pushToQueue(
                Mockito.argThat((addressParameter) -> addressParameter.equals(address)),
                Mockito.argThat(content -> content.equals(helloWorld)));
    }
}
