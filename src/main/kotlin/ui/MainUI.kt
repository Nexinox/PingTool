package ui

import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import modell.Response
import modell.Status
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.net.InetAddress
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.system.exitProcess


class MainUI: Application() {
    override fun start(primaryStage: Stage) {
        var timer = Timer()
        var firstExecution = true
        var tempWriter = FileWriter("temp.txt")
        val fileWriter = FileWriter("result.txt")
        primaryStage.title = "Ping Tool"

        val addressLabel = Label("Address:")
        addressLabel.padding = Insets(5.0, 0.0, 0.0, 0.0)
        val addressInput = TextField()

        val output = TextArea()
        output.isEditable = false

        val startBtn = Button()
        startBtn.text = "Start"
        startBtn.onAction = EventHandler<ActionEvent> {
            tempWriter = FileWriter("temp.txt")
            timer = Timer()
            output.clear()
            if (!firstExecution){
                fileWriter.write(System.lineSeparator() + System.lineSeparator())
            }else{
                firstExecution = false
            }
            val address = addressInput.text
            val ip: InetAddress
            try {
                ip = InetAddress.getByName(address)

            } catch (e: Exception) {
                println("Address invalid")
                exitProcess(1)
            }
            fileWriter.write("Sending Ping Requests to $address IP: ${ip.hostAddress}" + System.lineSeparator())
            output.appendText("Sending Ping Requests to $address IP: ${ip.hostAddress}" + System.lineSeparator())
            output.appendText(System.lineSeparator())
            fileWriter.write(System.lineSeparator())
            timer.scheduleAtFixedRate(timerTask {

                try {
                    if (ip.isReachable(5000)) {
                        val instant = LocalDateTime.now()
                        val response = Response(Status.REACHABLE, instant, address)
                        output.appendText(response.toString() + System.lineSeparator())
                        tempWriter.write(response.toString() + System.lineSeparator())

                    } else {
                        val instant = LocalDateTime.now()
                        val response = Response(Status.UNREACHABLE, instant, address)
                        output.appendText(response.toString() + System.lineSeparator())
                        tempWriter.write(response.toString() + System.lineSeparator())
                    }
                } catch (e: Exception) {
                    println("Exception:" + e.message)
                }

            }, 0, 3000)
        }

        val stopBtn = Button()
        stopBtn.text = "Stop"
        stopBtn.onAction = EventHandler<ActionEvent> {
            tempWriter.close()
            timer.cancel()
            timer.purge()
            val inputStream: InputStream = File("temp.txt").inputStream()
            val lineList = mutableListOf<String>()
            var unreached = 0
            val dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it)} }
            lineList.forEach{
                fileWriter.write(it + System.lineSeparator())
            }
            output.appendText(System.lineSeparator())
            fileWriter.write(System.lineSeparator())

            inputStream.close()
            output.appendText("Unreachable Times:" + System.lineSeparator())
            fileWriter.write("Unreachable Times:" + System.lineSeparator())

            lineList.forEach{
                val response = Response(it)
                if (response.status == Status.UNREACHABLE){
                    unreached++
                    val timeSting = it.format(dtf)
                    output.appendText(timeSting + System.lineSeparator())
                    fileWriter.write(timeSting + System.lineSeparator())
                }
            }

            output.appendText(System.lineSeparator())
            fileWriter.write(System.lineSeparator())

            output.appendText("$unreached pings did not get through" + System.lineSeparator())
            fileWriter.write("$unreached pings did not get through" + System.lineSeparator())



        }

        val starter = HBox()
        starter.padding = Insets(15.0, 80.0, 15.0, 90.0)
        starter.spacing = 10.0

        starter.children.addAll(addressLabel, addressInput, startBtn, stopBtn)

        val mainWrapper = VBox()
        mainWrapper.padding = Insets(15.0, 12.0, 15.0, 12.0)
        mainWrapper.spacing = 10.0
        mainWrapper.children.addAll(starter, output)

        val root = StackPane()
        root.children.addAll(mainWrapper)
        primaryStage.scene = Scene(root, 600.0, 300.0)
        primaryStage.show()

        primaryStage.setOnCloseRequest {
            fileWriter.close()
            exitProcess(0)
        }
    }
}