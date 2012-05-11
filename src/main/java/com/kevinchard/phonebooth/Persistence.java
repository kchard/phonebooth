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

package com.kevinchard.phonebooth;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;

public class Persistence {

	public static EntityManagerFactoryBuilder create(String emfClass) {
		return new EntityManagerFactoryBuilder(emfClass);
	}

	public static final class EntityManagerFactoryBuilder {
		
		private GraphDatabaseService dbService;
		private String emfClass;
		private List<Class<?>> entityClasses = new ArrayList<Class<?>>();
		private Map<String, Object> properties = new HashMap<String, Object>();
		
		public EntityManagerFactoryBuilder(String emfClass) {
			this.emfClass = emfClass;
		}
		
		public EntityManagerFactoryBuilder withGraphDatabaseService(GraphDatabaseService dbService) {
			this.dbService = dbService;
			return this;
		}
		
		public EntityManagerFactoryBuilder forEntity(Class<?> entityClass) {
			entityClasses.add(entityClass);
			return this;
		}
		
		public EntityManagerFactoryBuilder withProperty(String key, Object value) {
			properties.put(key, value);
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public EntityManagerFactory build() {
			
			EntityManagerFactory emf = null;
			try {
				
				Class<? extends EntityManagerFactory> clazz = (Class<? extends EntityManagerFactory>) Class.forName(emfClass);
				
				Constructor<? extends EntityManagerFactory> constructor = clazz.getDeclaredConstructor(GraphDatabaseService.class, List.class, Map.class);
				constructor.setAccessible(true);
				emf = (EntityManagerFactory) constructor.newInstance(dbService, entityClasses, properties);
				
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
			
			return emf;
		}
	}
}
