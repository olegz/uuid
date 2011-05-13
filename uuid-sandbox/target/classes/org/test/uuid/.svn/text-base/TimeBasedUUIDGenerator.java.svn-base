/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.test.uuid;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.UUID;

/**
 * Will generate time-based UUID (version 1 UUID).
 * This will allow Message ID to be unique but also contain an 
 * embedded timestamp which could be retrieved via UUID.timestamp()
 * 
 * @author Oleg Zhurakousky
 * @since 2.0
 */
class TimeBasedUUIDGenerator {
	//long processId = new SecureRandom(ManagementFactory.getRuntimeMXBean().getName().getBytes()).nextInt();
	public static final Object lock = new Object();
	
	private static boolean canNotDetermineMac = true;
	private static long lastTime;
	private static long clockSequence = 0;
	private static final long macAddress = getMac();
	private static final String binaryMacAddress = Long.toBinaryString(macAddress);
	
	/*
	 * s
	 */
	private static final long getMac(){
		long  macAddressAsLong = 0;
		try {
			InetAddress address = InetAddress.getLocalHost();
			NetworkInterface ni = NetworkInterface.getByInetAddress(address);
			if (ni != null) {
				byte[] mac = ni.getHardwareAddress();
				//Converts array of unsigned bytes to an long
				if (mac != null) {
					for (int i = 0; i < mac.length; i++) {					
						macAddressAsLong <<= 8;
						macAddressAsLong ^= (long)mac[i] & 0xFF;
					}
				}
			} 
			canNotDetermineMac = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("mac: " + Long.toBinaryString(macAddressAsLong));
		return macAddressAsLong;
	}

	/**
	 * Will generate unique time based UUID where the next UUID is 
	 * always greater then the previous.
	 * 
	 * @return
	 */
	public final static UUID generateId() {
		return generateIdFromTimestamp(System.currentTimeMillis());
	}
	/**
	 * 
	 * @param currentTimeMillis
	 * @return
	 */
	public final static UUID generateIdFromTimestamp(long currentTimeMillis){
		long time;
		long cSeq = 0;
		
		synchronized (lock) {
			if (currentTimeMillis > lastTime) {
				lastTime = currentTimeMillis;
				clockSequence = 0;
				//System.out.println("new time");
			} else  { 
				++clockSequence; 
			}
		}
		
	
		time = currentTimeMillis;
		
		// low Time
		time = currentTimeMillis << 32;
		
		// mid Time
		time |= ((currentTimeMillis & 0xFFFF00000000L) >> 16);

		// hi Time
		time |= 0x1000 | ((currentTimeMillis >> 48) & 0x0FFF); // version 1
		
		long clock_seq_hi_and_reserved = clockSequence;  
    	
    	clock_seq_hi_and_reserved <<=48;	
  
    	long cls = 0 | clock_seq_hi_and_reserved;
    	
		long lsb = cls | macAddress;
		if (canNotDetermineMac){
			System.out.println("UUID generation process was not able to determine your MAC address. Returning random UUID (non version 1 UUID)");
			return UUID.randomUUID();
		} else {
			System.out.println("MAC address for this node: " + binaryMacAddress);
			return new UUID(time, lsb);
		}
	}
}
