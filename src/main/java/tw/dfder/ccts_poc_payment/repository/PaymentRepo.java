package tw.dfder.ccts_poc_payment.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tw.dfder.ccts_poc_payment.Entity.PaymentMessageEnvelope;

@Repository
public interface PaymentRepo extends MongoRepository<PaymentMessageEnvelope,String> {
    PaymentMessageEnvelope findByPaymentId(String pid);

}
