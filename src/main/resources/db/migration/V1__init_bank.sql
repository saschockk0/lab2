-- === Customers ===
create table if not exists customers (
    id          bigserial primary key,
    first_name  varchar(255) not null,
    last_name   varchar(255) not null,
    tax_id      varchar(20) not null unique,
    email       varchar(255) not null unique,
    phone       varchar(50),
    address     text,
    created_at  timestamp not null default current_timestamp
);

-- === Accounts ===
create table if not exists accounts (
    id              bigserial primary key,
    account_number  varchar(20) not null unique,
    customer_id     bigint not null references customers(id) on delete restrict,
    balance         numeric(19,2) not null default 0,
    currency        varchar(3) not null default 'RUB',
    status          varchar(20) not null default 'ACTIVE',
    type            varchar(20) not null default 'DEBIT',
    credit_limit    numeric(19,2),
    opened_date     timestamp not null default current_timestamp
);

create index if not exists idx_accounts_customer on accounts(customer_id);
create index if not exists idx_accounts_status on accounts(status);

-- === Cards ===
create table if not exists cards (
    id              bigserial primary key,
    card_number     varchar(16) not null unique,
    account_id      bigint not null references accounts(id) on delete restrict,
    holder_name     varchar(255) not null,
    expiry_date     date not null,
    cvv             varchar(3) not null,
    status          varchar(20) not null default 'ACTIVE',
    created_at      timestamp not null default current_timestamp
);

create index if not exists idx_cards_account on cards(account_id);
create index if not exists idx_cards_status on cards(status);

-- === Transactions ===
create table if not exists transactions (
    id              bigserial primary key,
    from_account_id bigint not null references accounts(id) on delete restrict,
    to_account_id   bigint not null references accounts(id) on delete restrict,
    amount          numeric(19,2) not null,
    currency        varchar(3) not null,
    timestamp       timestamp not null default current_timestamp,
    status          varchar(20) not null default 'PENDING',
    description     text,
    failure_reason  text
);

create index if not exists idx_transactions_from_account on transactions(from_account_id);
create index if not exists idx_transactions_to_account on transactions(to_account_id);
create index if not exists idx_transactions_status on transactions(status);
create index if not exists idx_transactions_timestamp on transactions(timestamp);

-- === Users (для аутентификации) ===
create table if not exists users (
    id          bigserial primary key,
    username    varchar(255) not null unique,
    password    varchar(255) not null,
    role        varchar(20) not null default 'CLIENT',
    customer_id bigint references customers(id) on delete set null
);

create index if not exists idx_users_customer on users(customer_id);

-- === User Sessions (для JWT refresh токенов) ===
create table if not exists user_sessions (
    id          bigserial primary key,
    user_id     bigint not null references users(id) on delete cascade,
    refresh_jti varchar(80) not null unique,
    status      varchar(16) not null default 'ACTIVE',
    created_at  timestamp not null default current_timestamp,
    expires_at  timestamp not null,
    rotated_at  timestamp
);

create index if not exists ix_user_sessions_user on user_sessions(user_id);
create unique index if not exists ix_user_sessions_refresh_jti on user_sessions(refresh_jti);

-- === Seed data (для тестирования) ===
-- Тестовый клиент
insert into customers(first_name, last_name, tax_id, email, phone, address)
values ('Ivan', 'Petrov', '123456789012', 'ivan@example.com', '+79991234567', 'Moscow, Tverskaya st.')
on conflict (tax_id) do nothing;

-- Тестовый пользователь
insert into users(username, password, role)
values ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDN6', 'ADMIN')
on conflict (username) do nothing;
-- Пароль: admin123 (BCrypt hash)

insert into users(username, password, role)
values ('client', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pDN6', 'CLIENT')
on conflict (username) do nothing;



