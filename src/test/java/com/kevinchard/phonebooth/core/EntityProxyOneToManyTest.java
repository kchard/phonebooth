package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;

import com.kevinchard.phonebooth.Action;
import com.kevinchard.phonebooth.CollectionAction;
import com.kevinchard.phonebooth.IllegalRelationshipException;
import com.kevinchard.phonebooth.ManyToOne;
import com.kevinchard.phonebooth.OneToMany;
import com.kevinchard.phonebooth.core.EntityProxy;

public class EntityProxyOneToManyTest extends EntityProxyTestBase {
	
	@Before
	public void setUp() {
		super.setUp();
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongReadAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.get1toManyWrongAction();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongAddAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.add1toManyWrongAction(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongRemoveAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.remove1ToManyWrongAction(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.get1toManyWrongNumArgs(b);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongAddNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.add1toManyWrongNumArgs();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testOnetoManyRelationshipWithWrongRemoveNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		
		a.remove1ToManyWrongNumArgs();
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testOneToManyRelationshipWithWrongCardinality() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.add1toManyWrongCardinality(b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testManyToOneRelationshipWithWrongCardinality() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.setManyTo1WrongCardinality(b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testOneToManyRelationshipWithWrongDirection() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		a.add1toManyWrongDirection(b);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testManyToOneRelationshipWithWrongDirection() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
	
		b.setManyTo1WrongDirection(a);
	}
	
	@Test 
	public void testSetGetOneToManyRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b1.getManyTo1A());
		assertNull(b2.getManyTo1A());
		
		a.add1toManyB(b1);
		a.add1toManyB(b2);
		
		assertEquals(2, a.get1toManyB().size());
		assertEquals(a, b1.getManyTo1A());
		assertEquals(a, b2.getManyTo1A());
	}
	
	@Test 
	public void testSetGetOneToManyRelationshipTwice() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b.getManyTo1A());
		
		a.add1toManyB(b);
		a.add1toManyB(b);
		
		assertEquals(1, a.get1toManyB().size());
		assertEquals(b, a.get1toManyB().get(0));
		assertEquals(a, b.getManyTo1A());
	}
	
