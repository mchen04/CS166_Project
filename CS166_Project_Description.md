# CS 166 Project Description

**Course:** CS 166, Winter 2026
**Team Size:** Teams of two (must keep the same team for the entire project)

---

## Introduction

In this project, we will model and build a database for a mechanics shop. The system will be used to track information about customers, cars, mechanics, car ownership, service requests, and billing information.

The project is divided into three phases:

1. Requirement analysis using the ER-model
2. Relational schema design
3. Implementation

Students will demo the complete working system at the end of the quarter.

---

## Phase 1: ER Design & Relational Schema Design

### ER Diagram

Generate an ER-diagram with any other supporting documentation describing the assumptions you made. For the ER-diagram you can use any graphical editor you want, and you should create a PDF file using ER notations from the lectures/lab/book.

You will be evaluated on the correctness of your ER-diagram. You can make reasonable assumptions on your design, as long as:

- You state them clearly in the documentation for this phase.
- They do not contradict the system requirements analysis provided.

### Relational Model

Translate the ER design to a PostgreSQL relational database schema. The database schema will be in the form of a single executable SQL script (`*.sql` file with SQL statements).

You will be evaluated for the correctness and completeness of your relational schema. You may find some constraints in the model and/or system requirement analysis that are not possible to represent or enforce in the relational schema. You may specify all these issues in the documentation and it will be considered in your final grade.

### Phase 1 Deliverables

1. PDF of ER Diagram and all assumptions
2. `*.sql` file with SQL `CREATE TABLE` statements that translate the ER diagram to a relational model

---

## Phase 2: Implementation Phase

Develop a client application that uses Java Database Connector (JDBC) for PostgreSQL. The client application will support specific functionality and queries for your system.

The code will be in Java and will contain basic functionality to communicate with the database and issue various SQL statements. You will implement the following functions:

### Function 1: Add Customer

Add a new customer into the database. Provide an interface that takes as input the information of a new customer (first name, last name, phone, address) and checks if the provided information is valid based on the constraints of the database schema.

### Function 2: Add Mechanic

Add a new mechanic into the database. Provide an interface that takes as input the information of a new mechanic (first name, last name, specialty, experience) and checks if the provided information is valid based on the constraints of the database schema.

### Function 3: Add Car

Add a new car into the database. Provide an interface that takes as input the information of a new car (VIN, make, model, year) and checks if the provided information is valid based on the constraints of the database schema.

### Function 4: Initiate a Service Request

Add a service request for a customer into the database:

- Given a last name, search the database of existing customers.
- If multiple customers match, display a menu listing all customers with that last name and ask the user to choose which customer initiated the service request.
- If no customers match, provide the option to add a new customer.
- If an existing customer is chosen, list all cars associated with that customer and provide the option to initiate the service request for one of the listed cars.
- If no car is selected, a new car should be added along with the service request information.

### Function 5: Close a Service Request

Complete an existing service request:

- Given a service request number and an employee ID, verify the information provided and attempt to create a closing request record.
- Check for validity of inputs: does the mechanic exist, does the request exist, is the closing date after the request date, etc.

### Query 6: Closed Requests with Bill < $100

List the date, comment, and bill for all closed requests where the bill is lower than $100.

### Query 7: Customers with More Than 20 Cars

List the first and last name of customers having more than 20 different cars (counting from the ownership relation).

### Query 8: Cars Before 1995 with < 50,000 Miles

List the make, model, and year of all cars built before 1995 having less than 50,000 miles. Get the odometer reading from `service_requests`.

### Query 9: Top K Cars by Service Requests

List the make, model, and number of service requests for the first *k* cars with the highest number of service orders. The *k* value should be positive and greater than 0 (user-provided). Focus on open service requests.

### Query 10: Customers by Total Bill (Descending)

List the first name, last name, and total bill of customers in descending order of their total bill for all cars brought to the mechanic. Find the aggregate cost per customer across all service requests.

### Phase 2 Deliverables

1. `.sql` files and `.java` files
2. Dummy data in `.csv` files
3. Demo of project

---

## Requirement Analysis

Below are the minimal requirements. You can make additional assumptions.

### Customer

- Has a first name, last name, phone number, and address.
- Can own many different cars.
- May bring any of their cars for service.
- Brings their car for service at a certain date, indicating to the mechanic if there is any problem with the car.

### Cars

- Have a unique VIN, a year, a make, and a model.
- A car has exactly one owner.
- A car may have many outstanding service requests.
- For each outstanding service request, the car has an odometer reading and the date the car was brought in by the customer.
- The service request will be closed when the car is fixed by a mechanic.

### Mechanic

- Has a first name, a last name, an employee ID, and years of experience.
- Works on a single car at a time.
- When a mechanic fixes a car, they close the service request indicating when it was closed, any comments, and the final bill.
- Exactly one mechanic can create a service request.
- Both open and closed service requests need to be available at any time.

---

## Grading Breakdown

### Phase 1 (30%)

- Conceptual Design (ER Diagram)
- Logical DB Design (Relational Database Schema)

### Phase 2 (60%)

| Component | Weight |
|---|---|
| Documentation (assumptions, details) | 10% |
| SQL queries in the Client Application | 20% |
| Error handling | 10% |
| Physical DB Design (indexes, performance tuning) | 10% |
| Clean GUI or interface | 10% |

### Demo (10%)

### Extra Credit (up to 10%)

Good GUI design and interface, dataset or schema changes/extensions, etc.
