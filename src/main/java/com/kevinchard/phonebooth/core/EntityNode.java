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
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

import com.kevinchard.phonebooth.util.Assert;

final class EntityNode {

	public static final String CLASS_PROPERTY_KEY = EntityNode.class.getName() + "_CLASS";
	
	private final Node node;
	
	EntityNode(Node node, Class<?> entityClass) {
		
		Assert.notNull(node, "node is required to construct an EntityNode!!!");
		Assert.notNull(EntityNode.getEntityClass(node), "entityClass is required to construct an EntityNode!!!");
		Assert.state(EntityNode.getEntityClass(node).equals(entityClass), "node must have an an entityClass property that matches entityClass!!!");
		
		this.node = node;
	}
	
	Class<?> getEntityClass() {
		return EntityNode.getEntityClass(node);
	}
	
	Node getNode() {
		return node;
	}
	
	Long getId() {
		return node.getId();
	}
	
	Object getPropertyOrNull(String key) {
		return node.hasProperty(key) ? node.getProperty(key) : null;
	}
	
	void setProperty(String key, Object value) {
		
		if(CLASS_PROPERTY_KEY.equals(key)) {
			throw new IllegalArgumentException("'" + key + "' is a reserved key");
		}
		
		Transaction tx = node.getGraphDatabase().beginTx();
		try {
			node.setProperty(key, value);
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	EntityNode getRelatedEntity(RelationshipType relationshipType, Direction direction) {
		
		Relationship relationship = node.getSingleRelationship(relationshipType, direction);
		
		if(relationship == null) {
			return null;
		}
		
		Node relatedNode = determineRelatedNode(relationship, direction);
		Class<?> entityClass = EntityNode.getEntityClass(relatedNode);
		
		return new EntityNode(relatedNode, entityClass);
	}
	
	List<EntityNode> getRelatedEntities(RelationshipType relationshipType, Direction direction) {
		
		Iterable<Relationship> rels = node.getRelationships(relationshipType, direction);
		
		if(rels == null) {
			return null;
		}
		
		List<EntityNode> relatedEntities = new ArrayList<EntityNode>();
		
		for(Relationship relationship : rels) {
			Node relatedNode = determineRelatedNode(relationship, direction);
			Class<?> entityClass = EntityNode.getEntityClass(relatedNode);
			relatedEntities.add(new EntityNode(relatedNode, entityClass));
		}
		
		return relatedEntities;
	}
	
	boolean isRelatedTo(EntityNode entity, RelationshipType type, Direction direction) {
		
		for(Relationship relationship : node.getRelationships(type, direction)) {
			Node relatedNode = determineRelatedNode(relationship, direction);
			if(entity.getNode().equals(relatedNode)) {
				return true;
			}
		}
		
		return false;
	}
	
	void createRelationship(EntityNode entity, RelationshipType type, Direction direction) {
		
		Transaction tx = node.getGraphDatabase().beginTx();
		try {
			if(direction.equals(Direction.OUTGOING)) {
				node.createRelationshipTo(entity.node, type);
			} else if(direction.equals(Direction.INCOMING)) {
				entity.node.createRelationshipTo(node, type);
			} else if(direction.equals(Direction.BOTH)) {
				node.createRelationshipTo(entity.node, type);
			} else {
				throw new IllegalArgumentException("Direction must ne INCOMING, OUTGOING, or BOTH!!!");
			}
			
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	void deleteSingleRelationship(RelationshipType type, Direction direction) {
		
		Transaction tx = node.getGraphDatabase().beginTx();
		try {
			Relationship relationship = node.getSingleRelationship(type, direction);
			if(relationship != null) {
				relationship.delete();
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	void deleteRelationship(EntityNode entity, RelationshipType type, Direction direction) {
		
		Transaction tx = node.getGraphDatabase().beginTx();
		try {
			for(Relationship relationship : node.getRelationships(type, direction)) {
				Node relatedNode = determineRelatedNode(relationship, direction);
				if(entity.getNode().equals(relatedNode)) {
					relationship.delete();
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	void delete() {
		
		Transaction tx = node.getGraphDatabase().beginTx();
		try {
			
			for(Relationship rel : node.getRelationships()) {
				rel.delete();
			}
			
			node.delete();
			
			tx.success();
		} finally {
			tx.finish();
		}
	}
	
	private Node determineRelatedNode(Relationship relationship, Direction direction) {
		
		Node relatedNode;
		if(Direction.OUTGOING.equals(direction)) {
			relatedNode = relationship.getEndNode();
		} else if(Direction.INCOMING.equals(direction)) {
			relatedNode = relationship.getStartNode();
		} else if(Direction.BOTH.equals(direction)) {
			if(relationship.getStartNode().equals(relationship.getEndNode())) {
				relatedNode = relationship.getStartNode();
			} else if(node.equals(relationship.getStartNode())) {
				relatedNode = relationship.getEndNode();
			} else {
				relatedNode = relationship.getStartNode();
			}
		} else {
			throw new IllegalArgumentException("Direction must ne INCOMING or OUTGOING!!!");
		}
		
		return relatedNode;
	}
	
	@Override
	public int hashCode() {
		return node.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof EntityNode)) {
			return false;
		}
		
		EntityNode other = (EntityNode)o;
		
		return node.equals(other.getNode());
	}
	
	//This must be called from within a transaction... I dont know if I like this???
	static void setEntityClass(Node node, Class<?> entityClass) {
		node.setProperty(CLASS_PROPERTY_KEY, entityClass.getName());
	}
	
	static Class<?> getEntityClass(Node node) {
		try {
			String className = (String) node.getProperty(CLASS_PROPERTY_KEY);
			return Class.forName(className);
		} catch(NotFoundException nfe) {
			return null;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
