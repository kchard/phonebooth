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

import org.neo4j.graphdb.Transaction;

import com.kevinchard.phonebooth.EntityManager;
import com.kevinchard.phonebooth.UnknownEntityException;
import com.kevinchard.phonebooth.tx.TransactionManager;


final class NeoEntityManager implements EntityManager {

	private final TopologyManager topologyManager;
	private final TransactionManager txManager;
	
	NeoEntityManager(TopologyManager database, TransactionManager txManager) {
		this.topologyManager = database;
		this.txManager = txManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T create(Class<T> entityClass) {
		
		if(!topologyManager.entityDefinitionExists(entityClass)){
			throw new UnknownEntityException();
		}
		
		return (T) EntityProxy.createProxy(topologyManager.createNode(entityClass), entityClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> findAll(Class<T> entityClass) {
		
		List<T> entities = new ArrayList<T>();
		for(EntityNode entityNode : topologyManager.getAll(entityClass)) {
			entities.add((T) EntityProxy.createProxy(entityNode, entityClass));
		}
		
		return entities;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(Long id, Class<T> clazz) {
		
		EntityNode entityNode = topologyManager.get(id, clazz);
		
		return (T) EntityProxy.createProxy(entityNode, clazz);
	}

	@Override
	public void delete(Long id, Class<?> clazz) {
		topologyManager.get(id, clazz).delete();
	}
	
	@Override
	public Transaction beginTransaction() {
		return txManager.beginTx();
	}
}
