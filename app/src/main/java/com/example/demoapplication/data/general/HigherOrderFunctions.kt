import java.lang.Exception
import kotlin.concurrent.thread


fun main() {
    processRecords("Charlie", "Aman", "Bob", "David")
    val name by Delegate("Vaibhav")
}

fun processRecords(vararg names: String) {
    for (record in names) {
        execute(record, block =  {
            if (record == "Bob") {
                return@execute
            }
            saveRecord(record)
        })
    }
}

inline fun execute(name: String, block: () -> Unit, noinline onError: (Exception) -> Unit = {}) {
    try {
        val startTime = System.currentTimeMillis()
        block()
        val endTime = System.currentTimeMillis()
        println("Execution time for $name: ${endTime - startTime} ms")
    } catch (ex: Exception) {
        val x = onError
        handleError(x)
    }
}

//so to pass function as paramter it need to be marked as noinline

fun handleError(error : (Exception) -> Unit) {
    val sampleException = Exception("Sample exception")
    error(sampleException)
}

fun saveRecord(name: String) {
    println("Saving record for $name")
}


class Delegate(var value: String) {
    operator fun getValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>): String {
        return value
    }

    operator fun setValue(thisRef: Any?, property: kotlin.reflect.KProperty<*>, newValue: String) {
        value = newValue
    }
}

