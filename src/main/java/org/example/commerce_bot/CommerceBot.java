package org.example.commerce_bot;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.example.commerce_bot.db.Db;
import org.example.commerce_bot.entity.Book;
import org.example.commerce_bot.entity.Cart;
import org.example.commerce_bot.entity.OrderedBook;
import org.example.commerce_bot.entity.User;
import org.example.commerce_bot.status.Status;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.*;
import static org.example.commerce_bot.db.Db.*;

public class CommerceBot extends TelegramLongPollingBot {
    private final Map<Long, Cart> userCartMap = new HashMap<>();
    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {

if(update.hasMessage()){
    Message message = update.getMessage();
    Long chatId = message.getChatId();
    User user = getUser(chatId);

    //Start
    if (message.hasText()) {
String text = message.getText();

if(text.equalsIgnoreCase("/start") && user.getStatus().equals(Status.START)){
    user.setStatus(Status.CATEGORY);

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText("Select a category below:");
    sendMessage.setReplyMarkup(categoryBtn());
    Message execute = execute(sendMessage);
    user.setMenuMessageId(execute.getMessageId());

}
else if (user.getStatus().equals(Status.CATEGORY)) {
    switch (text) {
        case "history" -> {
            user.setStatus(Status.HISTORY_MENU);
            SendMessage historyMenuMessage = new SendMessage();
            historyMenuMessage.setChatId(chatId);
            historyMenuMessage.setText("History books");
            historyMenuMessage.setReplyMarkup(HistoryBtn());
            Message execute = execute(historyMenuMessage);
            user.setMenuMessageId(execute.getMessageId());


        }
        case "religion" -> {
            user.setStatus(Status.RELIGION);
            SendMessage religionMenuMessage = new SendMessage();
            religionMenuMessage.setChatId(chatId);
            religionMenuMessage.setText("Religious books");
            Message execute = execute(religionMenuMessage);
            user.setMenuMessageId(execute.getMessageId());


        }
        case "science" -> {
            user.setStatus(Status.SCIENCE);
            SendMessage scienceMenuMessage = new SendMessage();
            scienceMenuMessage.setChatId(chatId);
            scienceMenuMessage.setText("Science books");
            Message execute = execute(scienceMenuMessage);
            user.setMenuMessageId(execute.getMessageId());



        }
        case "tech" -> {
            user.setStatus(Status.TECH);
            SendMessage techMenuMessage = new SendMessage();
            techMenuMessage.setChatId(chatId);
            techMenuMessage.setText("Technology books");
            Message execute = execute(techMenuMessage);
            user.setMenuMessageId(execute.getMessageId());


        }
    }
}
else if (user.getStatus().equals(Status.HISTORY_MENU) && text.equals("return")) {
    user.setStatus(Status.CATEGORY);
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText("Back to Categories...");
    sendMessage.setReplyMarkup(categoryBtn());
    execute(sendMessage);

    deleteMessage(chatId, user.getMenuMessageId());
    user.setMenuMessageId(null);

}
    }
//Start

    //smallCategory
if(message.hasText()){
    if (message.getText().equalsIgnoreCase("/menu") && user.getStatus().equals(Status.START)) {
        user.setStatus(Status.HISTORY_MENU);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("History books");
        sendMessage.setReplyMarkup(HistoryBtn());
        Message execute = execute(sendMessage);
        user.setMenuMessageId(execute.getMessageId());
    }
    //smallCategory

    //HistoryBooks
    else if (user.getStatus().equals(Status.HISTORY_MENU)) {
        String text = message.getText();

        if (text.equals("book1") || text.equals("book2") || text.equals("book3") || text.equals("book4")) {
            Book foundBook = books.stream().filter(book->book.getTitle().equals(text)).findFirst().get();

            OrderedBook orderBook = new OrderedBook(UUID.randomUUID(), foundBook, foundBook.getTitle(), foundBook.getPrice(),1);
            user.setCurrentlyOrderedBook(orderBook);

            user.setStatus(Status.SELECTION);
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            File file = new File(text.equals("book1") ? "files/book1.jpg" : text.equals("book2") ? "files/book2.jpg" : text.equals("book3") ? "files/book3.webp" : "files/book4.webp");
            InputFile inputFile = new InputFile(file);
            sendPhoto.setPhoto(inputFile);
            sendPhoto.setCaption(text);

            sendPhoto.setReplyMarkup(historyInlineBtn(user));
            Message execute = execute(sendPhoto);
            user.setBookMessageId(execute.getMessageId());

            DeleteHistoryMenuBtn(user);

        }

        else if (text.equals("show cart")) {
            user.setStatus(Status.BUY);
            if (user.getBasket().isEmpty()) {
                SendMessage emptyCartMessage = new SendMessage();
                emptyCartMessage.setChatId(chatId);
                emptyCartMessage.setText("Your cart is empty");
                execute(emptyCartMessage);
            } else {
                Cart cart = new Cart(UUID.randomUUID(), user, new ArrayList<>(user.getBasket()));
                carts.add(cart);

                String bookDetails = historyBookInfo(cart);
                user.setBasket(new ArrayList<>());

                userCartMap.put(chatId, cart);

                SendMessage cartMessage = new SendMessage();
                cartMessage.setText(bookDetails);
                cartMessage.setChatId(chatId);
                execute(cartMessage);

                System.out.println("Sending cart for chatId " + chatId + ": " + cart.getOrderedBooks());
            }
            SendMessage orderQuestionMessage = new SendMessage();
            orderQuestionMessage.setChatId(chatId);
            orderQuestionMessage.setText("Do you want to order now ?");
            orderQuestionMessage.setReplyMarkup(buyOptionBtn());
            Message sentOrderQuestionMessage = execute(orderQuestionMessage);
            user.setOrderMessageId(sentOrderQuestionMessage.getMessageId());
        }




    }
    //HistoryBooks


}
else if (user.getStatus().equals(Status.CONTACT) && message.hasContact()) {
    deleteMessage(chatId, message.getMessageId());
    user.setStatus(Status.LOCATION);
    user.setContact(String.valueOf(message.getContact()));
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText("Share location");
    sendMessage.setReplyMarkup(LocationBtn());
    Message sentMessage = execute(sendMessage);
    user.setMenuMessageId(sentMessage.getMessageId());

} else if(user.getStatus().equals(Status.LOCATION) && message.hasLocation()){
    user.setStatus(Status.PAYMENT);

    deleteMessage(chatId, message.getMessageId());
    deleteMessage(chatId, user.getMenuMessageId());

    user.setLatitude(message.getLocation().getLatitude());
    user.setLongitude(message.getLocation().getLongitude());
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText("Select a payment Method:");
    sendMessage.setReplyMarkup(paymentBtn());
    execute(sendMessage);
}

}
//CallbackQuery
        if(update.hasCallbackQuery()){
            CallbackQuery callbackQuery = update.getCallbackQuery();
            Long Id = callbackQuery.getFrom().getId();
            String data = callbackQuery.getData();
            User user = getUser(Id);

            switch (data) {
                case "+" -> {
                    user.getCurrentlyOrderedBook().setQuantity(user.getCurrentlyOrderedBook().getQuantity() + 1);
                    editBook(user);
                }
                case "-" -> {
                    int currentQuantity = user.getCurrentlyOrderedBook().getQuantity();
                    if (currentQuantity > 0) {
                        user.getCurrentlyOrderedBook().setQuantity(currentQuantity - 1);
                        editBook(user);
                    } else {

                        if (user.getBookMessageId() != null) {
                            DeleteMessage deleteMessage = new DeleteMessage();
                            deleteMessage.setChatId(Id);
                            deleteMessage.setMessageId(user.getBookMessageId());
                            execute(deleteMessage);
                        }
                        user.setStatus(Status.HISTORY_MENU);
                        user.setCurrentlyOrderedBook(null);

                    }
                }
                case "add to cart" -> {
                    if (user.getCurrentlyOrderedBook().getQuantity() == 0) {
                        SendMessage warningMessage = new SendMessage();
                        warningMessage.setChatId(Id);
                        warningMessage.setText("⚠️ Quantity cannot be 0");
                        execute(warningMessage);
                    } else {
                        user.setStatus(Status.HISTORY_MENU);
                        OrderedBook currentOrderBook = user.getCurrentlyOrderedBook();
                        user.getBasket().add(currentOrderBook);

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setText("✅ Added to cart");
                        id2(Id, user, sendMessage);
                    }
                }
                case "cancel" -> {
                    user.setStatus(Status.HISTORY_MENU);

                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setText("Returned to books");
                    id2(Id, user, sendMessage);
                }
                case "yes" -> {
                    user.setStatus(Status.CONTACT);
                    deleteMessage(Id, user.getOrderMessageId());
                    user.setOrderMessageId(null);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(Id);
                    sendMessage.setText("Share your contact:");
                    sendMessage.setReplyMarkup(ContactBtn());
                    Message sentMessage = execute(sendMessage);
                    user.setMenuMessageId(sentMessage.getMessageId());
                }
                case "no" -> {
                    user.setStatus(Status.CATEGORY);
                    deleteMessage(Id, user.getOrderMessageId());
                    user.setOrderMessageId(null);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(Id);
                    sendMessage.setText("Thanks for your visit. Type /start to start again.");
                    sendMessage.setReplyMarkup(categoryBtn());
                    execute(sendMessage);
                }
                case "return" -> {
                    user.setStatus(Status.CATEGORY);
                    deleteMessage(Id, user.getOrderMessageId());
                    user.setOrderMessageId(null);
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(Id);
                    sendMessage.setText("Back to Categories...");
                    sendMessage.setReplyMarkup(categoryBtn());
                    execute(sendMessage);
                }
                case "Cash💵"->{
                    user.setPaymentChoice("cash");
                    user.setStatus(Status.CONFIRMED);
                    sendConfirmation(Id,user,callbackQuery.getMessage().getMessageId());
                }
                case "Card💳"->{
                    user.setPaymentChoice("card");
                    user.setStatus(Status.CONFIRMED);
                    sendConfirmation(Id,user,callbackQuery.getMessage().getMessageId());
                }
                default -> System.out.println("Invalid action " + data);
            }


        }
        //CallbackQuery

    }

