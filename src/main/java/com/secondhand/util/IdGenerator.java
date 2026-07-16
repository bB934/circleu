package com.secondhand.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    private final AtomicLong buyerSeq = new AtomicLong(0);
    private final AtomicLong sellerSeq = new AtomicLong(0);
    private final AtomicLong orderSeq = new AtomicLong(0);

    public String generateBuyerNumber() {
        return "B" + timeStamp() + String.format("%04d", buyerSeq.incrementAndGet() % 10000);
    }

    public String generateSellerNumber() {
        return "S" + timeStamp() + String.format("%04d", sellerSeq.incrementAndGet() % 10000);
    }

    public String generateOrderNumber() {
        return "O" + timeStamp() + String.format("%04d", orderSeq.incrementAndGet() % 10000);
    }

    private String timeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
}