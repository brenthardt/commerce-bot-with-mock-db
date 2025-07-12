package org.example.commerce_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.commerce_bot.status.Status;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {
    private Long id;
    private String contact;
    private Double latitude;
    private Double longitude;
    private String paymentChoice;
    public Status status;
    public Integer MenuMessageId;
    public Integer bookMessageId;
    public Integer orderMessageId;
    public OrderedBook currentlyOrderedBook;
    public List<OrderedBook> basket = new ArrayList<>();



}
