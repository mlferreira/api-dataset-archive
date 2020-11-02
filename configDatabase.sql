
# CREATE API USER
CREATE USER IF NOT EXISTS 'api'@'%' IDENTIFIED BY 'changeme';
# GRANT ALL PRIVILEGES ON *.* TO 'api'@'%';

# CREATE DATABASE
DROP DATABASE IF EXISTS DatasetArchive;
CREATE DATABASE IF NOT EXISTS DatasetArchive;
USE DatasetArchive;

# GRANTING PRIVILEGES TO API USER
GRANT ALL PRIVILEGES ON DatasetArchive.* TO 'api'@'%';
FLUSH PRIVILEGES;


#######################################
# CREATING SPRING SECURITY ACL TABLES #
#######################################

DROP TABLE IF EXISTS acl_entry cascade;
CREATE TABLE IF NOT EXISTS acl_entry (
     id                  bigint     auto_increment primary key,
     acl_object_identity bigint     not null comment ' specify the object identity, links to ACL_OBJECT_IDENTITY table ',
     ace_order           bigint     not null comment ' the order of current entry in the ACL entries list of corresponding Object Identityy ',
     sid                 bigint     not null comment ' the target SID which the permission is granted to or denied from, links to ACL_SID table ',
     mask                bigint     not null comment ' the integer bit mask that represents the actual permission being granted or denied ',
     granting            tinyint    not null comment ' 1 means granting, value 0 means denying ',
     audit_success       tinyint(1) not null comment ' auditing purpose ',
     audit_failure       tinyint(1) not null comment ' auditing purpose '
) comment 'Spring Security ACL mandatory table: stores individual permission assigns to each SID on an Object Identity';

DROP TABLE IF EXISTS acl_object_identity cascade;
CREATE TABLE IF NOT EXISTS acl_object_identity (
    id                  bigint               auto_increment primary key,
    object_id_class     bigint               not null comment ' define the domain object class, links to ACL_CLASS table ',
    object_id_identity  bigint               not null comment ' target object primary key ',
    parent_object       bigint               null comment ' specify parent of this Object Identity within this table ',
    owner_sid           bigint               null comment ' ID of the object owner, links to ACL_SID table ',
    entries_inheriting tinyint(1) default 1  not null comment ' whether ACL Entries of this object inherits from the parent object (ACL Entries are defined in ACL_ENTRY table)',
   constraint acl_object_identity_uk
       unique (object_id_identity, object_id_class)
) comment 'Spring Security ACL mandatory table: stores information for each unique domain object';

DROP TABLE IF EXISTS acl_class cascade;
CREATE TABLE IF NOT EXISTS acl_class (
    id    bigint       auto_increment primary key,
    class varchar(200) not null
) comment 'Spring Security ACL mandatory table: stores class name of the domain object';

DROP TABLE IF EXISTS acl_sid cascade;
CREATE TABLE IF NOT EXISTS acl_sid (
    id        bigint      auto_increment primary key,
    sid       varchar(50) not null comment ' username or role name (SID stands for Security Identity)',
    principal tinyint     not null comment ' 0 or 1, to indicate that the corresponding SID is a principal (user) or an authority (role)'
) comment 'Spring Security ACL mandatory table: universally identifies any principle or authority in the system';

# ADD FOREIGN KEYS ON ACL TABLES
ALTER TABLE acl_object_identity
    add constraint acl_object_identity_acl_sid_id_fk
        foreign key (owner_sid) references acl_sid (id)
            on update cascade on delete cascade,
   add constraint acl_object_identity_acl_class_id_fk
       foreign key (object_id_class) references acl_class (id)
           on update cascade on delete cascade,
   add constraint acl_object_identity_acl_object_identity_id_fk
       foreign key (parent_object) references acl_object_identity (id)
           on update cascade on delete set null;

ALTER TABLE acl_entry
    add constraint acl_entry_acl_object_identity_id_fk
        foreign key (acl_object_identity) references acl_object_identity (id)
            on update cascade on delete cascade,
    add constraint acl_entry_acl_sid_id_fk
        foreign key (sid) references acl_sid (id)
            on update cascade on delete cascade;


#######################
# CREATING API TABLES #
#######################

# ROLE ENUM TABLE
DROP TABLE IF EXISTS role cascade;
CREATE TABLE IF NOT EXISTS role (
    name varchar(20) primary key
);

