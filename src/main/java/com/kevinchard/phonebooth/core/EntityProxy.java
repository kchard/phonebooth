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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Transaction;

import com.kevinchard.phonebooth.Action;
import com.kevinchard.phonebooth.CollectionAction;
import com.kevinchard.phonebooth.Id;
import com.kevinchard.phonebooth.IllegalRelationshipException;
import com.kevinchard.phonebooth.ManyToMany;
import com.kevinchard.phonebooth.ManyToOne;
import com.kevinchard.phonebooth.OneToMany;
import com.kevinchard.phonebooth.OneToOne;
import com.kevinchard.phonebooth.Property;
import com.kevinchard.phonebooth.util.Assert;


final class EntityProxy implements InvocationHandler {

	private static Method hashCodeMethod;
	private static Method equalsMethod;
	private static Method toStringMethod;
	
	static {
		try {
			hashCodeMethod = Object.class.getMethod("hashCode", (Class<?>[]) null);
			equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
			toStringMethod = Object.class.getMethod("toString", (Class<?>[]) null);
		} catch (NoSuchMethodException e) {
			throw new NoSuchMethodError(e.getMessage());
		}
	}

	private final EntityNode entity;

	private EntityProxy(EntityNode entity) {
		this.entity = entity;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Class<?> declaringClass = method.getDeclaringClass();
		if (declaringClass == Object.class) {
			return processObjectMethods(method, args);
		}
		
		Id id = method.getAnnotation(Id.class);
		if(id != null) {
			return processId(args);
		}

		Property property = method.getAnnotation(Property.class);
		if (property != null) {
			return processProperty(property, args);
		}
		
		OneToOne oneToOne = method.getAnnotation(OneToOne.class);
		if(oneToOne != null) {
			return processOneToOne(oneToOne, args);
		}
		
		OneToMany oneToMany = method.getAnnotation(OneToMany.class);
		if(oneToMany != null) {
			return processOneToMany(oneToMany, args);
		}
		
		ManyToOne manyToOne = method.getAnnotation(ManyToOne.class);
		if(manyToOne != null) {
			return processManyToOne(manyToOne, args);
		}
		
		ManyToMany manyToMany = method.getAnnotation(ManyToMany.class);
		if(manyToMany != null) {
			return processManyToMany(manyToMany, args);
		}

		return null;
	}
	
	private Object processObjectMethods(Method method, Object[] args) {
		if (method.equals(hashCodeMethod)) {
			return entity.hashCode();
		} else if (method.equals(equalsMethod)) {
			return entity.equals(unwrap(args[0]));
		} else if (method.equals(toStringMethod)) {
			return entity.toString();
		} else {
			throw new InternalError("unexpected Object method dispatched: " + method);
		}
	}
	
	private Object processId(Object[] args) {
		checkReadArgs(args);
		return entity.getId();
	}
	
	private Object processProperty(Property property, Object[] args) {

		if (property.action().equals(Action.READ)) {
			checkReadArgs(args);
			return entity.getPropertyOrNull(property.name());
		} else if (property.action().equals(Action.WRITE)) {
			checkWriteArgs(args);
			entity.setProperty(property.name(), args[0]);
			return null;
		} else {
			throw new InternalError("Unexpected Action: " + property.action());
		}
	}

