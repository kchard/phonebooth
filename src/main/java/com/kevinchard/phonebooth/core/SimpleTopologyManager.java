/**
 * Copyright (c) 2012 Kevin Chard
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.kevinchard.phonebooth.core;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import com.kevinchard.phonebooth.Entity;
import com.kevinchard.phonebooth.UnknownEntityException;


final class SimpleTopologyManager implements TopologyManager {
	
	private static final String ENTITY_REF_TYPE = "ENTITY_REF_TYPE";
	private static final String SUFFIX = "_REF";
	
	private final GraphDatabaseService dbService;
	
	SimpleTopologyManager(GraphDatabaseService dbService) {
		this.dbService = dbService;
	}
	
	@Override
	public List<RelationshipType> entityDefinitions() {
		List<RelationshipType> types = new ArrayList<RelationshipType>();
		for(Relationship relationship : dbService.getReferenceNode().getRelationships(Direction.OUTGOING)) {
			types.add(relationship.getType());
		}
		
		return types;
	}
	
	@Override
	public boolean entityDefinitionExists(Class<?> entityClass) {
		Entity annotation = entityClass.getAnnotation(Entity.class);
		if(annotation == null) {
			return false;
		}
		
		RelationshipType type = createRelationshipType(annotation.value());
		
		return dbService.getReferenceNode().getSingleRelationship(type, Direction.OUTGOING) != null;
	}
	
	@Override
	public RelationshipType addEntityDefinition(Class<?> entityClass) {
		Transaction tx = dbService.beginTx();
		
		RelationshipType type = null;
		try {
			type = internalAddEntityDefinition(entityClass);
			tx.success();
		} finally {
			tx.finish();
		}
		
		return type;
	}
	
	@Override
	public List<RelationshipType> addEntityDefinitions(Class<?> ... entityClasses) {
		List<RelationshipType> types = new ArrayList<RelationshipType>();
		
		Transaction tx = dbService.beginTx();
		try {
			for(Class<?> entityClass : entityClasses) {
				types.add(addEntityDefinition(entityClass));
			}
			tx.success();
		}
		finally {
			tx.finish();
		}
		
		return types;
	}
	
	@Override
	public EntityNode createNode(Class<?> entityClass) {
	
		if(!entityDefinitionExists(entityClass)) {
			throw new UnknownEntityException();
		}
		
		Node node = null;
		Transaction tx = dbService.beginTx();
		try {
			Entity annotation = entityClass.getAnnotation(Entity.class);
			RelationshipType type = createRelationshipType(annotation.value());
			node = dbService.createNode();
			EntityNode.setEntityClass(node, entityClass);
			Node entityDefinitionNode = dbService.getReferenceNode().getSingleRelationship(type, Direction.OUTGOING).getEndNode();
			entityDefinitionNode.createRelationshipTo(node, DynamicRelationshipType.withName(annotation.value()));
			tx.success();
		} finally {
			tx.finish();
		}
		
		return new EntityNode(node, entityClass);
	}
	
	@Override
	public List<EntityNode> getAll(Class<?> entityClass) {
		
		Entity annotation = entityClass.getAnnotation(Entity.class);
		if(annotation == null) {
			throw new UnknownEntityException();
		}
		
		RelationshipType type = createRelationshipType(annotation.value());
		Node entityRefNode = dbService.getReferenceNode().getSingleRelationship(type, Direction.OUTGOING).getEndNode();
		
		List<EntityNode> entities = new ArrayList<EntityNode>();
		for(Relationship relationship : entityRefNode.getRelationships(DynamicRelationshipType.withName(annotation.value()), Direction.OUTGOING)) {
			entities.add(new EntityNode(relationship.getEndNode(), entityClass));
		}
		
		return entities;
	}
	
	@Override
	public EntityNode get(Long id, Class<?> entityClass) {
		
		Entity annotation = entityClass.getAnnotation(Entity.class);
		if(annotation == null) {
			throw new UnknownEntityException();
		}
		
		return new EntityNode(dbService.getNodeById(id), entityClass);
	}
	
	private RelationshipType internalAddEntityDefinition(Class<?> entityClass) {
		
		Entity annotation = entityClass.getAnnotation(Entity.class);
		if(annotation == null) {
			throw new UnknownEntityException();
		}
		
		RelationshipType type = createRelationshipType(annotation.value());
		if(!entityDefinitionExists(entityClass)) {
			Node node = dbService.createNode();
			node.setProperty(ENTITY_REF_TYPE, entityClass.getName());
			dbService.getReferenceNode().createRelationshipTo(node, type);
		}
		
		return type;
	}
	
	private RelationshipType createRelationshipType(final String type) {
		return new RelationshipType() {
			public String name() {
				return type + SUFFIX;
			}
		};
	}
}
