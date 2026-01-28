

fun main() {
    processRecords("Charlie", "Aman", "Bob", "David")
}

fun processRecords(vararg names: String) {
    for (record in names) {
        execute(record) {
            if (record == "Bob") {
                return@processRecords
            }
            saveRecord(record)
        }
    }
}

inline fun execute(name: String, block: () -> Unit) {
    val startTime = System.currentTimeMillis()
    block()
    val endTime = System.currentTimeMillis()
    println("Execution time for $name: ${endTime - startTime} ms")
}

fun saveRecord(name: String) {
    println("Saving record for $name")
}