    private void deleteMessage(Long chatId, Integer messageId) throws TelegramApiException {
        if(messageId != null){
            try {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(chatId);
                deleteMessage.setMessageId(messageId);
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                System.err.println("Error deleting message: " + e.getMessage() + " (Error Code: " + e.getCause() + ")");
            }


        }else{
            System.out.println("message id is null");
        }
    }

    private void sendConfirmation(Long chatId, User user,Integer paymentMessageId) throws TelegramApiException {
        Cart cart = userCartMap.get(chatId);
        if (cart == null) {
            SendMessage cartNotFoundMessage = new SendMessage();
            cartNotFoundMessage.setChatId(chatId);
            cartNotFoundMessage.setText("⚠️ Your cart is empty. Please add items to your cart before confirming.");
            execute(cartNotFoundMessage);
            return;
        }
        String orderDetails = historyBookInfo(cart);

        StringBuilder confirmationMessage = new StringBuilder();
        confirmationMessage.append("✅ *Order Confirmed!* ✅\n\n");
        confirmationMessage.append("Thank you for your purchase!\n\n");
        confirmationMessage.append("💳*Payment Method:* ").append(user.getPaymentChoice()).append("\n\n");
        confirmationMessage.append(orderDetails).append("\n");
        if (user.getContact() != null) {
            confirmationMessage.append("👤 *Your Contact:* ").append(user.getContact()).append("\n");
        }
        if (user.getLatitude() != null) {
            confirmationMessage.append("📍 *Location:* Latitude: ").append(user.getLatitude())
                    .append(", Longitude: ").append(user.getLongitude()).append("\n");
        }

        confirmationMessage.append("\nWe appreciate your business! 😊");
        confirmationMessage.append("\nContact us. 📞: 123-456-7890");

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setParseMode("Markdown");
        sendMessage.setText(confirmationMessage.toString());
        execute(sendMessage);

        userCartMap.remove(chatId);
        user.setBasket(new ArrayList<>());
        user.setStatus(Status.START);

        deleteMessage(chatId, paymentMessageId);
        user.setMenuMessageId(null);
        deleteMessage(chatId, user.getOrderMessageId());
        user.setOrderMessageId(null);

    }

