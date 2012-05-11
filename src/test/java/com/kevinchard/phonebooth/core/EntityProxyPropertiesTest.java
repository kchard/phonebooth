package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kevinchard.phonebooth.Action;
import com.kevinchard.phonebooth.Property;
import com.kevinchard.phonebooth.core.EntityProxy;

public class EntityProxyPropertiesTest extends EntityProxyTestBase{
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetIllegalType() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.setIllegalType(new Object());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertyGetterWithWrite() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.getStringWithWrite();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertySetterWithRead() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.setStringWithRead("STRING");
	}
	
	@Test(expected = IllegalStateException.class)
	public void testPropertySetterWrongNumberArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.setStringWithWrongNumberOfArgs("", "");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testPropertyGetterWrongNumberArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.getStringWithWrongNumberOfArgs("");
	}
	
	@Test
	public void setSetGetProperty() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertNull(a.getString());
		a.setString("STRING");
		assertEquals("STRING", a.getString());
	}
	
	private interface A {
		
		@Property(name = "ILLEGAL", action = Action.WRITE)
		void setIllegalType(Object o);

		@Property(name = "PROPERTY_GETTER_WRONG_ACTION", action = Action.WRITE)
		String getStringWithWrite();
		
		@Property(name = "PROPERTY_SETTER_WRONG_ACTION", action = Action.READ)
		String setStringWithRead(String string);
		
		@Property(name = "PROPERTY_GETTER_WRONG_NUMBER_ARGS", action = Action.READ)
		String getStringWithWrongNumberOfArgs(String one);
		
		@Property(name = "PROPERTY_SETTER_WRONG_NUMBER_ARGS", action = Action.WRITE)
		String setStringWithWrongNumberOfArgs(String one, String two);
		
		@Property(name = "STRING", action = Action.READ)
		String getString();
		
		@Property(name = "STRING", action = Action.WRITE)
		String setString(String string);
	}
}
