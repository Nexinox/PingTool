package modell

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Response(val status: Status, val localDateTime: LocalDateTime, private val address: String){

    constructor(string: String) : this(Status.valueOf(string.substring(string.indexOf(" is ")+4, string.length-1)), LocalDateTime.parse(string.substring(0, 19), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")), string.substring(8, string.indexOf(" is ")-1))

    override fun toString(): String {
        val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
        val timeSting = localDateTime.format(dtf)
        return "$timeSting $address is $status "
    }
}