    private InlineKeyboardMarkup paymentBtn() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        rows.add(row);

        InlineKeyboardButton cash = new InlineKeyboardButton();
        cash.setText("Cash💵");
        cash.setCallbackData("Cash💵");
        row.add(cash);

        InlineKeyboardButton card = new InlineKeyboardButton();
        card.setText("Card💳");
        card.setCallbackData("Card💳");
        row.add(card);

        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardMarkup buyOptionBtn(){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
       rows.add(row);
       rows.add(row2);

    InlineKeyboardButton yes = new InlineKeyboardButton();
    yes.setText("yes");
    yes.setCallbackData("yes");
    row.add(yes);

    InlineKeyboardButton no = new InlineKeyboardButton();
    no.setText("no");
    no.setCallbackData("no");
    row.add(no);

    InlineKeyboardButton return2 = new InlineKeyboardButton();
    return2.setText("return");
    return2.setCallbackData("return");
    row2.add(return2);

    return new InlineKeyboardMarkup(rows);
}

    private ReplyKeyboardMarkup ContactBtn(){
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        rows.add(row);

        KeyboardButton btnC = new KeyboardButton();
        btnC.setText("share contact");
        btnC.setRequestContact(true);
        row.add(btnC);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private ReplyKeyboardMarkup LocationBtn(){
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        rows.add(row);

        KeyboardButton btnL = new KeyboardButton();
        btnL.setText("share location");
        btnL.setRequestLocation(true);
        row.add(btnL);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private void id2(Long id, User user, SendMessage sendMessage) throws TelegramApiException {
        sendMessage.setChatId(id);
        sendMessage.setReplyMarkup(HistoryBtn());
        Message execute = execute(sendMessage);
        user.setMenuMessageId(execute.getMessageId());

        if (user.getBookMessageId() != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(id);
            deleteMessage.setMessageId(user.getBookMessageId());
            execute(deleteMessage);
        }

        user.setBookMessageId(null);
    }

    private void editBook(User user) throws TelegramApiException {
        EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup();
        editMessageReplyMarkup.setChatId(user.getId());
        editMessageReplyMarkup.setMessageId(user.getBookMessageId());
        editMessageReplyMarkup.setReplyMarkup(historyInlineBtn(user));
        execute(editMessageReplyMarkup);
    }

    private @NonNull String historyBookInfo(Cart cart) {
        StringBuilder s = new StringBuilder();
        double totalPrice = 0.0;

        s.append("🛒 Your Order:\n");
        for (OrderedBook orderBook : cart.getOrderedBooks()) {
            double bookTotal = orderBook.getPrice() * orderBook.getQuantity();
            s.append("- ")
                    .append(orderBook.getBook().getTitle())
                    .append(" x")
                    .append(orderBook.getQuantity())
                    .append(" - $")
                    .append(bookTotal)
                    .append("\n");
            totalPrice += bookTotal;
        }
        s.append("\n💰 Total Price: $").append(totalPrice);

        return s.toString();
    }

    private void DeleteHistoryMenuBtn(User user) throws TelegramApiException {
        Integer menuMessageId = user.getMenuMessageId();
        if (menuMessageId != null) {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(user.getId());
            deleteMessage.setMessageId(menuMessageId);
            execute(deleteMessage);
        } else {
            System.out.println("Menu message ID is null. No message to delete.");
        }
    }

    private InlineKeyboardMarkup historyInlineBtn(User user){
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        rows.add(row);
        rows.add(row2);
        rows.add(row3);

        InlineKeyboardButton plus = new InlineKeyboardButton();
        plus.setText("+");
        plus.setCallbackData("+");
        row.add(plus);

        InlineKeyboardButton quantity = new InlineKeyboardButton();
        quantity.setText(user.getCurrentlyOrderedBook().getQuantity()+"");
        quantity.setCallbackData("1");
        row.add(quantity);

        InlineKeyboardButton minus = new InlineKeyboardButton();
        minus.setText(user.getCurrentlyOrderedBook().getQuantity() == 0 ? "❌" : "-");
        minus.setCallbackData("-");
        row.add(minus);

        InlineKeyboardButton addToCart = new InlineKeyboardButton();
        addToCart.setText("add to cart");
        addToCart.setCallbackData("add to cart");
        row2.add(addToCart);

        InlineKeyboardButton cancel = new InlineKeyboardButton();
        cancel.setText("cancel");
        cancel.setCallbackData("cancel");
        row3.add(cancel);

        return new InlineKeyboardMarkup(rows);

    }

    private ReplyKeyboardMarkup HistoryBtn(){
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow rowTop = new KeyboardRow();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        rows.add(rowTop);
        rows.add(row);
        rows.add(row2);
        rows.add(row3);

        KeyboardButton return1 = new KeyboardButton();
        return1.setText("return");
        rowTop.add(return1);

        KeyboardButton book1 = new KeyboardButton();
        book1.setText("book1");
        row.add(book1);

        KeyboardButton book2 = new KeyboardButton();
        book2.setText("book2");
        row.add(book2);

        KeyboardButton book3 = new KeyboardButton();
        book3.setText("book3");
        row2.add(book3);

        KeyboardButton book4 = new KeyboardButton();
        book4.setText("book4");
        row2.add(book4);

        KeyboardButton orderButton = new KeyboardButton();
        orderButton.setText("show cart");
        row3.add(orderButton);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private ReplyKeyboardMarkup categoryBtn() {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row1 = new KeyboardRow();

        KeyboardButton historyBtn = new KeyboardButton("history");
        KeyboardButton religionBtn = new KeyboardButton("religion");
        KeyboardButton scienceBtn = new KeyboardButton("science");
        KeyboardButton techBtn = new KeyboardButton("tech");

        row.add(historyBtn);
        row.add(religionBtn);
        row1.add(scienceBtn);
        row1.add(techBtn);

        rows.add(row);
        rows.add(row1);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private User getUser(Long chatId){
        for (User user : Db.users) {
            if(user.getId().equals(chatId)){
                return user;
            }
        }
        User user = new User();
        user.setId(chatId);
        user.setStatus(Status.START);
        users.add(user);
        return user;
    }

    @Override
    public String getBotUsername() {
        return keys.userName;
    }

    @Override
    public String getBotToken() {
        return keys.token;
    }
}