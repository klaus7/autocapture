package com.allpiper.autocapture

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import javax.imageio.ImageIO
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

public class App {
    val minutesFormat: DateFormat = SimpleDateFormat("mm")

    val secInMs     : Long = 1000;
    val minuteInMs  : Long = secInMs * 60;
    val hourInMs    : Long = minuteInMs * 60;

    @Parameter(names=arrayOf("--period", "-p"), description = "Period in minutes.")
    var periodInMinutes: Long = 15
    var period: Long = periodInMinutes * minuteInMs

    @Parameter(names=arrayOf("--path", "-o"))
    var outPath: String = "";

    @Parameter(names=arrayOf("--wait-for-minute", "-w"), description = "Wait until minute X reached. -1 to deactivate.")
    var waitUntilMinuteX: Int = -1;

    @Parameter(names=arrayOf("--file-format", "-f"))
    var fileFormatStr = "yyyy-MM-dd_HH_mm_ss" // "yyyy-MM-dd--HH-mm-ss"
    var fileFormat: DateFormat = SimpleDateFormat(fileFormatStr)

    @Parameter(names=arrayOf("--prefix"))
    var prefix = ""

    @Parameter(names = arrayOf("--help"), help = true)
    var help = false

    fun init() {
        if (outPath.isNotBlank() && !outPath.endsWith(File.pathSeparatorChar)) {
            outPath = File.pathSeparatorChar + outPath
        }
        fileFormat = SimpleDateFormat(fileFormatStr)
        period = periodInMinutes * minuteInMs
        if (waitUntilMinuteX >= 0) waitForFullHour()
        val takeScreenshotTask = timerTask { takeScreenshot() }
        Timer().schedule(takeScreenshotTask, 3000, period)
    }

    fun waitForFullHour() {
        val minutesInHour = Integer.valueOf(minutesFormat.format(Date()))
        var waitForMinutes = (waitUntilMinuteX - minutesInHour) % 60
        if (waitForMinutes < 0) waitForMinutes += 60
        println("Wait for $waitForMinutes minutes, before taking first screenshot.")
        Thread.sleep(waitForMinutes * minuteInMs)
    }

    fun takeScreenshot() {
        val image = Robot().createScreenCapture(Rectangle(Toolkit.getDefaultToolkit().screenSize))
        val dateStr = fileFormat.format(Date())
        val filename = outPath + prefix + "$dateStr.png"
        println("Save screenshot to: $filename")
        ImageIO.write(image, "png", File(filename))
    }

    companion object {
        @JvmStatic public fun main(args: Array<String>) {
            val app = App()
            val jCommander = JCommander(app, *args)
            if (app.help) {
                jCommander.usage()
            } else {
                app.init()
            }
        }
    }
}
