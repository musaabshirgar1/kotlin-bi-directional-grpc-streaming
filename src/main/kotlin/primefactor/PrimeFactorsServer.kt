package primefactor

import io.grpc.Server
import io.grpc.ServerBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class PrimeFactorsServer(private val port: Int) {
    private val server: Server = ServerBuilder
        .forPort(port)
        .addService(PrimeFactorsService())
        .build()


    fun start() {
        server.start()
        println("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@PrimeFactorsServer.stop()
                println("*** server shut down")
            }
        )
    }

    private fun stop() {
        server.shutdown()
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    private class PrimeFactorsService : FactorsGrpcKt.FactorsCoroutineImplBase() {

        override fun primeFactors(requests: Flow<Request>): Flow<Response> = flow {
            requests.collect { number ->
                println("Received: ${number.num}")
                getPrimeFactors(number.num).forEach { prime ->
                    emit(
                        Response
                            .newBuilder()
                            .setResult(prime)
                            .build()
                    )
                }
            }
        }

        fun getPrimeFactors(number: Long): List<Long> {
            var n = number
            val setPrimeFactors: MutableSet<Long> = HashSet()
            var i: Long = 2
            while (i <= n) {
                if (n % i == 0L) {
                    setPrimeFactors.add(i)
                    n /= i
                    i--
                }
                i++
            }
            return setPrimeFactors.toList().sortedBy { it }
        }
    }

}

fun main() {
    val port = 50052
    val server = PrimeFactorsServer(port)
    server.start()
    server.blockUntilShutdown()
}