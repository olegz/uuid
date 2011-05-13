/*
 * Copyright 2002-2011 the original author or authors.
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
package org.olegz.uuid;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.olegz.uuid.TimeBasedUUIDGenerator;

import org.springframework.util.StopWatch;

/**
 * @author Oleg Zhurakousky
 *
 */
public class TimeBasedUUIDGeneratorTests {
	private long  macAddressAsLong = 0;

	@Test
	public void testGreaterThen(){
		UUID id = TimeBasedUUIDGenerator.generateId();
		for (int i = 0; i < 10; i++) {
			UUID newId = TimeBasedUUIDGenerator.generateId();
			// tests, that newly created UUID is always greater then the previous one.
			Assert.assertTrue(newId.compareTo(id) >= 0);
			id = newId;
		}
	}
	@Test
	public void testUniqueness(){
		long timestamp = System.currentTimeMillis();
		UUID id = TimeBasedUUIDGenerator.generateIdFromTimestamp(timestamp);
		for (int i = 0; i < 32000; i++) { //32767 max
			UUID newId = TimeBasedUUIDGenerator.generateIdFromTimestamp(timestamp);
			// tests, that newly created UUID is always greater then the previous one.
			Assert.assertTrue(newId.compareTo(id) == 1);
			Assert.assertEquals(newId.timestamp(), id.timestamp());
			id = newId;
		}
	}
	@Test
	public void testStructure(){
		long timestamp = System.currentTimeMillis();
		UUID id = TimeBasedUUIDGenerator.generateIdFromTimestamp(timestamp);
		for (int i = 0; i < 30000; i++) {
			UUID newId = TimeBasedUUIDGenerator.generateIdFromTimestamp(timestamp);
			id = newId;
		}
		Assert.assertEquals(timestamp, id.timestamp());
		Assert.assertEquals(macAddressAsLong, id.node());
	}
	@Test
	public void performanceTestSynch(){
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < 100000; i++) {
			TimeBasedUUIDGenerator.generateId();
		}
		stopWatch.stop();
		System.out.println("Generated 100000 UUID (sync) via TimeBasedUUIDGenerator.generateId(): in " + stopWatch.getTotalTimeSeconds() + " seconds");
		
		stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < 100000; i++) {
			UUID.randomUUID();
		}
		stopWatch.stop();
		System.out.println("Generated 100000 UUID  (sync) via UUID.randomUUID(): in " + stopWatch.getTotalTimeSeconds() + " seconds");
	}
	@Test
	public void performanceTestAsynch() throws Exception {
		ExecutorService executor = Executors.newFixedThreadPool(100);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < 100000; i++) {
			executor.execute(new Runnable() {
				public void run() {
					TimeBasedUUIDGenerator.generateId();
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		stopWatch.stop();
		System.out.println("Generated 100000 UUID (async) via TimeBasedUUIDGenerator.generateId(): in " + stopWatch.getTotalTimeSeconds() + " seconds");
		
		executor = Executors.newFixedThreadPool(100);
		stopWatch = new StopWatch();
		stopWatch.start();
		for (int i = 0; i < 100000; i++) {
			executor.execute(new Runnable() {
				public void run() {
					UUID.randomUUID();
				}
			});
		}
		executor.shutdown();
		executor.awaitTermination(10, TimeUnit.SECONDS);
		stopWatch.stop();
		System.out.println("Generated 100000 UUID (async) via UUID.randomUUID(): in " + stopWatch.getTotalTimeSeconds() + " seconds");
	}
	@Before
	public void prepare(){
		
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
