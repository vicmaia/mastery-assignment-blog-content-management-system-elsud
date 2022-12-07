drop database if exists Capstone;
create database Capstone;
use Capstone;

​
create table status(
statusId int primary key auto_increment,
statusName varchar(10) unique not null
);
​
create table post (
postId int primary key auto_increment,
creationTime Timestamp,
title varchar(50) not null,
descriptionField text,
postContent text not null,
statusId int not null,
publishDate date,
editTime timestamp,
expireDate date,
constraint foreign key (statusId)
references status (statusId)
);
​
create table hashTag (
hashTagId int primary key AUTO_INCREMENT,
hashTagName varchar(50) unique not null
);
​
create table postTag(
hashTagId int not null,
postId int not null,
primary key (hashTagId, postId),
constraint foreign key (postId) 
references post (postId),
constraint foreign key (hashTagId)
references hashTag (hashTagId)
);
​
create table rejectionReason (
postId int primary key ,
rejectionReason text,
constraint foreign key (postId)
references post (postId)
);
​
create table roles(
roleId int primary key not null auto_increment,
name varchar(15) not null
);
​
CREATE TABLE users (
  userId int primary key NOT NULL AUTO_INCREMENT,
  roleId int,
  email varchar(20) NOT NULL, 
  name varchar(20) NOT NULL,
  password varchar(20) NOT NULL,
  constraint foreign key (roleId)
  references roles (roleId),
  UNIQUE KEY UK_6dotkott2kjsp8vw4d0m25fb7 (email)
)ENGINE = InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
​
-- Insert Data--
​
insert into status (statusName)
 Values ('APPROVED'),('IN_WORK'),('REJECTED');
insert into users (name, email, password)
 Values ('admin', "admin@gmail.com", "1111"),('manager','manager@gmail.com', '2222');