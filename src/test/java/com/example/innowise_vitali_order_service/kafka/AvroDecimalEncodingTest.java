package com.example.innowise_vitali_order_service.kafka;

import com.example.events.OrderCreatedEvent;
import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvroDecimalEncodingTest {

    @Test
    void shouldRoundTripDecimalUsingAvroLogicalType() throws Exception {
        BigDecimal original = new BigDecimal("19.99");
        OrderCreatedEvent avroEvent = new AvroEventMapper().toOrderCreatedAvro(
                new com.example.innowise_vitali_order_service.kafka.OrderCreatedEvent("1001", "user-1", original)
        );

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(output, null);
        SpecificDatumWriter<OrderCreatedEvent> writer = new SpecificDatumWriter<>(OrderCreatedEvent.getClassSchema());
        writer.write(avroEvent, encoder);
        encoder.flush();

        Schema schema = OrderCreatedEvent.getClassSchema();
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(output.toByteArray(), null);
        SpecificDatumReader<OrderCreatedEvent> reader = new SpecificDatumReader<>(schema);
        OrderCreatedEvent decoded = reader.read(null, decoder);

        assertEquals(original, decoded.getTotalPrice());
    }
}
