import { Client } from '@stomp/stompjs';


export class Environment{
    static rest_api        = "http://localhost:8085/api"
    static frontend_proxy  = "http://127.0.0.100:8090/proxy"
    static available_logos = ["c#", "c", "cpp", "css", "html", "java", "jpg", "js", "json","pdf", "png", "py","svg","txt","xml"]

    static getWebSocket = () => {
        return new Promise(resolve => {
            let webSocketConnection = new Client({
                brokerURL: "ws://127.0.0.100:8090/wbsocket",
                reconnectDelay: 10,
                heartbeatIncoming: 3000,
                heartbeatOutgoing: 3000,
                onConnect: () => {
                    console.log("Websocket connected!")
                    resolve(webSocketConnection)
                },
                onDisconnect: () => {
                    console.log("Websocket disconnected.")
                }
            });
            webSocketConnection.activate()
        })
    }
}