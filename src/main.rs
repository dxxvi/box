use books_rust::init_logger;
use log::info;

/*
create table TABLE_A (
    ID           integer default nextval('SEQ_A') primary key,
    NAME         varchar(255) not null,
    DOB          timestamp(3),
    IS_PROCESSED char(1),
    constraint IS_PROCESSED_CHECK check (IS_PROCESSED is null or IS_PROCESSED in ('Y', 'N'))
);

 */
fn main() {
    init_logger();

    info!("Hello, world 1!");
}
