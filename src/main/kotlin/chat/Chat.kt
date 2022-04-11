package chat

data class Chat (
    val id: Int = 0,
    val messages: MutableList<Message> = ArrayList(),
    val date: Int,
    val deleted: Boolean = false
) {
}