# STATUS ENUM TABLE
DROP TABLE IF EXISTS status cascade;
CREATE TABLE IF NOT EXISTS status (
    name varchar(20) primary key
);

# USER TABLE
DROP TABLE IF EXISTS users cascade;
CREATE TABLE IF NOT EXISTS users (
    id       bigint auto_increment primary key,
    username varchar(50)  not null,
    email    varchar(50)  not null,
    password varchar(200) not null,
    constraint users_email_uindex
        unique (email),
    constraint users_username_uindex
        unique (username)
);

# DATASET TABLE
DROP TABLE IF EXISTS dataset cascade;
CREATE TABLE IF NOT EXISTS dataset (
    id                    bigint         auto_increment primary key,
    alias                 varchar(50)    null comment ' unique custom identifier ',
    name                  varchar(500)   not null,
    author_id             bigint         null,
    description           text(20000)    null,
    contribution_question text(20000)    null comment ' question users have to answer to submit an entry ',
    creation_date         datetime       not null,
    update_date           datetime       null,
    status                varchar(25)    not null comment ' DRAFT ; OPEN ; CLOSED ',
    file_name             varchar(500)   null,
    download_link         varchar(5000)  null comment ' S3 download link ',
    constraint dataset_alias_uindex
        unique (alias)
);

# ENTRY TABLE
DROP TABLE IF EXISTS entry cascade;
CREATE TABLE IF NOT EXISTS entry (
    id                  bigint         auto_increment primary key,
    dataset_id          bigint         null,
    name                varchar(50)    not null,
    contributor_id      bigint         null,
    description         text(20000)    null,
    contribution_answer text(20000)    null,
    creation_date       datetime       not null,
    update_date         datetime       not null,
    status              varchar(20)    not null,
    file_name           varchar(500)   null,
    download_link       varchar(5000)  null comment ' S3 download link '
);


# TAGS TABLE
DROP TABLE IF EXISTS tags cascade;
CREATE TABLE IF NOT EXISTS tags (
    id         bigint       auto_increment primary key,
    dataset_id bigint       not null,
    entry_id   bigint       null,
    tag        varchar(100) not null,
    constraint tags_dataset_id_fk
        foreign key (dataset_id) references dataset (id)
            on update cascade on delete cascade,
    constraint tags_entry_id_fk
        foreign key (entry_id) references entry (id)
            on update cascade on delete cascade
);

# USER ROLES TABLE
DROP TABLE IF EXISTS user_roles cascade;
CREATE TABLE IF NOT EXISTS user_roles (
    user_id bigint      not null,
    role    varchar(20) not null,
    primary key (user_id, role)
);

# ADD FOREIGN KEYS
ALTER TABLE dataset
    add constraint dataset_user_id_fk
        foreign key (author_id) references users (id)
            on update set null on delete set null;

ALTER TABLE entry
    add constraint entry_contributor_id_fk
        foreign key (contributor_id) references users (id)
            on update set null on delete set null,
    add constraint entry_dataset_id_fk
    foreign key (dataset_id) references dataset (id)
               on update set null on delete set null;

ALTER TABLE user_roles
    add constraint user_roles_role_name_fk
        foreign key (role) references role (name)
            on update cascade on delete cascade,
    add constraint user_roles_user_id_fk
        foreign key (user_id) references users (id)
                   on update cascade on delete cascade;

# POPOULATE STATUS TABLE
INSERT INTO DatasetArchive.status (name) VALUES ('DRAFT');
INSERT INTO DatasetArchive.status (name) VALUES ('DELETED');
INSERT INTO DatasetArchive.status (name) VALUES ('OPEN');
INSERT INTO DatasetArchive.status (name) VALUES ('CLOSED');
INSERT INTO DatasetArchive.status (name) VALUES ('PENDING');
INSERT INTO DatasetArchive.status (name) VALUES ('ACCEPTED');
INSERT INTO DatasetArchive.status (name) VALUES ('REJECTED');


# POPULATE ROLE TABLE
INSERT INTO DatasetArchive.role (name) VALUES ('ROLE_USER');
INSERT INTO DatasetArchive.role (name) VALUES ('ROLE_ADMIN');




# TRUNCATE TABLE acl_sid;
# TRUNCATE TABLE acl_entry;
# TRUNCATE TABLE acl_object_identity;
# TRUNCATE TABLE acl_class;
# TRUNCATE TABLE dataset;
# TRUNCATE TABLE entry;
# TRUNCATE TABLE tags;
# TRUNCATE TABLE users;


