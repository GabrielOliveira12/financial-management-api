ALTER TABLE tb_transaction
    ADD COLUMN usd_rate        NUMERIC(19, 6),
    ADD COLUMN usd_rate_date   DATE,
    ADD COLUMN amount_in_usd   NUMERIC(19, 2),
    ADD COLUMN eur_rate        NUMERIC(19, 6),
    ADD COLUMN eur_rate_date   DATE,
    ADD COLUMN amount_in_eur   NUMERIC(19, 2);
