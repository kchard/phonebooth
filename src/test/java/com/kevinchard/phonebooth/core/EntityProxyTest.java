package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kevinchard.phonebooth.Id;
import com.kevinchard.phonebooth.core.EntityProxy;

public class EntityProxyTest extends EntityProxyTestBase {
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	@Test
	public void testCreate() {
		assertNotNull((A) EntityProxy.createProxy(createEntityNode(A.class), A.class));
	}
	
	@Test
	public void testNotEquals() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertFalse(a1.equals(a2));
	}
	
	@Test
	public void testEquals() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertTrue(a1.equals(a1));
	}
	
	@Test
	public void testNotHashCode() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertFalse(a1.hashCode() == a2.hashCode());
	}
	
	@Test
	public void testHashCode() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertTrue(a1.hashCode() == a1.hashCode());
	}
	
	@Test
	public void testGetId() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertNotNull(a1.getId());
	}
	
	public interface A {
		@Id Long getId();
	}
}
