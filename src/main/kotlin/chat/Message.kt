package chat

data class Message(
    val id: Int = 0,
    val userId: Int = 0,
    val text: String,
    val date: Int,
    val unread: Boolean = true,
    val deleted: Boolean = false
) {
}