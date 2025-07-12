package org.example.commerce_bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    private UUID id;
    private User user;
    private List<OrderedBook> orderedBooks = new ArrayList<>();
}
