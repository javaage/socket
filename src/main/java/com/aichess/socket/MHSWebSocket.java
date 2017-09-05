package com.aichess.socket;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@ServerEndpoint("/websocket/{nodeId}")
@Component
public class MHSWebSocket {
	Logger logger = Logger.getLogger(MHSWebSocket.class);
	private static int onlineCount = 0;

	private static CopyOnWriteArraySet<MHSWebSocket> webSocketSetMachine = new CopyOnWriteArraySet<>();
	private static CopyOnWriteArraySet<MHSWebSocket> webSocketSetUser = new CopyOnWriteArraySet<>();
	private Session session;

	//0:user;1:machine
	@OnOpen
	public void onOpen(@PathParam("nodeId") int nodeId ,Session session) {
		logger.info("get an opent connection");
		this.session = session;
		if(0==nodeId){
			webSocketSetUser.add(this);
		}else{
			webSocketSetMachine.add(this);
		}
		addOnlineCount();
		logger.info("There is one new online. The total online now is "+ getOnlineCount());
	}

	@OnClose
	public void onClose(@PathParam("nodeId") int nodeId ) {
		if(0==nodeId){
			webSocketSetUser.remove(this);
		}else{
			webSocketSetMachine.remove(this);
		}
		subOnlineCount();
		logger.info("There is one get offline. The total online now is "+ getOnlineCount());
	}

	@OnMessage
	public void onMessage(@PathParam("nodeId") int nodeId,String message, Session session) throws IOException {
		logger.info("There is a message from client. The message: "+message);

		if(0==nodeId){//send to machine
			if ("HB".equals(message)) {
				for (MHSWebSocket item : webSocketSetUser) {
					item.sendMessage(message);
				}
			} else {
				for (MHSWebSocket item : webSocketSetMachine) {
					item.sendMessage(message);
				}
			}
			
		} else {//send to user
			if  ("HB".equals(message)) {
				for (MHSWebSocket item1 : webSocketSetMachine) {
					item1.sendMessage(message);
				}
			} else {
				for (MHSWebSocket item : webSocketSetUser) {
					item.sendMessage("ok");
				}
			}
			/*for (MHSWebSocket item : webSocketSetUser) {
				if  ("HB".equals(message)) {
					for (MHSWebSocket item1 : webSocketSetMachine) {
						item1.sendMessage(message);
					}
				} else {
					item.sendMessage("ok");
				}
				
			}*/
		}
	}

	public void sendMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}

	public static synchronized int getOnlineCount() {
		return MHSWebSocket.onlineCount;
	}

	public static synchronized void addOnlineCount() {
		MHSWebSocket.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		MHSWebSocket.onlineCount--;
	}
}
