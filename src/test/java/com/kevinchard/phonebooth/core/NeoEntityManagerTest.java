package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.kevinchard.phonebooth.Action;
import com.kevinchard.phonebooth.Entity;
import com.kevinchard.phonebooth.EntityManager;
import com.kevinchard.phonebooth.Id;
import com.kevinchard.phonebooth.Property;
import com.kevinchard.phonebooth.UnknownEntityException;
import com.kevinchard.phonebooth.core.NeoEntityManager;
import com.kevinchard.phonebooth.core.SimpleTopologyManager;
import com.kevinchard.phonebooth.core.TopologyManager;
import com.kevinchard.phonebooth.core.NeoEntityManagerFactory.SimpleTransactionManager;
import com.kevinchard.phonebooth.tx.TransactionManager;


public class NeoEntityManagerTest {

	private GraphDatabaseService dbService;
	private EntityManager em;
	private TopologyManager tm;
	private TransactionManager txm;
	
	@Before
	public void setUp() {
		dbService = new ImpermanentGraphDatabase();
		tm = new SimpleTopologyManager(dbService);
		txm = new SimpleTransactionManager(dbService);
		em = new NeoEntityManager(tm, txm);
	}
	
	@After
	public void tearDown() {
		dbService.shutdown();
	}
	
	@Test(expected = UnknownEntityException.class)
	public void testCreateUnkownEntity() {
		em.create(A.class);
	}
	
	@Test
	public void testCreate() {
		tm.addEntityDefinition(A.class);
		A entity = em.create(A.class);
		assertNotNull(entity);
	}
	
	@Test
	public void testGetAll() {
		tm.addEntityDefinition(A.class);
		em.create(A.class);
		em.create(A.class);
		assertEquals(2, em.findAll(A.class).size());
	}
	
	@Test
	public void testGet() {
		tm.addEntityDefinition(A.class);
		A a = em.create(A.class);
		A notA = em.create(A.class);
		assertEquals(a, em.find(a.getId(), A.class));
		assertFalse(notA.equals(em.find(a.getId(), A.class)));
	}
	
	@Test
	public void testDelete() {
		tm.addEntityDefinition(A.class);
		
		A a = em.create(A.class);
		assertEquals(1, em.findAll(A.class).size());
		
		em.delete(a.getId(), A.class);
		assertEquals(0, em.findAll(A.class).size());
	}
	
	@Test
	public void testGetTransaction() {
		tm.addEntityDefinition(A.class);
		
		final A a = em.create(A.class);
		
		a.setProp("VALUE");
		assertEquals("VALUE", a.getProp());
		
		Transaction tx = em.beginTransaction();
		try {
			a.setProp("UPDATED");
			tx.success();
		} finally {
			tx.finish();
		}
		
		assertEquals("UPDATED", a.getProp());
	}
	
	@Test
	public void testGetTransactionWithException() {
		tm.addEntityDefinition(A.class);
		
		final A a = em.create(A.class);
		
		a.setProp("VALUE");
		assertEquals("VALUE", a.getProp());
		
		try {
			Transaction tx = em.beginTransaction();
			try {
				a.setProp("UPDATED");
				throw new RuntimeException();
			} finally {
				tx.finish();
			}
		} catch(Exception e) {}
		
		assertEquals("VALUE", a.getProp());
	}
	
	@Entity(value = "A")
	private interface A {
		@Id Long getId();
		
		@Property(name="PROP", action = Action.READ) String getProp();
		
		@Property(name="PROP", action = Action.WRITE) void setProp(String prop);
	}
}
