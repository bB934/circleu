package com.secondhand.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDelayMessage implements Serializable {
    public static final String TYPE_PAY_TIMEOUT = "PAY_TIMEOUT";   // 待付款超时（15分钟）
    public static final String TYPE_SHIP_TIMEOUT = "SHIP_TIMEOUT";  // 待发货超时（24小时）

    private Long orderId;
    private Long userId;
    private String orderNumber;
    private String type;
}
