phonebooth
==========

**A persistence framework for Neo4j**

The phonebooth framework is intended allow applications to quickly and easily define and persist domain entity objects using Neo4j. With the phonebooth framework, data persistence is as easy as:

1. Annotate some interfaces to model relationships between entities

2. Use the builder API of the Persistence class to create an EntityManagerFactory

3. Create an EntityManager

4. Use your entities in your application

With the phonebooth framework, an application does not need to explicitly define a DAO layer or any anemic entity objects whose only purpose is to provide accessor/mutator methods. By annotating an interface with meta information about your entities properties and relationships, phonebooth will take care of managing the underlying graph's nodes and relationships in a type safe way.

Getting Started
---------------
**Manager.java**

    @Entity(value = "MANAGER")
	public interface Manager {

		@Id
		Long getId();
	
		@Property(name = "NAME", action = Action.READ)
		String getName();
	
		@Property(name = "NAME", action = Action.WRITE)
		void setName(String name);
	
		@OneToMany(name = "WORKS_FOR", direction = Direction.INCOMING, action = CollectionAction.READ)
		List<Employee> getEmployees(); 
	
		@OneToMany(name = "WORKS_FOR", direction = Direction.INCOMING, action = CollectionAction.ADD)
		List<Employee> addEmployee(Employee employee); 
	
		@OneToMany(name = "WORKS_FOR", direction = Direction.INCOMING, action = CollectionAction.REMOVE)
		List<Employee> removeEmployee(Employee employee); 
	}

**Employee.java**

	@Entity(value = "EMPLOYEE")
	public interface Employee {

		@Id
		Long getId();
	
		@Property(name = "NAME", action = Action.READ)
		String getName();
	
		@Property(name = "NAME", action = Action.WRITE)
		void setName(String name);
	
		@ManyToOne(name = "WORKS_FOR", direction = Direction.OUTGOING, action = Action.READ)
		Manager getManager(); 
	
		@ManyToOne(name = "WORKS_FOR", direction = Direction.OUTGOING, action = Action.WRITE)
		Manager setManager(Manager manager); 
	
		@ManyToMany(name = "WORKS_WITH", direction = Direction.BOTH, action = CollectionAction.READ)
		List<Employee> getCoworkers(); 
	
		@ManyToMany(name = "WORKS_WITH", direction = Direction.BOTH, action = CollectionAction.ADD)
		List<Employee> addCoworker(Employee employee); 
	
		@ManyToMany(name = "WORKS_WITH", direction = Direction.BOTH, action = CollectionAction.REMOVE)
		List<Employee> removeCoworker(Employee employee); 
	}


**OfficeSpace.java**

    public class OfficeSpace {
	
		public static void main(String[] args) {

			EntityManagerFactory emf = Persistence.create("com.kevinchard.phonebooth.core.NeoEntityManagerFactory")
												  .withGraphDatabaseService(new ImpermanentGraphDatabase())
												  .forEntity(Manager.class)
				  							      .forEntity(Employee.class)
												  .build();

			EntityManager ef = emf.createEntityManager();
		
			//Create the manager
			Manager lumbergh = ef.create(Manager.class);
			lumbergh.setName("Bill Lumbergh");
		
			//Create employees
			Employee peter = ef.create(Employee.class);
			peter.setName("Peter Gibbons");
		
			Employee michael = ef.create(Employee.class);
			michael.setName("Michael Bolton");
		
			Employee samir = ef.create(Employee.class);
			samir.setName("Samir Nagheenanajar");
		
			//Peter works with Samir
			peter.addCoworker(samir);
		
			//Lumbergh managers Peter
			lumbergh.addEmployee(peter);
		
			//Michael is managed by Lumbergh
			michael.setManager(lumbergh);
		
			//Samir (who is Peter's coworker) is managed by Lumbergh
			peter.getCoworkers().get(0).setManager(lumbergh);
		
			//Michael works with Peter
			michael.addCoworker(peter);
		
			//Samir works with Michael
			samir.addCoworker(michael);
		
			print(peter);
			print(michael);
			print(samir);
		}
	
		private static void print(Employee employee) {
			System.out.println(employee.getName());
			System.out.println("\tReports To: " + employee.getManager().getName());
		
			for(Employee coworker : employee.getCoworkers()) {
				System.out.println("\tWorks With: " + coworker.getName());
			}
		
			System.out.println();
		}
	}
	
**Output**

	Peter Gibbons
		Reports To: Bill Lumbergh
		Works With: Samir Nagheenanajar
		Works With: Michael Bolton

	Michael Bolton
		Reports To: Bill Lumbergh
		Works With: Peter Gibbons
		Works With: Samir Nagheenanajar

	Samir Nagheenanajar
		Reports To: Bill Lumbergh
		Works With: Michael Bolton
		Works With: Peter Gibbons


