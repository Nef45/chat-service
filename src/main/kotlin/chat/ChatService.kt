package chat

import exceptions.AccessErrorException
import project.exceptions.ChatNotFoundException
import project.exceptions.MessageNotFoundException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


object ChatService {
    private var listOfChats: MutableList<Chat> = ArrayList()


    /**
     * Добавляет от указанного пользователя сообщение, присваивая ему уникальный для всех чатов id,
     * в указанный чат из [listOfChats]. Если указанного чата не существует - создает новый чат, присваивая ему
     * уникальный id, и добавляет его в список.
     * @param <userId> ID пользователя-отправителя.
     * @param <chatId> ID чата.
     * @param <sentMessage> Сообщение [Message] для отправки.
     * @return ID добавленного сообщения.
     */
    fun addMessage(userId: Int, chatId: Int = 0, message: Message): Int {
        for (chat in listOfChats) {
            if (chatId == chat.id) {
                chat.messages.add(
                    Message(
                        id = listOfChats.last().messages.last().id + 1,
                        userId = userId,
                        text = message.text,
                        date = message.date
                    )
                )
                return chat.messages.last().id
            }
        }
        if (listOfChats.isEmpty()) {
            val newChat = Chat(id = 1, date = message.date)
            newChat.messages.add(
                Message(
                    id = 1,
                    userId = userId,
                    text = message.text,
                    date = message.date
                )
            )
            listOfChats.add(newChat)
            return newChat.messages.last().id
        }
        val newChat = Chat(id = listOfChats.last().id + 1, date = message.date)
        newChat.messages.add(
            Message(
                id = listOfChats.last().messages.last().id + 1,
                userId = userId,
                text = message.text,
                date = message.date
            )
        )
        listOfChats.add(newChat)
        return newChat.messages.last().id
    }

    /**
     * Заменяет текст найденного по id сообщения от указанного пользователя на текст, переданный в параметре.
     * @param <userId> ID пользователя-отправителя.
     * @param <messageId> ID сообщения.
     * @param <text> Обновленный текст [Message.text] сообщения.
     * @return
     * true - текст сообщения успешно обновлен.
     * @throws <AccessErrorException> Указанный пользователь не является автором сообщения.
     * @throws <MessageNotFoundException> Указанное сообщение не существует или помечено как удаленное.
     */
    fun editMessage(userId: Int, messageId: Int, text: String): Boolean {
        for (chat in listOfChats) {
            for ((i, message) in chat.messages.withIndex()) {
                if (message.id == messageId && message.userId == userId && !message.deleted) {
                    chat.messages[i] = message.copy(text = text)
                    return true
                } else if (message.id == messageId && message.userId != userId && !message.deleted) {
                    throw AccessErrorException()
                }
            }
        }
        throw MessageNotFoundException()
    }

    /**
     * Помечает найденное по id сообщение от указанного пользователя как удаленное, присваивая [Message.deleted]
     * значение true. Если после этого все сообщения в чате помечены как удаленные, [Chat.deleted] также присваивается
     * значение true, помечая чат как удаленный.
     * @param <userId> ID пользователя-отправителя.
     * @param <messageId> ID сообщения.
     * @return
     * true - метка об удалении успешно поставлена.
     * @throws <AccessErrorException> Указанный пользователь не является автором сообщения.
     * @throws <MessageNotFoundException> Указанное сообщение не существует или уже помечено как удаленное.
     */
    fun deleteMessage(userId: Int, messageId: Int): Boolean {
        var counter = 0
        for ((c, chat) in listOfChats.withIndex()) {
            for ((i, message) in chat.messages.withIndex()) {
                if (message.deleted) {
                    counter++
                }
                if (message.id == messageId && message.userId == userId && !message.deleted) {
                    chat.messages[i] = message.copy(deleted = true)
                    counter++
                    if (counter == chat.messages.size) {
                        listOfChats[c] = chat.copy(deleted = true)
                    }
                    return true
                } else if (message.id == messageId && message.userId != userId && !message.deleted) {
                    throw AccessErrorException()
                }
            }
        }
        throw MessageNotFoundException()
    }

