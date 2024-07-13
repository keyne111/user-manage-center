-- auto-generated definition
-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                         null comment '用户昵称',
    userAccount  varchar(256)                         null comment '登录账号',
    avatarUrl    varchar(1024)                        null comment '头像',
    gender       tinyint                              null comment '性别',
    salt         varchar(32)                          null comment '密码、通信等加密盐',
    userPassword varchar(512)                         not null comment '登录密码',
    phone        varchar(128)                         null comment '电话',
    email        varchar(256)                         null comment '邮箱',
    userRole     int      default 0                   not null comment '用户权限 0-用户 1-管理员 2-vip用户',
    userStatus   int      default 0                   not null comment '用户状态 0-正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete     tinyint  default 0                   not null comment '是否删除',
    planetCode   varchar(128)                         null comment '星球编号，唯一'
)
    comment '用户表';

