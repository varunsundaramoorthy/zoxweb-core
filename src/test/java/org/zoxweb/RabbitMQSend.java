package org.zoxweb;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import org.zoxweb.shared.util.Const.Bool;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

public class RabbitMQSend {


	private  static String EXCHANGE = "";
	public static void main(String[] args) 
	{
		long ts = System.currentTimeMillis();
		try
		{
			int index = 0;
			boolean persistent = true;
		// TODO Auto-generated method stub
		 ConnectionFactory factory = new ConnectionFactory();
		 	
		 	factory.setHost(args[index++]);
		 	factory.setVirtualHost(args[index++]);
		 	EXCHANGE = args[index++];
		    factory.setUsername(args[index++]);
		    factory.setPassword(args[index++]);
		    int repeat = 1;
		    if (args.length > index)
		    {
		    	repeat = Integer.parseInt(args[index++]);
		    }
		    
		    if (args.length > index)
		    {
		    	persistent = Bool.lookupValue(args[index++]);
		    }
		    Connection connection = factory.newConnection();
		    Channel channel = connection.createChannel();
		    Map<String, Object> argsRM = new HashMap<String, Object>();
		    argsRM.put("x-max-priority", 10);
		    argsRM.put("x-max-length", 10000000);
		    //args.argsRM.put("x-delayed-type", "direct");
		   // channel.exchangeDeclare(EXCHANGE, "fanout", true, true, argsRM);
		  
		    channel.queueDeclare(EXCHANGE, true, false, false, argsRM);
		    //channel.queueDeclarePassive(QUEUE_NAME);
//		    SecureRandom sr =  SecureRandom.getInstanceStrong();
//		    BasicProperties bpAll[] = new BasicProperties[repeat];
//		    byte messages[][] = new byte[repeat][];
		    
		    SecureRandom sr = SecureRandom.getInstanceStrong();
		    for (int i = 0; i < repeat; i++)
		    {
		    	//MessageProperties.PERSISTENT_BASIC
		    	int priority =1  +sr.nextInt(9);
		    	String message = "[" + i +"]:" + priority;
		    	BasicProperties bp = new BasicProperties();
		    	// delivery mode 2 make messages persistent
		    	HashMap<String, Object> headers = new HashMap<String, Object>();
		    	//headers.put("x-delay", 10000);
		    	bp = bp.builder().priority(priority).messageId("" + i).deliveryMode(persistent ? 2 : 1).headers(headers).build();
		    	//bp = bp.builder().priority(priority).messageId("" + i).build();
		    	
		    	//messages[i] = message.getBytes();
		    	//System.out.println(" [x] Sent '" + message + "'");
		    	channel.basicPublish("", EXCHANGE, bp, message.getBytes());
		    
		    }
		  
		    
		    channel.close();
		    connection.close();
		    
		    ts = System.currentTimeMillis() - ts;
		    System.out.println("Total sent " + repeat + " it took " + ts + "  rate per sec " + ((float)repeat/(float)ts)*1000);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
//		System.out.println("Total sent " + repeat + " it took " + ts millis);
	}

}