    /**
     * Получает список всех сообщений, не отмеченных как удаленные, из указанного чата, помечая их как прочитанные,
     * присвоив [Message.unread] значение false.
     * @param <chatId> ID чата.
     * @return Список MutableList<Message>, состоящий из всех сообщений чата.
     * @throws <ChatNotFoundException> Указанный чат не существует или помечен как удаленный.
     */
    fun getAllMessagesFromChat(chatId: Int): MutableList<Message> {
        val searchChatById: (Int, MutableList<Chat>) -> Chat = searchChatById@{ id, listOfChats ->
            for (chat in listOfChats) {
                if (chat.id == id && !chat.deleted) {
                    return@searchChatById chat
                }
            }
            throw ChatNotFoundException()
        }
        val markMessagesAsRead: (MutableList<Message>) -> MutableList<Message> = {
            for ((i, message) in it.withIndex()) {
                it[i] = message.copy(unread = false)
            }
            it
        }

        return markMessagesAsRead(searchChatById(chatId, listOfChats).messages)
            .filter { !it.deleted }
                as MutableList<Message>
    }

    /**
     * Получает в виде списка указанное количество сообщений, не отмеченных как удаленные, начиная с сообщения,
     * указанного по id. Помечает эти сообщения как прочитанные, присвоив [Message.unread] значение false.
     * @param <chatId> ID чата.
     * @param <messageId> ID сообщения, начиная с которого будет выведен список.
     * @param <numberOfMessages> Количество сообщений, которые будут выведены в список.
     * @return Список MutableList<Message>, состоящий из сообщений чата, начиная с указанного сообщения в указанном
     * количестве.
     * @throws <ChatNotFoundException> Указанный чат не существует или помечен как удаленный.
     * @throws <MessageNotFoundException> Сообщение, начиная с которого должен будет выводиться список, не существует или
     * помечено как удаленное.
     */
    fun getSeveralMessagesFromChat(chatId: Int, messageId: Int, numberOfMessages: Int): MutableList<Message> {
        val searchChatById: (Int, MutableList<Chat>) -> Chat = searchChatById@{ id, listOfChats ->
            for (chat in listOfChats) {
                if (chat.id == id && !chat.deleted) {
                    return@searchChatById chat
                }
            }
            throw ChatNotFoundException()
        }
        val markMessagesAsRead: (MutableList<Message>) -> MutableList<Message> = {
            for ((i, message) in it.withIndex()) {
                it[i] = message.copy(unread = false)
            }
            it
        }
        val findMessageIndexById: (MutableList<Message>, Int) -> Int = findMessage@{ listOfMessages, id ->
            for ((i, message) in listOfMessages.withIndex()) {
                if (message.id == id && !message.deleted) {
                    return@findMessage i
                }
            }
            throw MessageNotFoundException()
        }

        val listOfUndeletedMessages = searchChatById(chatId, listOfChats)
            .messages
            .filter { !it.deleted }
                as MutableList<Message>

        val numberOfMessagesWithinBorders = if (
            (listOfUndeletedMessages.size - findMessageIndexById(listOfUndeletedMessages, messageId)) >= numberOfMessages) {
            numberOfMessages
        } else {
            listOfUndeletedMessages.size - findMessageIndexById(listOfUndeletedMessages, messageId)
        }

        return markMessagesAsRead(
            listOfUndeletedMessages
                .subList(
                    findMessageIndexById(listOfUndeletedMessages, messageId),
                    findMessageIndexById(listOfUndeletedMessages, messageId) + numberOfMessagesWithinBorders
                )
        )
    }

