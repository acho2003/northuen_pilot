do $$ begin create type settlement_status as enum ('PENDING', 'PAID'); exception when duplicate_object then null; end $$;

create table if not exists carts (
    id uuid primary key default gen_random_uuid(),
    customer_id uuid not null unique references users(id) on delete cascade,
    vendor_id uuid references vendors(id),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

create table if not exists cart_items (
    id uuid primary key default gen_random_uuid(),
    cart_id uuid not null references carts(id) on delete cascade,
    product_id uuid not null references products(id),
    quantity integer not null check (quantity > 0),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint uk_cart_item_product unique (cart_id, product_id)
);

alter table payments add column if not exists settlement_id uuid;

create table if not exists driver_cash_settlements (
    id uuid primary key default gen_random_uuid(),
    driver_id uuid not null references drivers(id),
    payment_id uuid not null unique references payments(id),
    amount numeric(12,2) not null check (amount >= 0),
    status varchar(20) not null default 'PENDING' check (status in ('PENDING', 'PAID')),
    settled_at timestamp,
    notes text,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

do $$
begin
    if not exists (select 1 from pg_constraint where conname = 'fk_payments_settlement') then
        alter table payments add constraint fk_payments_settlement foreign key (settlement_id) references driver_cash_settlements(id);
    end if;
end $$;

create index if not exists idx_carts_customer on carts(customer_id);
create index if not exists idx_cart_items_cart on cart_items(cart_id);
create index if not exists idx_settlements_driver on driver_cash_settlements(driver_id);
create index if not exists idx_settlements_status on driver_cash_settlements(status);

create or replace function set_updated_at()
returns trigger as $$
begin
    new.updated_at = now();
    return new;
end;
$$ language plpgsql;

do $$ begin create trigger carts_updated_at before update on carts for each row execute function set_updated_at(); exception when duplicate_object then null; end $$;
do $$ begin create trigger cart_items_updated_at before update on cart_items for each row execute function set_updated_at(); exception when duplicate_object then null; end $$;
do $$ begin create trigger driver_cash_settlements_updated_at before update on driver_cash_settlements for each row execute function set_updated_at(); exception when duplicate_object then null; end $$;
