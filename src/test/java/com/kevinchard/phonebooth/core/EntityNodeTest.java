package com.kevinchard.phonebooth.core;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.kevinchard.phonebooth.core.EntityNode;

public class EntityNodeTest {

	private GraphDatabaseService dbService;
	
	@Before
	public void setUp() {
		dbService = new ImpermanentGraphDatabase();
	}
	
	@After
	public void tearDown() {
		dbService.shutdown();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNullNode() {
		new EntityNode(null, A.class);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorWithNoEntityClassForNode() {
		new EntityNode(createNode(), A.class);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testConstructorWithNonMatchingEntityClass() {
		Node node = createNode(String.class);
		new EntityNode(node, A.class);
	}
	
	@Test
	public void testConstructor() {
		Node node = createNode(A.class);
		assertNotNull(new EntityNode(node, A.class));
	}
	
	@Test
	public void testGetEntityClass() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		assertEquals(EntityNode.getEntityClass(node), en.getEntityClass());
	}
	
	@Test
	public void testGetNode() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		assertEquals(node, en.getNode());
	}
	
	@Test
	public void testGetNonExistentKey() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		assertNull(en.getPropertyOrNull("key"));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testSetReservedKeyProperty() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		en.setProperty(EntityNode.CLASS_PROPERTY_KEY, "val");
	}
	
	@Test
	public void testGetSetProperty() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		en.setProperty("key", "val");
		
		assertEquals("val", en.getPropertyOrNull("key"));
	}
	
	@Test
	public void testGetId() {
		Node node = createNode(A.class);
		
		EntityNode en = new EntityNode(node, A.class);
		assertEquals(node.getId(), en.getId().longValue());
	}
	
	@Test
	public void testGetRelatedEntityWithNoRelations() {		
		EntityNode en = new EntityNode(createNode(A.class), A.class);
		assertNull(en.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
	}
	
	@Test(expected = NotFoundException.class)
	public void testGetRelatedEntityWithTwoRelations() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
	}
	
	@Test
	public void testCreateOutgoingRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
	}
	
	@Test
	public void testCreateIncomingRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
	}
	
	@Test
	public void testCreateBothRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
	}
	
	@Test
	public void testGetRelatedEntitiesWithNoRelations() {		
		EntityNode en = new EntityNode(createNode(A.class), A.class);
		assertEquals(0, en.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.INCOMING).size());
	}
	
	@Test
	public void testGetRelatedEntitiestWithTwoRelations() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		assertEquals(2, en1.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.OUTGOING).size());
	}
	
	@Test
	public void testCreateOutgoingRelationshipWithGetRelatedEntities() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		assertEquals(en2, en1.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.OUTGOING).get(0));
		assertEquals(en1, en2.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.INCOMING).get(0));
	}
	
	@Test
	public void testCreateIncomingRelationshipWithGetRelatedEntities() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING);
		
		assertEquals(en2, en1.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.INCOMING).get(0));
		assertEquals(en1, en2.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.OUTGOING).get(0));
	}
	
	@Test
	public void testCreateBothRelationshipWithGetRelatedEntities() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(en2, en1.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.BOTH).get(0));
		assertEquals(en1, en2.getRelatedEntities(DynamicRelationshipType.withName("rel"), Direction.BOTH).get(0));
	}
	
	@Test
	public void testIsRelatedToWithNoRelation() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		assertFalse(en1.isRelatedTo(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING));
	}
	
	@Test
	public void testIsRelatedTo() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING);
		assertTrue(en1.isRelatedTo(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING));
		assertTrue(en2.isRelatedTo(en1, DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
	}
	
	@Test
	public void testIsRelatedToBoth() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		assertTrue(en1.isRelatedTo(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertTrue(en2.isRelatedTo(en1, DynamicRelationshipType.withName("rel"), Direction.BOTH));
	}
	
	@Test
	public void testDeleteSingleRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		
		en1.deleteSingleRelationship(DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(null, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(null, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
	}
	
	@Test
	public void testDeleteIncomingRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
		
		en1.deleteRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.INCOMING);
		
		assertEquals(null, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
		assertEquals(null, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
	}
	
	@Test
	public void testDeleteOutgoingRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
		
		en1.deleteRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.OUTGOING);
		
		assertEquals(null, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.OUTGOING));
		assertEquals(null, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.INCOMING));
	}
	
	@Test
	public void testDeleteBothRelationship() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		
		en1.deleteRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(null, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(null, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
	}
	
	@Test
	public void testDelete() {		
		EntityNode en1 = new EntityNode(createNode(A.class), A.class);
		EntityNode en2 = new EntityNode(createNode(A.class), A.class);
		
		en1.createRelationship(en2, DynamicRelationshipType.withName("rel"), Direction.BOTH);
		
		assertEquals(en2, en1.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		assertEquals(en1, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
		
		en1.delete();
		
		assertEquals(null, en2.getRelatedEntity(DynamicRelationshipType.withName("rel"), Direction.BOTH));
	}
	
	private Node createNode() {
		Transaction tx = dbService.beginTx();
		try{
			return dbService.createNode();
		} finally {
			tx.success();
			tx.finish();
		}
	}
	
	private Node createNode(Class<?> clazz) {
		Transaction tx = dbService.beginTx();
		try{
			Node node = dbService.createNode();
			EntityNode.setEntityClass(node, clazz);
			return node;
		} finally {
			tx.success();
			tx.finish();
		}
	}
	
	private static interface A {}
}
