# Ecommerce Application with Permit.io Authorization
This example project showcases a simple ecommerce app that implements authorization using permit.io.

# Features
- User Authentication: Users can log in to the application using their credentials.
- Product Management: Authenticated users can create new peoducts, view existing products, update their products, and delete them depending on their authorization level.
- Authorization with permit.io: The application uses permit.io to manage user roles and permissions, ensuring that access to certain actions is appropriately restricted.
- Role-Based Access Control: Defines different roles for users, such as viewer, editor, and admin, each with varying levels of access and permissions.

# Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

## Prerequisites
Before you begin, ensure you have the following installed:

- Java JDK 21
- Gradle
- Docker

## Step 1: Setting Up the Project
To set up the e-commerce application, git clone the source code using the code below.

```
git clone https://github.com/tyaga001/java-spring-fine-grained-auth.git
```

## Step 2: Get your Permit Environment API Key
Login to your Permit.io account and create a new project in your workspace and copy your API key. Set the PERMIT_API_KEY environment variable to your API key:

```
export PERMIT_API_KEY=<YOUR_API_KEY>
```

## Step 3: Start your local PDP container
The PDP (Policy Decision Point) is the component of permit.io that evaluates access control decisions. You can run a local PDP container using Docker:

```
docker run -it -p 7766:7000 --env PDP_DEBUG=True --env PDP_API_KEY=<YOUR_API_KEY> permitio/pdp-v2:latest
```

## Step 4: Run the Application
You can run the application using the following Gradle command:

```
./gradlew bootRun
```


