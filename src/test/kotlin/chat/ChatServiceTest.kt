package chat

import chat.ChatService.getUnreadChatsCount
import exceptions.AccessErrorException
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import project.exceptions.ChatNotFoundException
import project.exceptions.MessageNotFoundException

class ChatServiceTest {

    @After
    fun reset() {
        ChatService.reset()
    }

    @Test
    fun sendMessage_returns_messageId_when_addsNewMessage_and_createNewChat_if_chatIdDoesntExist_emptyListOfChats() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val expectedResultMessageId = 1
        val expectedResultChatId = 1

        val actualResult = ChatService.addMessage(user1, message = message1)

        assertEquals(expectedResultMessageId, actualResult)
        assertEquals(expectedResultChatId, ChatService.getLastChatIdTest())
    }

    @Test
    fun sendMessage_returns_messageId_when_addsNewMessage_and_createNewChat_if_chatIdDoesntExist_filledListOfChats() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val expectedResultMessageId = 2
        val expectedResultChatId = 2

        ChatService.addMessage(user1, message = message1)
        val actualResult = ChatService.addMessage(user1, message = message2)

        assertEquals(expectedResultMessageId, actualResult)
        assertEquals(expectedResultChatId, ChatService.getLastChatIdTest())
    }

    @Test
    fun sendMessage_returns_messageId_when_addsNewMessageToTheExistingChat_byItsId() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val expectedResultMessageId = 3
        val expectedResultChatId = 2

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user1, message = message2)
        val actualResult = ChatService.addMessage(user2, chatId = 2, message = message3)

        assertEquals(expectedResultMessageId, actualResult)
        assertEquals(expectedResultChatId, ChatService.getLastChatIdTest())
    }


    @Test
    fun editMessage_returns_true_when_successfully_editsMessageText() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, chatId = 1, message = message2)
        val actualResult = ChatService.editMessage(user1, 1, "Замена")

        assertTrue(actualResult)
    }

    @Test(expected = AccessErrorException::class)
    fun editMessage_throws_AccessErrorException_when_editingMessageWasntCreatedByUser() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, chatId = 1, message = message2)
        ChatService.editMessage(user2, 1, "Замена")
    }

    @Test(expected = MessageNotFoundException::class)
    fun editMessage_throws_MessageNotFoundException_when_messageIsMarkedAsDeleted() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460, deleted = true)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, chatId = 1, message = message2)
        ChatService.deleteMessage(user2, 2)
        ChatService.editMessage(user2, 2, "Замена")
    }

    @Test(expected = MessageNotFoundException::class)
    fun editMessage_throws_MessageNotFoundException_when_messageIdDoesntExists() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.editMessage(user1, 2, "Замена")
    }


    @Test
    fun deleteMessage_returns_true_when_message_successfully_markedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        val actualResult = ChatService.deleteMessage(user1, 1)

        assertTrue(actualResult)
    }

    @Test
    fun deleteMessage_returns_true_when_message_successfully_markedAsDeleted_when_allMessagesAreDeleted_deletesChat() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        val actualResult = ChatService.deleteMessage(user1, 1)

        assertTrue(actualResult)
        assertTrue(ChatService.getChatStatusByIdTest(1))
    }

    @Test(expected = AccessErrorException::class)
    fun deleteMessage_throws_AccessErrorException_when_deletingMessageWasntCreatedByUser() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user2, 1)
    }

    @Test(expected = MessageNotFoundException::class)
    fun deleteMessage_throws_AccessErrorException_when_messageIsAlreadyMarkedAsDeleted() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.deleteMessage(user1, 1)
    }

    @Test(expected = MessageNotFoundException::class)
    fun deleteMessage_throws_AccessErrorException_when_messageIdDoesntExists() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 2)
    }


    @Test
    fun getAllMessagesFromChat_returns_MutableListOfMessagesFromChat_byChatId_marksMessagesAsRead() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val expectedResult: MutableList<Message> = arrayListOf(
            Message(id = 1, userId = 999, text = "Сообщение 1", date = 1649311450, unread = false),
            Message(id = 3, userId = 999, text = "Сообщение 3", date = 1649311470, unread = false)
        )

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, 1, message = message2)
        ChatService.addMessage(user1, 1, message = message3)
        ChatService.deleteMessage(user2, 2)
        val actualResult = ChatService.getAllMessagesFromChat(1)

        assertEquals(expectedResult, actualResult)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getAllMessagesFromChat_throws_ChatNotFoundException_when_chatIdDoesntExist() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.getAllMessagesFromChat(2)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getAllMessagesFromChat_throws_ChatNotFoundException_when_chatMarkedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.getAllMessagesFromChat(1)
    }


    @Test
    fun getSeveralMessagesFromChat_returns_MutableListOfSpecifiedAmountOfMessagesFromChat_byChatId_marksMessagesAsRead() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val message4 = Message(text = "Сообщение 4", date = 1649311480)
        val message5 = Message(text = "Сообщение 4", date = 1649311490)
        val expectedResult: MutableList<Message> = arrayListOf(
            Message(id = 1, userId = 999, text = "Сообщение 1", date = 1649311450, unread = false),
            Message(id = 3, userId = 999, text = "Сообщение 3", date = 1649311470, unread = false)
        )

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, 1, message = message2)
        ChatService.addMessage(user1, 1, message = message3)
        ChatService.addMessage(user2, 1, message = message4)
        ChatService.addMessage(user1, 1, message = message5)
        ChatService.deleteMessage(user2, 2)
        val actualResult = ChatService.getSeveralMessagesFromChat(1, 1, 2)

        assertEquals(expectedResult, actualResult)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getSeveralMessagesFromChat_throws_ChatNotFoundException_when_chatIdDoesntExist() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.getSeveralMessagesFromChat(2, 1, 1)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getSeveralMessagesFromChat_throws_ChatNotFoundException_when_chatMarkedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.getSeveralMessagesFromChat(1, 1, 1)
    }

    @Test(expected = MessageNotFoundException::class)
    fun getSeveralMessagesFromChat_throws_MessageNotFoundException_when_messageIdDoesntExist() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.getSeveralMessagesFromChat(1, 2, 1)
    }

    @Test(expected = MessageNotFoundException::class)
    fun getSeveralMessagesFromChat_throws_MessageNotFoundException_when_messageMarkedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user1, 1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.getSeveralMessagesFromChat(1, 1, 1)
    }


    @Test
    fun createChat_adds_newChat_to_listOfChats() {
        val expectedResult: MutableList<Chat> = arrayListOf(Chat(id = 1, date = 1649311450), Chat(id = 2, date = 1649311460))

        ChatService.createChat(1649311450)
        ChatService.createChat(1649311460)
        val actualResult = ChatService.getListOfChatsTest()

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun getUserChats_returns_correct_mutableMapOfUsersAllChatsAndLastMessageInChat_by_userId_standard() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val message4 = Message(text = "Сообщение 4", date = 1649311480)
        val expectedMessage1 = Message(1, 999, "Сообщение 1", 1649311450, true, false)
        val expectedMessage2 = Message(2, 111, "Сообщение 2", 1649311460, true, false)
        val expectedMessage3 = Message(3, 999, "Сообщение 3", 1649311470, true, false)
        val expectedMessage4 = Message(4, 999, "Сообщение 4", 1649311480, true, true)
        val expectedChat1 = Chat(1, arrayListOf(expectedMessage1), 1649311450, false)
        val expectedChat2 = Chat(2, arrayListOf(expectedMessage2, expectedMessage3, expectedMessage4), 1649311460, false)
        val expectedResult: MutableMap<Chat, Message> = hashMapOf(
            expectedChat1 to expectedMessage1,
            expectedChat2 to expectedMessage3
        )

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, message = message2)
        ChatService.addMessage(user1, chatId = 2, message = message3)
        ChatService.addMessage(user1, chatId = 2, message = message4)
        ChatService.deleteMessage(user1, 4)
        val actualResult = ChatService.getUserChats(999)

        assertEquals(expectedResult, actualResult)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getUserChats_throws_ChatNotFoundException_when_userDoesntHaveChats() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.getUserChats(111)
    }

    @Test(expected = ChatNotFoundException::class)
    fun getUserChats_throws_ChatNotFoundException_when_userChatsMarkedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.getUserChats(999)
    }


    @Test
    fun getUnreadChatsCount_returns_correct_numberOfUserChatsHavingUnreadMessages_byUserId(){
        val user1 = 999
        val user2 = 111
        val user3 = 222
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val message4 = Message(text = "Сообщение 4", date = 1649311480)
        val expectedResult = 2

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, chatId = 1, message = message2)
        ChatService.addMessage(user2, message = message3)
        ChatService.addMessage(user1, chatId = 2, message = message4)
        ChatService.addMessage(user3, message = message1)
        ChatService.addMessage(user1, chatId = 3, message = message2)
        ChatService.deleteMessage(user3, 5)
        val actualResult = ChatService.getListOfChatsTest().getUnreadChatsCount(999)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun getUnreadChatsCount_returns_correct_numberOfUserChatsHavingUnreadMessages_byUserId_zero(){
        val user1 = 999
        val user2 = 111
        val user3 = 222
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val message3 = Message(text = "Сообщение 3", date = 1649311470)
        val message4 = Message(text = "Сообщение 4", date = 1649311480)
        val expectedResult = 0

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user1, chatId = 1, message = message2)
        ChatService.deleteChat(1)
        ChatService.addMessage(user2, message = message3)
        ChatService.addMessage(user1, chatId = 2, message = message4)
        ChatService.getAllMessagesFromChat(2)
        ChatService.addMessage(user3, message = message1)
        ChatService.addMessage(user1, chatId = 3, message = message2)
        ChatService.deleteMessage(user3, 5)
        val actualResult = ChatService.getListOfChatsTest().getUnreadChatsCount(999)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun deleteChat_marks_chatByItsId_and_allChatMessages_as_deleted() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)
        val expectedMessage1 = Message(1, 999, "Сообщение 1", 1649311450,  true, true)
        val expectedMessage2 = Message(2, 111, "Сообщение 2", 1649311460, true, true)
        val expectedResultChat = Chat(1, arrayListOf(expectedMessage1,expectedMessage2), 1649311450, true)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, 1, message2)
        ChatService.deleteChat(1)
        val actualResult = ChatService.getListOfChatsTest().last()

        assertEquals(expectedResultChat, actualResult)
    }

    @Test(expected = ChatNotFoundException::class)
    fun deleteChat_throws_ChatNotFoundException_when_chadIdDoesntExist() {
        val user1 = 999
        val user2 = 111
        val message1 = Message(text = "Сообщение 1", date = 1649311450)
        val message2 = Message(text = "Сообщение 2", date = 1649311460)

        ChatService.addMessage(user1, message = message1)
        ChatService.addMessage(user2, 1, message2)
        ChatService.deleteChat(2)
    }

    @Test(expected = ChatNotFoundException::class)
    fun deleteChat_throws_ChatNotFoundException_when_chadIsAlreadyMarkedAsDeleted() {
        val user1 = 999
        val message1 = Message(text = "Сообщение 1", date = 1649311450)

        ChatService.addMessage(user1, message = message1)
        ChatService.deleteMessage(user1, 1)
        ChatService.deleteChat(1)
    }
}