    /**
     * Добавляет новый чат в список [listOfChats], присваивая ему уникальный id.
     * @param <date> Дата создания чата.
     */
    fun createChat(date: Int) {
        val chatId = if (listOfChats.isEmpty()) {
            1
        } else {
            listOfChats.last().id + 1
        }
        val newChat = Chat(id = chatId, date = date)
        listOfChats.add(newChat)
    }

    /**
     * Получает коллекцию всех чатов и последних сообщений в них, в которых пользователь с указанным id оставил хотя бы
     * одно сообщение, не отмеченное как удаленное.
     * @param <userId> ID пользователя.
     * @return Коллекция MutableMap<Chat, Message>, состоящая из всех чатов пользователя и последних сообщений в них, не
     * отмеченных как удаленные.
     * @throws <ChatNotFoundException> У пользователя нет чатов, либо они помечены как удаленные.
     */
    fun getUserChats(userId: Int): MutableMap<Chat, Message> {
        val listOfUserChats: MutableMap<Chat, Message> = HashMap()
        for (chat in listOfChats) {
            if (!chat.deleted) {
                for (message in chat.messages) {
                    if (message.userId == userId && !message.deleted) {
                        var undeletedMessages: MutableList<Message> = chat.messages
                        if (chat.messages.last().deleted) {
                            undeletedMessages = chat.messages.filter { !it.deleted } as MutableList<Message>
                        }
                        listOfUserChats[chat] = undeletedMessages.last()
                        break
                    }
                }
            }
        }
        if (listOfUserChats.isEmpty()) {
            throw ChatNotFoundException()
        }
        return listOfUserChats
    }

    /**
     * Подсчитывает количество всех неудаленных чатов, в которых у пользователя с указанным id есть хотя бы одно
     * непрочитанное сообщение от другого пользователя.
     * @param <userId> ID пользователя.
     * @return Количество чатов с непрочитанными сообщениями.
     */
    fun <Chat> MutableList<Chat>.getUnreadChatsCount(userId: Int): Int {
        var counter: Int = 0
        for (chat in listOfChats) {
            if (chat.deleted) {
                continue
            }
            for (message in chat.messages) {
                if (message.userId == userId) {
                    continue
                }
                if (message.userId != userId && !message.deleted) {
                    if (message.unread) {
                        counter++
                        break
                    }
                }
            }
        }
        return counter
    }

    /**
     * Помечает указанный неудаленный ранее чат как удаленный, присваивая [Chat.deleted] значение true, и помечает как
     * удаленные все сообщения данного чата.
     * @param <chatId> ID чата.
     * @throws <ChatNotFoundException> Когда указанного чата не существует или он уже был помечен как удаленный.
     */
    fun deleteChat(chatId: Int) {
        val markChatAsDeleted: (Int, MutableList<Chat>) -> Chat = searchChatById@{ id, listOfChats ->
            for ((i, chat) in listOfChats.withIndex()) {
                if (chat.id == id && !chat.deleted) {
                    listOfChats[i] = chat.copy(deleted = true)
                    return@searchChatById listOfChats[i]
                }
            }
            throw ChatNotFoundException()
        }
        val markMessagesAsDeleted: (MutableList<Message>) -> MutableList<Message> = {
            for ((i, message) in it.withIndex()) {
                it[i] = message.copy(deleted = true)
            }
            it
        }

        markMessagesAsDeleted(markChatAsDeleted(chatId, listOfChats).messages)
    }


    // FOR TESTS //

    fun reset() {
        listOfChats.removeAll(listOfChats)
    }

    fun getListOfChatsTest(): MutableList<Chat> {
        return listOfChats
    }

    fun getLastChatIdTest(): Int {
        return listOfChats.last().id
    }

    fun getChatStatusByIdTest(chatId: Int): Boolean {
        for (chat in listOfChats) {
            if (chat.id == chatId) {
                return chat.deleted
            }
        }
        throw ChatNotFoundException()
    }
}
