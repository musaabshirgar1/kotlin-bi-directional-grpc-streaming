package primefactor

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import primefactor.FactorsGrpcKt.FactorsCoroutineStub
import java.io.Closeable
import java.util.*
import java.util.concurrent.TimeUnit

class PrimeFactorsClient(private val channel: ManagedChannel) : Closeable {
    private val stub: FactorsCoroutineStub = FactorsCoroutineStub(channel)

    suspend fun primeFactors(request: Flow<Request>) {
        stub.primeFactors(request).collect { prime ->
            println("Prime Factor: ${prime.result}")
        }
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

}

suspend fun gen(): Flow<Request> = flow {
    val read = Scanner(System.`in`)
    while (true) {
        println("Enter a number:")
        val num = read.nextLong()
        emit(Request.newBuilder().setNum(num).build())
        delay(2000)
    }
}


suspend fun main() {
    val port = 50052
    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()
    val client = PrimeFactorsClient(channel)
    client.primeFactors(gen())
}