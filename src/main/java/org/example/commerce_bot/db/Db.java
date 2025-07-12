package org.example.commerce_bot.db;

import org.example.commerce_bot.entity.Book;
import org.example.commerce_bot.entity.Cart;
import org.example.commerce_bot.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Db {
    public static List<User> users = new ArrayList<>();
    public static List<Cart> carts = new ArrayList<>();
    public static  List<Book> books = new ArrayList<>(List.of(
            new Book(UUID.randomUUID(),"book1",7.5),
            new Book(UUID.randomUUID(),"book2",9.5),
            new Book(UUID.randomUUID(), "book3",2.0),
              new Book(UUID.randomUUID(), "book4",6.3)
    ));
}
