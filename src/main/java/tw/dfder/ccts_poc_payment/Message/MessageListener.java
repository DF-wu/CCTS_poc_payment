package tw.dfder.ccts_poc_payment.Message;


import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;

import org.springframework.stereotype.Service;
import tw.dfder.ccts_poc_payment.Entity.PaymentMessageEnvelope;
import tw.dfder.ccts_poc_payment.configuration.RabbitmqConfig;
import tw.dfder.ccts_poc_payment.configuration.ServiceConfig;
import tw.dfder.ccts_poc_payment.repository.PaymentRepo;

import java.io.IOException;

@EnableRabbit
@Service("MessageListener")
public class MessageListener {
    private final Gson gson;
    private final CCTSMessageSender sender;
    private final PaymentRepo repo;
    private final ServiceConfig serviceConfig;

    @Autowired
    public MessageListener(Gson gson, CCTSMessageSender sender, PaymentRepo repo, ServiceConfig serviceConfig) {
        this.gson = gson;
        this.sender = sender;
        this.repo = repo;
        this.serviceConfig = serviceConfig;
    }


    @RabbitListener(queues = {
            RabbitmqConfig.QUEUE_PAYMENT_REQUEST
    })
    public void receivedRequest(String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel ch) throws IOException {
//        decode the message
        PaymentMessageEnvelope receivedMessage = gson.fromJson(msg, PaymentMessageEnvelope.class);
        System.out.println("get a msg!!" + receivedMessage);
        ch.basicAck(deliveryTag, false);

        if (receivedMessage.getMethod().equals("pay")){
            paymentProcess(receivedMessage);
        }else if(receivedMessage.getMethod().equals("get")){
            //
//            queryProcess(receivedMessage);
        }else if(receivedMessage.getMethod().equals("rollback")){
            rollbackPayment(receivedMessage);
        }
        else{
            System.out.println(receivedMessage);
        }


    }

    // rollback
    private void rollbackPayment(PaymentMessageEnvelope receivedMessage) {
        repo.deleteByPaymentId(receivedMessage.getPaymentId());
        System.out.println("deleted. pid: " + receivedMessage.getPaymentId());
        receivedMessage.setMethod("rollback res");
        sender.sendRequestMessage(
                gson.toJson(receivedMessage),
                "orchestrator",
                RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                "t-payment-orc-03",
                "13"
        );
    }


    public void paymentProcess(PaymentMessageEnvelope receivedMessage){

        if (receivedMessage.getPaymentId() != null && receivedMessage.getBuyerId() != null && receivedMessage.isValid()) {
            // return a valid result
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(receivedMessage.isValid());
            paymentMessageEnvelope.setTotalAmount(receivedMessage.getTotalAmount());
            paymentMessageEnvelope.setMethod("pay");
            repo.save(paymentMessageEnvelope);
            // send msg
            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    "orchestrator",
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    "t-payment-orc-01",
                    "2"
            );
        }else {
            // invalid
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(false);
            paymentMessageEnvelope.setMethod("rollback");
            paymentMessageEnvelope.setTotalAmount(receivedMessage.getTotalAmount());
            repo.save(paymentMessageEnvelope);

            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    "orchestrator",
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    "t-payment-orc-02",
                    "7"
            );
        }
    }



    // find by paymentid

    // dont use now
    public void queryProcess(PaymentMessageEnvelope receivedMessage){


        PaymentMessageEnvelope result = repo.findByPaymentId(receivedMessage.getPaymentId());
        System.out.println("!!!!!" + result.toString());
        sender.sendRequestMessage(
                gson.toJson(result),
                "orchestrator",
                RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                "queryProcess",
                "nonono"
        );


    }



}
