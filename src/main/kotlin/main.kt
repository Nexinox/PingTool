import javafx.application.Application
import modell.Response
import modell.Status
import ui.MainUI
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess

fun main(args: Array<String>){
    var noUiModeStarted = false
    args.forEach {
        if (!noUiModeStarted){
            noUiMode(it)
            noUiModeStarted = true
        }
    }
    if (!noUiModeStarted){
        Application.launch(MainUI::class.java, *args)
    }
}

fun noUiMode(address: String){
    val timer = Timer()
    val tempWriter = FileWriter("temp.txt")
    val fileWriter = FileWriter("result.txt")
    val ip: InetAddress
    try {
        ip = InetAddress.getByName(address)
    } catch (e: Exception){
        println("Address invalid")
        exitProcess(1)
    }
    println("Sending Ping Requests to $address IP: ${ip.hostAddress}")
    fileWriter.write("Sending Ping Requests to $address IP: ${ip.hostAddress}" + System.lineSeparator())
    println()
    fileWriter.write(System.lineSeparator())
    timer.scheduleAtFixedRate(timerTask {

        try {
            if (ip.isReachable(5000)) {
                val instant = LocalDateTime.now()
                val response = Response(Status.REACHABLE, instant, address)
                tempWriter.write(response.toString() + System.lineSeparator())
                println(response.toString())
            } else {
                val instant = LocalDateTime.now()
                val response = Response(Status.UNREACHABLE, instant, address)
                tempWriter.write(response.toString() + System.lineSeparator())
                println(response.toString())
            }
        } catch (e: Exception) {
            println("Exception:" + e.message)
        }
    }, 0, 3000)
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            timer.cancel()
            timer.purge()
            tempWriter.close()
            val inputStream: InputStream = File("temp.txt").inputStream()
            val lineList = mutableListOf<String>()
            var unreached = 0
            val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }

            lineList.forEach{
                fileWriter.write(it + System.lineSeparator())
            }

            println()
            fileWriter.write(System.lineSeparator())

            println("Unreachable Times:")
            fileWriter.write("Unreachable Times:" + System.lineSeparator())
            println()
            fileWriter.write(System.lineSeparator())
            lineList.forEach{
                val response = Response(it)
                if (response.status == Status.UNREACHABLE){
                    unreached++
                    val timeSting = it.format(dtf)
                    print(timeSting + System.lineSeparator())
                    fileWriter.write(timeSting + System.lineSeparator())
                }
            }
            inputStream.close()
            println()
            fileWriter.write(System.lineSeparator())
            println("$unreached pings did not get through")
            fileWriter.write("$unreached pings did not get through" + System.lineSeparator())
            fileWriter.close()
            Runtime.getRuntime().halt(0)
        }
    })
}