	@Test 
	public void testDeleteOneToManyRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b.getManyTo1A());
		
		a.add1toManyB(b);
		
		assertEquals(1, a.get1toManyB().size());
		assertEquals(a, b.getManyTo1A());
		
		a.remove1ToManyB(b);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b.getManyTo1A());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testManyToOneRelationshipGetterWrongAction() {
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		b.getManyTo1WrongAction();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testManyToOneRelationshipSetterWrongAction() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		b.setManyTo1WrongAction(a);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testManyToOneRelationshipGetterWrongNumArgs() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		b.getManyTo1WrongNumArgs(a);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testManyToOneRelationshipSetterWrongNumArgs() {
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		b.setManyTo1WrongNumArgs();
	}
	
	@Test 
	public void testSetGetManyToOneRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b1.getManyTo1A());
		assertNull(b2.getManyTo1A());
		
		b1.setManyTo1A(a);
		b2.setManyTo1A(a);
		
		assertEquals(2, a.get1toManyB().size());
		assertEquals(a, b1.getManyTo1A());
		assertEquals(a, b2.getManyTo1A());
	}
	
	@Test 
	public void testSetGetManyToOneRelationshipTwice() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b.getManyTo1A());
		
		b.setManyTo1A(a);
		b.setManyTo1A(a);
		
		assertEquals(1, a.get1toManyB().size());
		assertEquals(a, b.getManyTo1A());
	}
	
	@Test 
	public void testSetGetManyToOneRelationshipWithOverwrite() {
		A a1 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		A a2 = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a1.get1toManyB().size());
		assertEquals(0, a2.get1toManyB().size());
		assertNull(b.getManyTo1A());
		
		b.setManyTo1A(a1);
		assertEquals(1, a1.get1toManyB().size());
		assertEquals(b, a1.get1toManyB().get(0));
		assertEquals(0, a2.get1toManyB().size());
		assertEquals(a1, b.getManyTo1A());

		a2.add1toManyB(b);
		assertEquals(0, a1.get1toManyB().size());
		assertEquals(1, a2.get1toManyB().size());
		assertEquals(b, a2.get1toManyB().get(0));
		assertEquals(a2, b.getManyTo1A());
	}
	
	@Test 
	public void testDeleteManyToOneRelationship() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		B b1 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		B b2 = (B) EntityProxy.createProxy(createEntityNode(B.class), B.class);
		
		assertEquals(0, a.get1toManyB().size());
		assertNull(b1.getManyTo1A());
		assertNull(b2.getManyTo1A());
		
		b1.setManyTo1A(a);
		b2.setManyTo1A(a);
		
		assertEquals(2, a.get1toManyB().size());
		assertEquals(a, b1.getManyTo1A());
		assertEquals(a, b2.getManyTo1A());
		
		b1.setManyTo1A(null);
		
		assertEquals(1, a.get1toManyB().size());
		assertNull(b1.getManyTo1A());
		assertEquals(a, b2.getManyTo1A());
	}
	
	public void testOneToManyGetReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertNull(a.get1toManyReflexive());
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testOneToManyAddReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.add1toManyReflexive(a);
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testOneToManyRemoveReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.remove1ToManyReflexive(a);
	}
	
	public void testManyToOneGetReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		assertNull(a.getManyTo1Reflexive());
	}
	
	@Test(expected = IllegalRelationshipException.class)
	public void testManyToOneSetReflexive() {
		A a = (A) EntityProxy.createProxy(createEntityNode(A.class), A.class);
		a.setManyTo1Reflexive(a);
	}
	
	private interface A {
		
		@OneToMany(name = "WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		List<B> get1toManyWrongAction();
		
		@OneToMany(name = "WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.READ)
		void add1toManyWrongAction(B b);
		
		@OneToMany(name = "WRONG_ACTION", direction = Direction.OUTGOING, action = CollectionAction.READ)
		void remove1ToManyWrongAction(B b);
		
		@OneToMany(name = "WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.READ)
		List<B> get1toManyWrongNumArgs(B b);
		
		@OneToMany(name = "WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void add1toManyWrongNumArgs();
		
		@OneToMany(name = "WRONG_NUM_ARGS", direction = Direction.OUTGOING, action = CollectionAction.REMOVE)
		void remove1ToManyWrongNumArgs();
		
		@OneToMany(name = "1", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void add1toManyWrongCardinality(B b);

		@ManyToOne(name = "2", direction = Direction.OUTGOING, action = Action.WRITE)
		void setManyTo1WrongCardinality(B b);
		
		@OneToMany(name = "3", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void add1toManyWrongDirection(B b);
		
		@OneToMany(name = "5", direction = Direction.OUTGOING, action = CollectionAction.READ)
		List<B> get1toManyB();
		
		@OneToMany(name = "5", direction = Direction.OUTGOING, action = CollectionAction.ADD)
		void add1toManyB(B b);
		
		@OneToMany(name = "5", direction = Direction.OUTGOING, action = CollectionAction.REMOVE)
		void remove1ToManyB(B b);
		
		@OneToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.READ)
		List<A> get1toManyReflexive();
		
		@OneToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.ADD)
		void add1toManyReflexive(A a);
		
		@OneToMany(name = "REFLEXIVE", direction = Direction.BOTH, action = CollectionAction.REMOVE)
		void remove1ToManyReflexive(A a);
		
		@ManyToOne(name = "REFLEXIVE", direction = Direction.BOTH, action = Action.READ)
		A getManyTo1Reflexive();
		
		@ManyToOne(name = "REFLEXIVE", direction = Direction.BOTH, action = Action.WRITE)
		void setManyTo1Reflexive(A a);
	}
	
	private interface B {
		
		@ManyToOne(name = "WRONG_ACTION", direction = Direction.INCOMING, action = Action.WRITE)
		A getManyTo1WrongAction();
		
		@ManyToOne(name = "WRONG_ACTION", direction = Direction.INCOMING, action = Action.READ)
		void setManyTo1WrongAction(A a);
		
		@ManyToOne(name = "WRONG_NUM_ARGS", direction = Direction.INCOMING, action = Action.READ)
		A getManyTo1WrongNumArgs(A a);
		
		@ManyToOne(name = "WRONG_NUM_ARGS", direction = Direction.INCOMING, action = Action.WRITE)
		void setManyTo1WrongNumArgs();
		
		//No OneToMany
		void add1toManyWrongCardinality(A a);
		
		//No ManyToOne
		void setManyTo1WrongCardinality(A a);
		
		@ManyToOne(name = "3", direction = Direction.OUTGOING, action = Action.WRITE)
		void setManyTo1WrongDirection(A a);
		
		@ManyToOne(name = "5", direction = Direction.INCOMING, action = Action.READ)
		A getManyTo1A();
		
		@ManyToOne(name = "5", direction = Direction.INCOMING, action = Action.WRITE)
		void setManyTo1A(A a);
	}
	
}
