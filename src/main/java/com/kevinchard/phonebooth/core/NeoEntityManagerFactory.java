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

import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.kevinchard.phonebooth.EntityManager;
import com.kevinchard.phonebooth.EntityManagerFactory;
import com.kevinchard.phonebooth.tx.TransactionManager;
import com.kevinchard.phonebooth.util.Assert;

final class NeoEntityManagerFactory implements EntityManagerFactory {

	private final TopologyManager topologyManager;
	private final TransactionManager txManager;
	
	NeoEntityManagerFactory(GraphDatabaseService dbService, List<Class<?>> entityClasses, Map<String, Object> properties) {
		Assert.notNull(dbService, "dbSerivce cannot be null!!!");
		Assert.notNull(entityClasses, "entityClasses cannot be null!!!");
		
		this.topologyManager = new SimpleTopologyManager(dbService);
		this.txManager = new SimpleTransactionManager(dbService);
		
		initialize(entityClasses, properties);
	}
	
	private void initialize(List<Class<?>> entityClasses, Map<String, Object> properties) {
		for(Class<?> entityClass : entityClasses) {
			if(!topologyManager.entityDefinitionExists(entityClass)) {
				topologyManager.addEntityDefinition(entityClass);
			}
		}
	}
	
	@Override
	public EntityManager createEntityManager() {
		return new NeoEntityManager(topologyManager, txManager);
	}
	

	static final class SimpleTransactionManager implements TransactionManager {
		
		private final GraphDatabaseService dbService;
		
		public SimpleTransactionManager(GraphDatabaseService dbService) {
			this.dbService = dbService;
		}
		
		@Override
		public Transaction beginTx() {
			return dbService.beginTx();
		}
		
	}
}
