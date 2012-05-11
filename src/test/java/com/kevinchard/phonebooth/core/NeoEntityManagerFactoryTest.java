package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.kevinchard.phonebooth.core.NeoEntityManagerFactory;

public class NeoEntityManagerFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public void testCreateEntityManagerWithNoGraphDatabaseService() {
		new NeoEntityManagerFactory(null, new ArrayList<Class<?>>(), new HashMap<String, Object>());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testCreateEntityManagerWithNoEntityClasses() {
		new NeoEntityManagerFactory(new ImpermanentGraphDatabase(), null, new HashMap<String, Object>());
	}
	
	@Test
	public void testCreateEntityManager() {
		assertNotNull(new NeoEntityManagerFactory(new ImpermanentGraphDatabase(), new ArrayList<Class<?>>(), new HashMap<String, Object>()));
	}
}
