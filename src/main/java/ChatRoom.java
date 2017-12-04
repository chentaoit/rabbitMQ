import com.rabbitmq.client.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class ChatRoom extends Thread {

    public static final String HOST = "112.74.173.155";
    public static final int PORT = 5672;
    public static final String USERNAME = "ct";
    public static final String PASSWORD = "123456";
    public static final String QUEUE_NAME = "CHAT_ROOM";
    public static final String EXCHANGE_NAME = "CHAT_ROOM_EXCHANGE";

    public static void main(String[] args) throws IOException, InterruptedException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("请输入您的昵称：");
        String myName = scanner.nextLine();
        System.out.println(myName+"，欢迎加入群聊（按'q'退出）");
        sendMsg(myName+"加入群聊");

        /**
         * 开启线程接收队列消息
         */
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.start();

//        recvMsg();

        String msg = "";
        while (true)
        {
            msg = scanner.nextLine();
            if ("q".equals(msg))
            {
                System.out.println("您已经退出群聊");
                sendMsg(myName+"已经退出群聊");
                System.exit(0);
            }
            DateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            String curTime = sdf.format(new Date());
            sendMsg(myName+" "+curTime+"\n"+"    "+msg);
        }


    }

    public static void sendMsg(String msg) throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);
        factory.setUsername(USERNAME);
        factory.setPassword(PASSWORD);
        factory.setPort(PORT);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicPublish(EXCHANGE_NAME, "", null, msg.getBytes());
        channel.close();
        connection.close();
    }

    @Override
    public void run() {
        try {
            recvMsg();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void recvMsg() throws IOException, InterruptedException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(ChatRoom.USERNAME);
        factory.setPassword(ChatRoom.PASSWORD);
        factory.setHost(ChatRoom.HOST);
        factory.setPort(ChatRoom.PORT);
        factory.setVirtualHost("/");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(ChatRoom.EXCHANGE_NAME, "fanout");
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, ChatRoom.EXCHANGE_NAME, "");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(message);
            }
        };
        channel.basicConsume(queueName, true, consumer);
    }
}
