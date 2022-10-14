package utilities

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Coroutines {

    suspend fun main(work: suspend (() -> Unit)) {
        coroutineScope {
            launch {
                work()
            }
        }
    }

}