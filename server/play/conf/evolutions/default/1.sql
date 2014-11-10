# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `chat_participants` (`room_id` INTEGER NOT NULL,`person_id` INTEGER NOT NULL);
create table `chat` (`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY);
create table `messages` (`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,`room_id` INTEGER NOT NULL,`sender_id` INTEGER NOT NULL,`type` INTEGER NOT NULL,`content` VARCHAR(254) NOT NULL,`date` DATE NOT NULL);
create table `people` (`id` INTEGER NOT NULL PRIMARY KEY,`name` VARCHAR(254) NOT NULL);
alter table `chat_participants` add constraint `person_fk` foreign key(`person_id`) references `people`(`id`) on update CASCADE on delete SET NULL;
alter table `chat_participants` add constraint `room_fk` foreign key(`room_id`) references `chat`(`id`) on update CASCADE on delete CASCADE;
alter table `messages` add constraint `person_fk` foreign key(`sender_id`) references `people`(`id`) on update CASCADE on delete SET NULL;
alter table `messages` add constraint `room_fk` foreign key(`room_id`) references `chat`(`id`) on update CASCADE on delete NO ACTION;

# --- !Downs

ALTER TABLE messages DROP FOREIGN KEY person_fk;
ALTER TABLE messages DROP FOREIGN KEY room_fk;
ALTER TABLE chat_participants DROP FOREIGN KEY person_fk;
ALTER TABLE chat_participants DROP FOREIGN KEY room_fk;
drop table `people`;
drop table `messages`;
drop table `chat`;
drop table `chat_participants`;

