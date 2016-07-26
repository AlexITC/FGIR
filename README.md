# FGIR
Facebook Groups Information Retriever is a project to retrieve and organize publications from Facebook groups working almost in real time.

## Requisites
- Gradle
- Postgresql

## How to build
- gradle build

## How to run
- java -jar build/libs/FGIR-1.0.jar [email] [password]

## Usage
This project provides a way to retrieve and organize publications from Facebook groups.

The project needs an email account which is used to parse Facebook emails, you can turn on email notification from Facebook groups.

Facebook sends two kind of email notifications:
- A notification that you get inside a group
- A notification about a new publication in a group.

The project is able to parse both kind of notification to get the relevant data which is stored in a database.

This assumes you have a database with the following specifications:
- A database called 'fgir_db' accesible with 'postgres' as user and password (required by com.alex.db.Connector which should edit for sure).
- An stored procedure called facebook.addGroup(group_id, group_name) which stores a new group.
- An stored procedure called facebook.addPublication(publication_id, group_id, user_id, username, description, photo_url, publication_time) which stores a new publication.

You can build another application to query the database or run plain sql queries.
