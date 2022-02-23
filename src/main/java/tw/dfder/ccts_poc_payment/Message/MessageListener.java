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
    public void receivedRequest(String msg, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag, Channel ch){
//        decode the message
        PaymentMessageEnvelope receivedMessage = gson.fromJson(msg, PaymentMessageEnvelope.class);


        if (receivedMessage.getMethod().equals("pay")){
            paymentProcess(receivedMessage);
        }else if(receivedMessage.getMethod().equals("get")){
            queryProcess(receivedMessage);
        }


    }


    public void paymentProcess(PaymentMessageEnvelope receivedMessage){

        if (receivedMessage.getPaymentId() != null && receivedMessage.getBuyerId() != null && receivedMessage.isValid()) {
            // return a valid result
            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(receivedMessage.isValid());

            repo.save(paymentMessageEnvelope);
            // send msg
            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    ServiceConfig.destinations.get(0),
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }else {

            PaymentMessageEnvelope paymentMessageEnvelope = new PaymentMessageEnvelope();
            paymentMessageEnvelope.setPaymentId(receivedMessage.getPaymentId());
            paymentMessageEnvelope.setBuyerId(receivedMessage.getBuyerId());
            paymentMessageEnvelope.setValid(false);

            repo.save(paymentMessageEnvelope);

            sender.sendRequestMessage(
                    gson.toJson(paymentMessageEnvelope),
                    ServiceConfig.destinations.get(3),
                    RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                    ServiceConfig.serviceName
            );
        }
    }



    // find by paymentid
    public void queryProcess(PaymentMessageEnvelope receivedMessage){

        repo.findByPaymentId(receivedMessage.getPaymentId());
        sender.sendRequestMessage(
                gson.toJson(receivedMessage),
                ServiceConfig.destinations.get(3),
                RabbitmqConfig.ROUTING_PAYMENT_RESPONSE,
                ServiceConfig.serviceName
        );


    }



}
