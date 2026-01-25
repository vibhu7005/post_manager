package com.example.demoapplication.data.general

import kotlin.properties.Delegates.observable

interface Printer {
    fun print()
}

class ColorPrinter : Printer {
    override fun print() {
        println("Printing in color")
    }
}

class BlackAndWhitePrinter : Printer {
    override fun print() {
        println("Printing in black and white")
    }
}

class PrintManager(val printer : Printer) : Printer by printer {
    fun doPrint() {
        printer.print()
    }
}

fun main() {
    val settings by lazy {
        "default settings m45"
    }

    var result by observable(0) { property, old, new ->
        println("Property '${property.name}' changed from $old to $new")
    }

    result = 10

    result = 23

    result = 45

}