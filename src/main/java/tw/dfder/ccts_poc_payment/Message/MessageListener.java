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


    @Autowired
    public MessageListener(Gson gson, CCTSMessageSender sender, PaymentRepo repo) {
        this.gson = gson;
        this.sender = sender;
        this.repo = repo;
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
            queryProcess(receivedMessage);
        }else {
            System.out.println(receivedMessage);
        }


    }


    public void paymentProcess(PaymentMessageEnvelope receivedMessage){

        if (receivedMessage.getPaymentId() != null && receivedMessage.getBuyerId() != null && receivedMessage.isValid()) {
            // return a valid result
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(receivedMessage.isValid());
            paymentMessageEnvelope.setTotalAmount(receivedMessage.getTotalAmount());
            repo.save(paymentMessageEnvelope);
            // send msg
            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    "orchestrator",
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }else {

            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(false);
            paymentMessageEnvelope.setTotalAmount(receivedMessage.getTotalAmount());
            repo.save(paymentMessageEnvelope);

            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    "orchestrator",
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }
    }



    // find by paymentid
    public void queryProcess(PaymentMessageEnvelope receivedMessage){

        PaymentMessageEnvelope result = repo.findByPaymentId(receivedMessage.getPaymentId());
        System.out.println("!!!!!" + result.toString());
        sender.sendRequestMessage(
                gson.toJson(result),
                "orchestrator",
                RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                ServiceConfig.serviceName
        );


    }



}
