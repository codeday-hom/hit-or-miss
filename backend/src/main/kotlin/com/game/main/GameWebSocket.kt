package com.game.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.game.repository.GameRepository
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson
import org.http4k.lens.Path
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import java.util.concurrent.ConcurrentHashMap

class GameWebSocket {
    private val wsConnections = ConcurrentHashMap<String, MutableList<Websocket>>()
    private val allCategories = listOf("Sports", "Music", "Science", "Art", "History")

    fun gameWsHandler(): (Request) -> WsResponse {
        return { req: Request ->
            WsResponse { ws: Websocket ->
                val gameId = Path.of("gameId")(req)
                wsConnections.getOrPut(gameId) { mutableListOf() }.add(ws)

                ws.onMessage {
                    println("Received a message: ${it.bodyString()}")
                    val userIds = GameRepository.getGame(gameId)!!.userIds
                    println("Sending user IDs: $userIds")
                    sendWsMessage(ws, "userJoined", userIds)

                    val incomingData = ObjectMapper().readValue(it.bodyString(), Map::class.java)
                    val messageType = incomingData["type"] as? String

                    when (messageType) {
                        "requestCard" -> {
                            val cardToShow = fetchNextCardForUser()
                            showCardToPlayer(ws, cardToShow)
                        }
                        "categorySelected" -> {
                            val selectedCategory = incomingData["data"] as? String
                            if (selectedCategory != null) {
                                announceCategoryChosen(gameId, selectedCategory)
                            }
                            var currentUserIndex = (GameRepository.getGame(gameId)!!.currentPlayerIndex + 1) % userIds.size
                            var currentPlayer = userIds[currentUserIndex]
                            announceCurrentPicker(gameId, currentPlayer)
                        }
//                        else -> {
//                            println("Received a message: ${it.bodyString()}")
//                            val userIds = GameRepository.getGame(gameId)!!.userIds
//                            println("Sending user IDs: $userIds")
//                            sendWsMessage(ws, "userJoined", userIds)
//                        }
                    }
                }

                ws.onClose {
                    println("$gameId is closing")
                    wsConnections[gameId]?.remove(ws)
                }
            }
        }
    }

    fun sendWsMessage(ws: Websocket, type: String, data: Any) {
        val message = mapOf("type" to type, "data" to data)
        val mapper = ObjectMapper()
        val messageJson = mapper.writeValueAsString(message)
        println("Sending a message: $message")
        ws.send(WsMessage(messageJson))
    }

    fun sendUserJoinedMessages(gameId: String, userIds: List<String>) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, "userJoined", userIds)
        }
    }

    fun showCardToPlayer(ws: Websocket, card: String) {
        sendWsMessage(ws, "cardShown", card)
    }

    fun announceCategoryChosen(gameId: String, category: String) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, "categoryChosen", category)
        }
    }

//    fun startNewRound(gameId: String, pickerUserId: String) {
//        wsConnections[gameId]?.forEach { ws ->
//            sendWsMessage(ws, "roundStart", pickerUserId)
//        }
//    }

    fun announceCurrentPicker(gameId: String, pickerUserId: String) {
        wsConnections[gameId]?.forEach { ws ->
            sendWsMessage(ws, "currentPiker", pickerUserId)
        }
    }

    fun handleCategorySelection(req: Request, wsHandler: GameWebSocket): Response {
        val gameId = Path.of("gameId")(req)
        val userId = req.header("user_id") ?: return Response(NOT_FOUND).body("User ID not found.")

        val selectedCategory = req.bodyString()
        val chosenCard: String

        if (selectedCategory.isNotEmpty() && allCategories.contains(selectedCategory)) {
            chosenCard = selectedCategory
        } else {
            chosenCard = allCategories.shuffled().first()
        }

        wsHandler.announceCategoryChosen(gameId, chosenCard)
        return Response(OK).body(Jackson.asInputStream(mapOf("chosenCategory" to chosenCard)))
    }

    fun fetchNextCardForUser(): String {
        return allCategories.shuffled().first()
    }





}