package tw.dfder.ccts_poc_payment.provider;


import au.com.dius.pact.core.model.Interaction;
import au.com.dius.pact.core.model.Pact;
import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import tw.dfder.ccts_poc_payment.Entity.PaymentMessageEnvelope;

import java.util.HashMap;
import java.util.UUID;

@Provider("paymentService")
@Consumer("orchestrator")
@PactBroker(url = "http://23.dfder.tw:10141")
public class ToOrchestratorTest {

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void testTemplate(Pact pact, Interaction interaction, PactVerificationContext context) {
        context.verifyInteraction();
    }


    @BeforeEach
    void before(PactVerificationContext context) {
        context.setTarget(new MessageTestTarget());
        System.setProperty("pact.verifier.publishResults", "true");
        System.setProperty("pact.provider.version", "v0.1");
    }


    @PactVerifyProvider("request payment")
    public MessageAndMetadata verifyMessageOfPayment() {

        Gson gson = new Gson();
        PaymentMessageEnvelope msg = new PaymentMessageEnvelope();
        msg.setPaymentId(UUID.randomUUID().toString());
        msg.setBuyerId(UUID.randomUUID().toString());
        msg.setTotalAmount((int) (Math.random()*1000));
        msg.setValid(true);
        msg.setMethod("response");

        HashMap<String, String> props = new HashMap<>();
        props.put("source", "orchestrator");
        props.put("destination","pointService");
        return new MessageAndMetadata(gson.toJson(msg).getBytes(), props);
    }

}
