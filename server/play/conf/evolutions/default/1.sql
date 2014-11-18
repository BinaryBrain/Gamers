# --- Created by Slick DDL
# To stop Slick DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table `chat_messages` (`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,`room_id` INTEGER NOT NULL,`sender_id` INTEGER NOT NULL,`type` INTEGER NOT NULL,`content` VARCHAR(254) NOT NULL,`time` TIMESTAMP NOT NULL);
create table `chat_participants` (`room_id` INTEGER NOT NULL,`person_id` INTEGER NOT NULL);
create table `chat_rooms` (`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,`participants` VARCHAR(254) NOT NULL);
create table `people` (`id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,`name` VARCHAR(254) NOT NULL,`email` VARCHAR(254) NOT NULL,`password` VARCHAR(254) NOT NULL);
create table `sessions` (`token` VARCHAR(254) NOT NULL PRIMARY KEY,`user` INTEGER NOT NULL,`created` TIMESTAMP NOT NULL,`last_access` TIMESTAMP NOT NULL);
alter table `chat_messages` add constraint `message_person_fk` foreign key(`sender_id`) references `people`(`id`) on update CASCADE on delete CASCADE;
alter table `chat_messages` add constraint `message_room_fk` foreign key(`room_id`) references `chat_rooms`(`id`) on update CASCADE on delete CASCADE;
alter table `chat_participants` add constraint `chatparticipant_person_fk` foreign key(`person_id`) references `people`(`id`) on update CASCADE on delete CASCADE;
alter table `chat_participants` add constraint `chatparticipant_room_fk` foreign key(`room_id`) references `chat_rooms`(`id`) on update CASCADE on delete CASCADE;
alter table `sessions` add constraint `session_person_fk` foreign key(`user`) references `people`(`id`) on update CASCADE on delete CASCADE;

# --- !Downs

ALTER TABLE sessions DROP FOREIGN KEY session_person_fk;
ALTER TABLE chat_participants DROP FOREIGN KEY chatparticipant_person_fk;
ALTER TABLE chat_participants DROP FOREIGN KEY chatparticipant_room_fk;
ALTER TABLE chat_messages DROP FOREIGN KEY message_person_fk;
ALTER TABLE chat_messages DROP FOREIGN KEY message_room_fk;
drop table `sessions`;
drop table `people`;
drop table `chat_rooms`;
drop table `chat_participants`;
drop table `chat_messages`;

