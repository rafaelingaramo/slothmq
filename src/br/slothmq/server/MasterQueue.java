package br.slothmq.server;

import br.slothmq.exception.NonexistentQueueException;

import java.util.*;

public class MasterQueue {
    private static final Map<String, Queue<Object>> MAP_QUEUE_STRUCT = new HashMap<>();
    private static final MasterQueue INSTANCE = new MasterQueue();

    public MasterQueue() {
        //it needs to be able to recover from database
    }

    public static MasterQueue getInstance() {
        return INSTANCE;
    }

    //TODO it needs to be synchronized
    public void pushToQueue(String queueName, Object content) {
        Queue<Object> queue;
        if (!MAP_QUEUE_STRUCT.containsKey(queueName)) {
            queue = new LinkedList<>();
        } else {
            queue = MAP_QUEUE_STRUCT.get(queueName);
        }

        queue.add(content);
        MAP_QUEUE_STRUCT.put(queueName, queue);
    }

    //TODO it needs to be synchronized
    public Object consumeFromQueue(String queueName) {
        if (!MAP_QUEUE_STRUCT.containsKey(queueName)) {
            throw new NonexistentQueueException(queueName);
        }
        return MAP_QUEUE_STRUCT.get(queueName).poll();
    }
}
