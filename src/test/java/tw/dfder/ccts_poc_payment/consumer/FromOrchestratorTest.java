package tw.dfder.ccts_poc_payment.consumer;


import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "orchestrator", providerType = ProviderType.ASYNCH)
public class FromOrchestratorTest {

    @Pact(consumer = "paymentService")
    public MessagePact validateMessageFromOrchestrator(MessagePactBuilder builder) {
        return builder
                .expectsToReceive("t-orc-payment-01")   
                .withMetadata(m -> {
                    m.add("provider", "orchestrator");
                    m.add("consumer", "paymentService");
                })
                .toPact();

    }

    @Test
    @PactTestFor(pactMethod = "validateMessageFromOrchestrator")
    public void validateMessageFromOrchestratorTest(List<Message> messages) {

        // 起碼有上面的案例吧
        assertThat(messages).isNotEmpty();
        // 驗header
        messages.forEach(m -> {
            assertThat(m.getMetadata()).hasFieldOrProperty("provider");
            assertThat(m.getMetadata()).hasFieldOrProperty("consumer");
        });

    }
}