/*
COURSE : COMP4300
NAME   : QI WEN
ST.ID. : 7724931
REMARK : Assignment #2 Question #2
         implementation of mini-DNS client. 
*/

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class A2Q2_MiniDNSClient {

	public static final int HEADERSIZE = 12;
	public static final int BUFFERSIZE = 1024;

    public static void main(String[] args) throws IOException {
    	
		System.out.print("domain:");
		Scanner scanner = new Scanner(System.in);
    	String myDomain = "";
    	
    	if(scanner.hasNext()) 
    		myDomain = scanner.next();
    	else {
    		System.out.println("Invaild Input!");
    		System.exit(1);
    	}
    	
		scanner.close();
		
        InetAddress server = InetAddress.getByName("8.8.8.8");

        ByteArrayOutputStream baStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(baStream);
        
		/*************SEND HEADER*************/
		
        outputStream.writeShort(0x4300);// arbitrary ID
        outputStream.writeShort(0x0100);// flags, recursive RD = 1
        outputStream.writeShort(0x0001);// 1 question
        outputStream.writeShort(0x0000);// 0 answer
        outputStream.writeShort(0x0000);// 0 authority records
        outputStream.writeShort(0x0000);// 0 additional records
		
		/*************SEND HEADER*************/
		
		/*************SEND QUESTION*************/
		
        String[] domainTemp = myDomain.split("\\.");

        for (int i = 0; i<domainTemp.length; i++) {
        	//length
        	outputStream.writeByte(domainTemp[i].length());
        	
            byte[] byteTemp = domainTemp[i].getBytes(StandardCharsets.US_ASCII);
            outputStream.write(byteTemp);
        }
		
        outputStream.writeByte(0x00); //End of domain name
        outputStream.writeShort(0x0001); //QTYPE
        outputStream.writeShort(0x0001); //QCLASS
		
		/*************SEND QUESTION*************/

		DatagramSocket ds = new DatagramSocket();
        
		byte[] sendBuf = baStream.toByteArray();
		DatagramPacket dp1 = new DatagramPacket(sendBuf, sendBuf.length, server, 53);
		
		byte[] recvBuf = new byte[BUFFERSIZE];
        DatagramPacket dp2 = new DatagramPacket(recvBuf, recvBuf.length);
		
		ds.send(dp1);
        ds.receive(dp2);

		int recvivedBytes = dp2.getLength();
        System.out.println("\nthe length of the reply: " + recvivedBytes + " bytes");
		
/*TEST:
        for (int i = 0; i < dp2.getLength(); i++) 
            System.out.print("0x" + String.format("%x", recvBuf[i]) + " " );
*/
		
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(recvBuf));
		
		/*************RECV HEADER*************/
		
        inputStream.readShort();//reading ID
		String flagStr = Integer.toBinaryString(0xFFFF & inputStream.readShort());//reading flags
		
		if(flagStr.charAt(8) == '1')
			System.out.println("recursion was available at the server!");
		else
			System.out.println("recursion was not available at the server!");
		
		String rcode = flagStr.substring(HEADERSIZE,flagStr.length());
		
		System.out.println("the response code of the query : " + rcode);
		
		if(rcode.compareTo("0000") != 0){
			System.out.println("Error: DNS Server is not able to solve this domain!");
			System.exit(1);
		}
		
        inputStream.readShort();//reading # of questions
        System.out.println("the number of answers : " + String.format("%d", (inputStream.readShort() & 0xFFFF)));//reading # of answers
		inputStream.readInt();//reading authority records and additional records
		
		/*************RECV HEADER*************/

		/*************RECV QUESTION*************/
		
		int questionSize = sendBuf.length - HEADERSIZE;
		
		for(int i = 0; i < questionSize; i++)//reading questions
			inputStream.readByte();
			
		/*************RECV QUESTION*************/
		
		/*************RECV ANSWER*************/
		
		int adressCount = 0;
		int bytesLeft = recvivedBytes - (BUFFERSIZE - inputStream.available());
		
		System.out.println("IP addresses list:");
		
		//System.out.println(bytesLeft);
		while(bytesLeft > 0){
			
			int start = inputStream.available();
						
			inputStream.readInt();//reading name and type			
			inputStream.readShort();//reading class			
			inputStream.readInt();//reading TTL
			short ipLen = inputStream.readShort();//length of ip address
			
			//System.out.println(ipLen);

			if(ipLen > 0){
				for (int i = 0; i < ipLen; i++ ) {//reading IP
					System.out.print(String.format("%d", (inputStream.readByte() & 0xFF)));
					if( i != ipLen - 1)
						System.out.print(".");
				}
			}
			
			int end = inputStream.available();
			
			bytesLeft -= start - end;			
			adressCount++;
			System.out.println();
		}
		
		System.out.println("the number of IP addresses found : " + adressCount);
		
		/*************RECV ANSWER*************/
    }
}