	private Object processOneToOne(OneToOne oneToOne, Object[] args) {
		
		if(oneToOne.action().equals(Action.READ)) {
			checkReadArgs(args);
			EntityNode relatedEntity =  entity.getRelatedEntity(DynamicRelationshipType.withName(oneToOne.name()), oneToOne.direction());
			return relatedEntity == null ? null : EntityProxy.createProxy(relatedEntity, relatedEntity.getEntityClass());
		} else if(oneToOne.action().equals(Action.WRITE)) {
			checkWriteArgs(args);
			
			if(args[0] == null) {
				entity.deleteSingleRelationship(DynamicRelationshipType.withName(oneToOne.name()), oneToOne.direction());
				return null;
			}
				
			EntityNode otherEntity = unwrap(args[0]);
			OneToOne oppositeOneToOne = getRelationshipNamed(otherEntity.getEntityClass(), oneToOne.name(), OneToOne.class);
			if(oppositeOneToOne == null) {
				throw new IllegalRelationshipException();
			}
				
			if(!oneToOne.direction().equals(oppositeOneToOne.direction().reverse())) {
				throw new IllegalRelationshipException();
			}
			
			if(!entity.isRelatedTo(otherEntity, DynamicRelationshipType.withName(oneToOne.name()), oneToOne.direction())) {
				
				Transaction tx = entity.getNode().getGraphDatabase().beginTx();
				try {
					otherEntity.deleteSingleRelationship(DynamicRelationshipType.withName(oppositeOneToOne.name()), oppositeOneToOne.direction());
					entity.deleteSingleRelationship(DynamicRelationshipType.withName(oneToOne.name()), oneToOne.direction());
					entity.createRelationship(otherEntity, DynamicRelationshipType.withName(oneToOne.name()), oneToOne.direction());
					tx.success();
				} finally {
					tx.finish();
				}
			}
			
			return null;
		} else {
			throw new InternalError("Unexpected Action: " + oneToOne.action());
		}
	}

