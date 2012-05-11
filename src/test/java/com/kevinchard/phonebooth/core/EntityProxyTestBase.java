package com.kevinchard.phonebooth.core;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.kevinchard.phonebooth.core.EntityNode;


public class EntityProxyTestBase {

private GraphDatabaseService dbService;
	
	public void setUp() {
		dbService = new ImpermanentGraphDatabase();
	}
	
	public void tearDown() {
		dbService.shutdown();
	}
	
	protected EntityNode createEntityNode(Class<?> entityClass) {
		Transaction tx = dbService.beginTx();
		try {
			Node node = dbService.createNode();
			EntityNode.setEntityClass(node, entityClass);
			tx.success();
			return new EntityNode(node, entityClass);
		} finally {
			tx.finish();
		}
	}
}
