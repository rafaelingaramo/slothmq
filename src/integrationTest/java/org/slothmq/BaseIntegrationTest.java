package org.slothmq;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public class BaseIntegrationTest {
    @BeforeAll
    public static void beforeAll() throws Exception {
       BaseIntegrationTestSingletonHolder.startServer();
    }

}