	private Object processOneToMany(OneToMany oneToMany, Object[] args) {
		
		if(oneToMany.action().equals(CollectionAction.READ)) {
			checkReadArgs(args);
			List<EntityNode> entities = entity.getRelatedEntities(DynamicRelationshipType.withName(oneToMany.name()), oneToMany.direction());
			return entities.size() == 0 ? Collections.emptyList() : EntityProxy.createProxies(entities, entities.get(0).getEntityClass());
		} else if (oneToMany.action().equals(CollectionAction.ADD) || oneToMany.action().equals(CollectionAction.REMOVE)) {
			checkWriteArgs(args);
			
			if(args[0] == null) {
				return null;
			}
				
			EntityNode otherEntity = unwrap(args[0]);
			
			ManyToOne manyToOne = getRelationshipNamed(otherEntity.getEntityClass(), oneToMany.name(), ManyToOne.class);
			if(manyToOne == null) {
				throw new IllegalRelationshipException();
			}
				
			if(!oneToMany.direction().equals(manyToOne.direction().reverse())) {
				throw new IllegalRelationshipException();
			}
			
			if(oneToMany.action().equals(CollectionAction.ADD)) {
				if(!entity.isRelatedTo(otherEntity, DynamicRelationshipType.withName(oneToMany.name()), oneToMany.direction())) {
					
					Transaction tx = entity.getNode().getGraphDatabase().beginTx();
					try {
						otherEntity.deleteSingleRelationship(DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
						entity.createRelationship(otherEntity, DynamicRelationshipType.withName(oneToMany.name()), oneToMany.direction());
						tx.success();
					} finally {
						tx.finish();
					}
				}
				
			} else if (oneToMany.action().equals(CollectionAction.REMOVE)) {
				checkWriteArgs(args);
				otherEntity.deleteSingleRelationship(DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
			}
						
			return null;
				
		}  else {
			throw new InternalError("Unexpected Action: " + oneToMany.action());
		}
		
	}
	
	private Object processManyToOne(ManyToOne manyToOne, Object[] args) {
		
		if(manyToOne.action().equals(Action.READ)) {
			checkReadArgs(args);
			EntityNode relatedEntity =  entity.getRelatedEntity(DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
			return relatedEntity == null ? null : EntityProxy.createProxy(relatedEntity, relatedEntity.getEntityClass());
		} else if (manyToOne.action().equals(Action.WRITE)) {
			checkWriteArgs(args);
			
			if(args[0] == null) {
				entity.deleteSingleRelationship(DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
				return null;
			}
				
			EntityNode otherEntity = unwrap(args[0]);
			
			OneToMany oneToMany = getRelationshipNamed(otherEntity.getEntityClass(), manyToOne.name(), OneToMany.class);
			if(oneToMany == null) {
				throw new IllegalRelationshipException();
			}
				
			if(!manyToOne.direction().equals(oneToMany.direction().reverse())) {
				throw new IllegalRelationshipException();
			}
			
			if(!entity.isRelatedTo(otherEntity, DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction())) {
				
				Transaction tx = entity.getNode().getGraphDatabase().beginTx();
				try {
					entity.deleteSingleRelationship(DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
					entity.createRelationship(otherEntity, DynamicRelationshipType.withName(manyToOne.name()), manyToOne.direction());
					tx.success();
				} finally {
					tx.finish();
				}
			}
			
			return null;
				
		} else {
			throw new InternalError("Unexpected Action: " + manyToOne.action());
		}
		
	}
	
	private Object processManyToMany(ManyToMany manyToMany, Object[] args) {
		
		if(manyToMany.action().equals(CollectionAction.READ)) {
			checkReadArgs(args);
			List<EntityNode> entities = entity.getRelatedEntities(DynamicRelationshipType.withName(manyToMany.name()), manyToMany.direction());
			return entities.size() == 0 ? Collections.emptyList() : EntityProxy.createProxies(entities, entities.get(0).getEntityClass());
		} else if(manyToMany.action().equals(CollectionAction.ADD) || manyToMany.action().equals(CollectionAction.REMOVE)) {
			checkWriteArgs(args);
			
			if(args[0] == null) {
				return null;
			}
				
			EntityNode otherEntity = unwrap(args[0]);
			
			ManyToMany oppositeManyToMany = getRelationshipNamed(otherEntity.getEntityClass(), manyToMany.name(), ManyToMany.class);
			if(oppositeManyToMany == null) {
				throw new IllegalRelationshipException();
			}
			
			if(!manyToMany.direction().equals(oppositeManyToMany.direction().reverse())) {
				throw new IllegalRelationshipException();
			}
			
			if(manyToMany.action().equals(CollectionAction.ADD)) {
				if(!entity.isRelatedTo(otherEntity, DynamicRelationshipType.withName(manyToMany.name()), manyToMany.direction())) {
					entity.createRelationship(otherEntity, DynamicRelationshipType.withName(manyToMany.name()), manyToMany.direction());
				}
			} else if (manyToMany.action().equals(CollectionAction.REMOVE)) {
				checkWriteArgs(args);
				entity.deleteRelationship(otherEntity, DynamicRelationshipType.withName(manyToMany.name()), manyToMany.direction());
			}
						
			return null;
		} else {
			throw new InternalError("Unexpected Action: " + manyToMany.action());
		}
	}
	
	private <T extends Annotation> T getRelationshipNamed(Class<?> entityClass, String name, Class<T> annotationClass) {
		for(Method method : entityClass.getMethods()) {
			T relationship = method.getAnnotation(annotationClass);
			if(relationship != null) {
				try {
					Method nameMethod = relationship.annotationType().getMethod("name");
					if(nameMethod.invoke(relationship).equals(name)) {
						return relationship;
					}
				} catch (Exception e) {
					throw new IllegalStateException("Relationship annotations must have a name() method!!!");
				} 
			}
		}
		
		return null;
	}

	private void checkReadArgs(Object[] args) {
		Assert.isNull(args, "Accessor methods mush take zero arguments!!!");
	}
	
	private void checkWriteArgs(Object[] args) {
		String message = "Mutator methods must have one arguments!!!";
		Assert.notNull(args, message);
		Assert.state(args.length == 1, message);
	}
	
	private EntityNode unwrap(Object proxy) {
		EntityProxy entityProxy = (EntityProxy)Proxy.getInvocationHandler(proxy);
		return entityProxy.entity;
	}

	static Object createProxy(EntityNode entity, Class<?> entityInterface) {
		return Proxy.newProxyInstance(EntityProxy.class.getClassLoader(), new Class[] { entityInterface }, new EntityProxy(entity));
	}
	
	static List<Object> createProxies(List<EntityNode> entities, Class<?> entityInterface) {
		
		List<Object> objects = new ArrayList<Object>();
		for(EntityNode entity : entities) {
			objects.add(EntityProxy.createProxy(entity, entityInterface));
		}
		
		return objects;
	}
}
