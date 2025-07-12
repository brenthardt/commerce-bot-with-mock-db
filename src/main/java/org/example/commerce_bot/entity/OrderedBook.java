package org.example.commerce_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderedBook {
    private UUID id;
    private Book book;
    private String title;
    private Double price;
    private Integer quantity;


}
