dbcrud
======

# Goal

Just connect to a JDBC datasource and start CRUDing without predefining any ORM or DAOs

# Use Cases: 

* In some ocassions I just want a slightly dumb data storage backend, not much business logic on top of the data, so having a quick and generic DAO is the goal, no need for custom class definitions to represent the data.
* The previous scenario can be the case when the smartness of the logic resides in the client side, or the presentation tier. E.g. a rich 'smart' Angular JS app backed by a plain dumb data storage.
* In other ocassions, like in my current job, there's some legacy DB that I just want to expose 'quickly' as a rest service.

# Usage Examples

* Creating an model

```scala
val dataModel = new DataModel(dataSource)
```

* Some CRUDs.

```scala
val employees = dataModel.select('EMPLOYEE, offset=10, count=10)

dataModel.insert('EMPLOYEE, 'ID->100, 'NAME->"Jack", 'ROLE->"Manager")

dataModel.update('EMPLOYEE, 100, 'ROLE->"Supervisor")

dataModel.delete('EMPLOYEE, 100)

dataModel.deleteWhere('EMPLOYEE, 'ROLE->"Manager")

dataModel.entities // 'EMPLOYEE, 'DEPARTMENT, 'OFFICE, ...

dataModel.select('EMPLOYEE, 'ROLE->"Manager")

```

# Extensions.

* dbcrud-rest: Exposes a REST interface to a backend database.
 
