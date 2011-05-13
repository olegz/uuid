package org.olegz.uuid;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

public class BitwiseTest {
	public static final Object lock = new Object();
	private static long clockSequence;
	private static long lastTime;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long currentTime = System.currentTimeMillis();
		long nanos = System.nanoTime() | new SecureRandom(ManagementFactory.getRuntimeMXBean().getName().getBytes()).nextLong();
		nanos = 0;
		// msb
		long time_low = (currentTime & 0xFFFFFFFFL);
		time_low <<= 32;
	
	    long time_mid = (currentTime & 0xFFFFFFFF00000000L) >> 16;
		long time_hi_and_version = ((currentTime >> 48) & 0x0FFF);
	    time_hi_and_version |= (1 << 12);
	    long msb = time_low | time_mid | time_hi_and_version;
		

		long variant = 2;
		variant <<= 62;
		//System.out.println(Long.toBinaryString(variant));
		
		long clockSequence = nanos & 0x000000000000FFFFL;
		clockSequence <<= 48;
		//System.out.println(Long.toBinaryString(clockSequence));
		
		//System.out.println(Long.toBinaryString(variant | clockSequence));
		
		long mac = getNode();
		//System.out.println(Long.toBinaryString(mac));
		long lsb = variant | clockSequence | mac;
		//System.out.println(Long.toBinaryString(lsb));
		
		UUID id = new UUID(msb, lsb);
		for (int i = 1; i < 100000; i++) {
			clockSequence += i;
			clockSequence &= 0x000000000000FFFFL;
			clockSequence <<= 48;
			//System.out.println(Long.toBinaryString(clockSequence));
			lsb = variant | clockSequence | mac;
			//System.out.println(Long.toBinaryString(lsb));
			//sSystem.out.println(id.clockSequence());
			UUID idA = new UUID(msb, lsb);
			if (idA.compareTo(id) < 1){
				throw new RuntimeException(idA.clockSequence()+ " + " + i);
			}
			//System.out.println(idA.clockSequence());
			id = idA;
		}

		System.out.println(id);
		System.out.println(id.variant());
		System.out.println(id.version());
		System.out.println(new Date(id.timestamp()));
		System.out.println(Long.toBinaryString(id.node()));
	}
	
	public static long getSecond(int pid, long currentTime){
		synchronized (lock) {
			if (currentTime > lastTime){
				lastTime = currentTime;
				clockSequence = 0;
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			} else {
				++clockSequence;
			}
				
			return (pid|clockSequence) << 48;
		}
	}
	
	public static long getNode(){
		long macAddressAsLong = 0;
		try {
			int processClIdentity = new SecureRandom(ManagementFactory.getRuntimeMXBean().getName().getBytes()).nextInt();
//			macAddressAsLong <<= 8;
//			macAddressAsLong ^= (long)processClIdentity & 0xFF;
//			System.out.println("id: " + Long.toBinaryString(macAddressAsLong));
			
			InetAddress address = InetAddress.getLocalHost();
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			if (ni != null) {
				byte[] mac = ni.getHardwareAddress();
				
				//Converts array of unsigned bytes to an long
				if (mac != null) {
					for (int i = 0; i < mac.length; i++) {					
						macAddressAsLong <<= 8;
						macAddressAsLong ^= (long)mac[i] & 0xFF;
						//System.out.println(Long.toBinaryString(macAddressAsLong));
					}
				}
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return macAddressAsLong ;
	}

}
