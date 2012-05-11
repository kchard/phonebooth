package com.kevinchard.phonebooth.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.kevinchard.phonebooth.Entity;
import com.kevinchard.phonebooth.UnknownEntityException;
import com.kevinchard.phonebooth.core.EntityNode;
import com.kevinchard.phonebooth.core.SimpleTopologyManager;
import com.kevinchard.phonebooth.core.TopologyManager;

public class SimpleTopologyManagerTest {

	private GraphDatabaseService dbService;
	private TopologyManager tm;
	
	@Before
	public void setUp() {
		dbService = new ImpermanentGraphDatabase();
		tm = new SimpleTopologyManager(dbService);
	}
	
	@After
	public void tearDown() {
		dbService.shutdown();
	}
	
	@Test(expected = UnknownEntityException.class)
	public void testNoEntityAnnotation() {
		tm.addEntityDefinition(NoEntityAnnotation.class);
	}
	
	
	@Test
	public void testEntityDefinitionNotExists() {
		assertFalse(tm.entityDefinitionExists(A.class));
		assertEquals(0, tm.entityDefinitions().size());
	}
	
	@Test
	public void testAddEntityDefinition() {
		assertEquals("A_REF", tm.addEntityDefinition(A.class).name());
		assertTrue(tm.entityDefinitionExists(A.class));
		
		assertEquals(1, tm.entityDefinitions().size());
	}
	
	public void testAddEntityDefinitionAlreadyExists() {
		tm.addEntityDefinition(A.class);
		tm.addEntityDefinition(A.class);
		
		assertEquals(1, tm.entityDefinitions().size());
	}
	
	@Test
	public void testAddEntityDefinitions() {
		
		tm.addEntityDefinitions(A.class, B.class);
		
		assertTrue(tm.entityDefinitionExists(A.class));
		assertTrue(tm.entityDefinitionExists(B.class));
		
		assertEquals(2, tm.entityDefinitions().size());
	}
	
	@Test
	public void testAddEntityDefinitionsWithException() {
		
		try {
			tm.addEntityDefinitions(A.class, NoEntityAnnotation.class);
			fail("Should have thrown exception");
		} catch(UnknownEntityException e) {}
		
		assertFalse(tm.entityDefinitionExists(A.class));
		assertFalse(tm.entityDefinitionExists(NoEntityAnnotation.class));
		assertEquals(0, tm.entityDefinitions().size());
	}
	
	@Test(expected = UnknownEntityException.class)
	public void testCreateNodeNoDefinition() {
		tm.createNode(A.class);
	}
	
	@Test
	public void testCreateNode() {
		tm.addEntityDefinition(A.class);
		EntityNode entityNode = tm.createNode(A.class);
		assertEquals("A", entityNode.getNode().getSingleRelationship(DynamicRelationshipType.withName("A"), Direction.INCOMING).getType().name());
		assertEquals(A.class, EntityNode.getEntityClass(entityNode.getNode()));
	}
	
	@Entity(value = "A")
	private class A {}
	
	@Entity(value = "B")
	private class B {}
	
	private class NoEntityAnnotation{}
}
