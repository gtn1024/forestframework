package io.forestframework.samples.realtimeauctions

import io.forestframework.core.http.routing.Get
import io.forestframework.core.http.routing.Intercept
import io.forestframework.core.http.routing.Patch
import io.forestframework.core.http.param.PathParam
import io.forestframework.core.http.param.RequestBody
import io.forestframework.core.http.routing.Route
import io.forestframework.core.http.socketjs.SocketJSBridge
import io.forestframework.core.Forest
import io.forestframework.core.ForestApplication
import io.forestframework.ext.core.HttpException
import io.forestframework.core.http.HttpStatusCode
import io.forestframework.core.http.result.JsonResponseBody
import io.vertx.core.eventbus.EventBus
import io.vertx.core.shareddata.LocalMap
import io.vertx.core.shareddata.SharedData
import io.vertx.ext.bridge.BridgeEventType
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.sockjs.BridgeEvent
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import java.math.BigDecimal
import java.util.Optional
import javax.inject.Inject
import javax.inject.Singleton

@ForestApplication
class RealtimeAuctions {
}

fun main() {
    Forest.run(RealtimeAuctions::class.java)
}

@Singleton
class EventBusHandler {
    @SocketJSBridge("/eventbus/*")
    fun bridgeEvent(event: BridgeEvent, socket: SockJSSocket) {
        if (event.type() == BridgeEventType.SOCKET_CREATED) {
            println("A socket was created")
        }

        event.complete(true)
    }

//    @SocketJS("")
//    suspend fun test(socket: SockJSSocket) {
//        socket.writeAwait("hello")
//    }
}

@Singleton
@Route("/api")
class AuctionHandler(private val repository: AuctionRepository, private val validator: AuctionValidator) {

    @Get("/auctions/:id")
    @JsonResponseBody(pretty = true)
    fun handleGetAuction(context: RoutingContext, @PathParam("id") auctionId: String): Auction {
        return repository.getById(auctionId).orElseThrow { HttpException(HttpStatusCode.NOT_FOUND) }
    }

    @Patch("/auctions/:id")
    fun handleChangeAuctionPrice(eventBus: EventBus,
                                 @PathParam("id") auctionId: String,
                                 @RequestBody body: Map<String, Any>,
                                 @RequestBody bodyString: String) {
        val auctionRequest = Auction(
            auctionId,
            BigDecimal(body["price"].toString())
        )
        if (validator.validate(auctionRequest)) {
            repository.save(auctionRequest)
            eventBus.publish("auction.$auctionId", bodyString)
        } else {
            throw HttpException(HttpStatusCode.UNPROCESSABLE_ENTITY)
        }
    }

    @Intercept("/auctions/:id")
    fun initAuctionInSharedData(@PathParam("id") auctionId: String) {
        val auction = repository.getById(auctionId)
        if (!auction.isPresent) {
            repository.save(Auction(auctionId))
        }
    }
}


@Singleton
class AuctionRepository @Inject constructor(private val sharedData: SharedData) {
    fun getById(auctionId: String): Optional<Auction> {
        val auctionSharedData = sharedData.getLocalMap<String, String>(auctionId)
        return Optional.of(auctionSharedData)
            .filter { m: LocalMap<String, String> -> !m.isEmpty() }
            .map { auction: LocalMap<String, String> -> convertToAuction(auction) }
    }

    fun save(auction: Auction) {
        val auctionSharedData: LocalMap<String, String> = sharedData.getLocalMap(auction.id)
        auctionSharedData["id"] = auction.id
        auctionSharedData["price"] = auction.price.toString()
    }

    private fun convertToAuction(auction: LocalMap<String, String>) = Auction(
        auction.getValue("id"),
        BigDecimal(auction["price"])
    )
}

data class Auction(val id: String, val price: BigDecimal) {
    constructor(auctionId: String) : this(auctionId, BigDecimal.ZERO)
}

class AuctionNotFoundException(auctionId: String) : RuntimeException("Auction not found: $auctionId")

@Singleton
class AuctionValidator @Inject constructor(private val repository: AuctionRepository) {
    fun validate(auction: Auction): Boolean {
        val auctionDatabase = repository.getById(auction.id)
            .orElseThrow { AuctionNotFoundException(auction.id) }
        return auctionDatabase.price < auction.price
    }
}
