package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import com.kevinchard.phonebooth.CollectionAction;
import com.kevinchard.phonebooth.IllegalRelationshipException;
import com.kevinchard.phonebooth.ManyToMany;
import com.kevinchard.phonebooth.OneToMany;
import com.kevinchard.phonebooth.core.EntityProxy;

public class EntityProxyManyToManyTest extends EntityProxyTestBase {

	@Before
	public void setUp() {
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetManyToManyRelationshipWithWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.getManyToManyWrongAction();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddManyToManyRelationshipWithWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.addManyToManyWrongAction(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveManyToManyRelationshipWithWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.removeManyToManyWrongAction(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetManyToManyRelationshipWithWrongNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.getManyToManyWrongNumArgs(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAddManyToManyRelationshipWithWrongNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.addManyToManyWrongNumArgs();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testRemoveManyToManyRelationshipWithWrongNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.removeManyToManyWrongNumArgs();
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testSetManyToManyRelationshipWithWrongCardinality() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.addManyToManyWrongCardinality(b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testSetManyToManyRelationshipWithWrongDirection() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.addManyToManyWrongDirection(b);
	}
	
	@Test
	public void testGetSetManyToManyRelationship() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a1.getManyToManyB().size());
		assertEquals(0, a2.getManyToManyB().size());
		assertEquals(0, b1.getManyToManyA().size());
		assertEquals(0, b2.getManyToManyA().size());
		
		a1.addManyToManyB(b1);
		a1.addManyToManyB(b2);
		b1.addManyToManyA(a2);
		b2.addManyToManyA(a2);
		
		assertEquals(2, a1.getManyToManyB().size());
		assertEquals(2, a2.getManyToManyB().size());
		assertEquals(2, b1.getManyToManyA().size());
		assertEquals(2, b2.getManyToManyA().size());
	}

	@Test
	public void testGetSetManyToManyRelationshipTwice() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.getManyToManyB().size());
		assertEquals(0, b.getManyToManyA().size());
		
		a.addManyToManyB(b);
		a.addManyToManyB(b);
		b.addManyToManyA(a);
		b.addManyToManyA(a);
		
		assertEquals(1, a.getManyToManyB().size());
		assertEquals(1, b.getManyToManyA().size());
	}
	
	@Test
	public void testDeleteManyToManyRelationship() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a1.getManyToManyB().size());
		assertEquals(0, a2.getManyToManyB().size());
		assertEquals(0, b1.getManyToManyA().size());
		assertEquals(0, b2.getManyToManyA().size());
		
		a1.addManyToManyB(b1);
		a1.addManyToManyB(b2);
		b1.addManyToManyA(a2);
		b2.addManyToManyA(a2);
		
		assertEquals(2, a1.getManyToManyB().size());
		assertEquals(2, a2.getManyToManyB().size());
		assertEquals(2, b1.getManyToManyA().size());
		assertEquals(2, b2.getManyToManyA().size());
		
		a1.removeManyToManyB(b1);
		b1.removeManyToManyA(a2);
		
		assertEquals(1, a1.getManyToManyB().size());
		assertEquals(1, a2.getManyToManyB().size());
		assertEquals(0, b1.getManyToManyA().size());
		assertEquals(2, b2.getManyToManyA().size());
	}
	
	@Test
	public void testSetAddRemoveReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		assertEquals(0, a.getManyToManyReflexive().size());
		
		a.addManyToManyReflexive(a);
		assertEquals(1, a.getManyToManyReflexive().size());
		
		a.removeManyToManyReflexive(a);
		assertEquals(0, a.getManyToManyReflexive().size());
	}
	
	private interface A {
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.REMOVE)
		List<B> getManyToManyWrongAction();
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.READ)
		void addManyToManyWrongAction(B b);
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.READ)
		void removeManyToManyWrongAction(B b);
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.READ)
		List<B> getManyToManyWrongNumArgs(B b);
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void addManyToManyWrongNumArgs();
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.REMOVE)
		void removeManyToManyWrongNumArgs();
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_CARDINALITY", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void addManyToManyWrongCardinality(B b);
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_DIRECTION", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void addManyToManyWrongDirection(B b);
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.OUTGOING, action = CollectionAction.READ)
		List<B> getManyToManyB();
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void addManyToManyB(B b);
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.OUTGOING, action = CollectionAction.REMOVE)
		void removeManyToManyB(B b);
		
		@ManyToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.READ)
		List<A> getManyToManyReflexive();
		
		@ManyToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.ADD)
		void addManyToManyReflexive(A a);
		
		@ManyToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.REMOVE)
		void removeManyToManyReflexive(A a);
	}
	
	private interface B {
		
		@OneToMany(name = "MANY_TO_MANY_WRONG_CARDINALITY", direction = Direction.INCOMING, action = CollectionAction.ADD)
		void addManyToManyWrongCardinality(A a);
		
		@ManyToMany(name = "MANY_TO_MANY_WRONG_DIRECTION", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void addManyToManyWrongDirection(A a);
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.INCOMING, action = CollectionAction.READ)
		List<B> getManyToManyA();
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.INCOMING, action = CollectionAction.ADD)
		void addManyToManyA(A a);
		
		@ManyToMany(name = "MANY_TO_MANY", direction = Direction.INCOMING, action = CollectionAction.REMOVE)
		void removeManyToManyA(A a);
	}
}
