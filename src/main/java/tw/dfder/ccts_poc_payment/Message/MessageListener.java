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

@EnableRabbit
@Service("MessageListener")
public class MessageListener {
    private final Gson gson;
    private final CCTSMessageSender sender;

    @Autowired
    public MessageListener(Gson gson, CCTSMessageSender sender) {
        this.gson = gson;
        this.sender = sender;
    }


    @RabbitListener(queues = {
            RabbitmqConfig.QUEUE_PAYMENT_REQUEST
    })
    public void receivedMessageFromPayment(String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel ch){
//        decode the message
        PaymentMessageEnvelope receivedMessage = gson.fromJson(msg, PaymentMessageEnvelope.class);


        if (receivedMessage.getPaymentID() != null && receivedMessage.getBuyerID() != null && receivedMessage.isValid()) {
            // return a valid result
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentID(receivedMessage.getPaymentID());
            paymentMessageEnvelope.setBuyerID(receivedMessage.getBuyerID());
            paymentMessageEnvelope.setValid(receivedMessage.isValid());

            // send msg
            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    ServiceConfig.destinations.get(0),
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }else {
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentID(receivedMessage.getPaymentID());
            paymentMessageEnvelope.setBuyerID(receivedMessage.getBuyerID());
            paymentMessageEnvelope.setValid(false);
            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    ServiceConfig.destinations.get(3),
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }


    }
}
