package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import com.kevinchard.phonebooth.Action;
import com.kevinchard.phonebooth.IllegalRelationshipException;
import com.kevinchard.phonebooth.ManyToOne;
import com.kevinchard.phonebooth.OneToOne;
import com.kevinchard.phonebooth.core.EntityProxy;

public class EntityProxyOneToOneTest extends EntityProxyTestBase {
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRelationshipGetterWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.getWrongAction();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRelationshipSetterWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		a.setWrongAction(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRelationshipGetterWrongNumberOfArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		a.getWrongNumberOfArgs(b);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testRelationshipSetterWrongNumberOfArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		a.setWrongNumberOfArgs(b, b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testSetOneToOneRelationshipWithWrongCardinality() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.setOneToOneWithWrongCardinality(b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testSetOneToOneRelationshipWithWrongDirection() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.setOneToOneWithWrongDirection(b);
	}
	
	@Test 
	public void testSetGetOneToOneRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertNull(a.getOneToOne());
		assertNull(b.getOneToOne());
		
		a.setOneToOne(b);
		
		assertEquals(b, a.getOneToOne());
		assertEquals(a, b.getOneToOne());
	}
	
	@Test 
	public void testSetGetOneToOneRelationshipWithOverwrite() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertNull(a.getOneToOne());
		assertNull(b1.getOneToOne());
		assertNull(b2.getOneToOne());
		
		a.setOneToOne(b1);
		assertEquals(b1, a.getOneToOne());
		assertNull(b2.getOneToOne());
		assertEquals(a, b1.getOneToOne());
		
		b2.setOneToOne(a);
		assertNull(b1.getOneToOne());
		assertEquals(b2, a.getOneToOne());
		assertEquals(a, b2.getOneToOne());
	}
	
	@Test 
	public void testDeleteOneToOneRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertNull(a.getOneToOne());
		assertNull(b.getOneToOne());
		
		a.setOneToOne(b);
		
		assertEquals(b, a.getOneToOne());
		assertEquals(a, b.getOneToOne());
		
		a.setOneToOne(null);
		
		assertNull(a.getOneToOne());
		assertNull(b.getOneToOne());
	}
	
	@Test 
	public void testSetGetReflexiveRelationshipWithSelf() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		assertNull(a.getReflexive());
		a.setReflexive(a);
		assertEquals(a, a.getReflexive());
	}
	
	@Test 
	public void testSetGetReflexiveRelationshipToAnother() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		assertNull(a1.getReflexive());
		assertNull(a2.getReflexive());
		
		a1.setReflexive(a2);
		
		assertEquals(a1, a2.getReflexive());
		assertEquals(a2, a1.getReflexive());
	}
	
	private interface A {
		
		@OneToOne(name = "GETTER_WRONG_NUMBER_ARGS", direction = Direction.OUTGOING, action = Action.READ)
		void getWrongNumberOfArgs(B one);
		
		@OneToOne(name = "SETTER_WRONG_NUMBER_ARGS", direction = Direction.OUTGOING, action = Action.WRITE)
		void setWrongNumberOfArgs(B one, B two);
		
		@OneToOne(name = "GETTER_WRONG_ACTION", direction = Direction.OUTGOING, action = Action.WRITE)
		void getWrongAction();
		
		@OneToOne(name = "SETTER_WRONG_ACTION", direction = Direction.OUTGOING, action = Action.READ)
		void setWrongAction(B b);
		
		@OneToOne(name = "ONE_TO_ONE_WRONG_CARDINALITY", direction = Direction.OUTGOING, action = Action.WRITE)
		void setOneToOneWithWrongCardinality(B b);
		
		@OneToOne(name = "ONE_TO_ONE_WRONG_DIRECTION", direction = Direction.OUTGOING, action = Action.WRITE)
		void setOneToOneWithWrongDirection(B b);
		
		@OneToOne(name = "ONE_TO_ONE", direction = Direction.OUTGOING, action = Action.READ)
		B getOneToOne();
		
		@OneToOne(name = "ONE_TO_ONE",  direction = Direction.OUTGOING, action = Action.WRITE)
		void setOneToOne(B b);
		
		@OneToOne(name = "REFLEXIVE", direction = Direction.BOTH, action = Action.READ)
		A getReflexive();		
		
		@OneToOne(name = "REFLEXIVE", direction = Direction.BOTH, action = Action.WRITE)
		void setReflexive(A a);
		
	}
	
	private interface B {
		
		@ManyToOne(name = "ONE_TO_ONE_WRONG_CARDINALITY", direction = Direction.INCOMING, action = Action.WRITE)
		void setOneToOneWithWrongCardinality(A a);
		
		@OneToOne(name = "ONE_TO_ONE_WRONG_DIRECTION", direction = Direction.OUTGOING, action = Action.WRITE)
		void setOneToOneWithWrongDirection(A a);
		
		@OneToOne(name = "ONE_TO_ONE", direction = Direction.INCOMING, action = Action.READ)
		A getOneToOne();
		
		@OneToOne(name = "ONE_TO_ONE", direction = Direction.INCOMING, action = Action.WRITE)
		void setOneToOne(A a);
		
	}
}
