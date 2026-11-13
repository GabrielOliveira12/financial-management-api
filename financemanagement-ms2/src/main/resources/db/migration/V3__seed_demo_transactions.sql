-- Usuário 1 (Ana Silva)
INSERT INTO tb_transaction (user_id, destination_user_id, type, amount, occurred_at, description, category, status, rejection_reason, created_at) VALUES
    (1, NULL, 'DEPOSIT',    1500.00, '2026-11-01 09:00:00', 'Salário',              NULL,          'APPROVED', NULL,                         '2026-11-01 09:00:00'),
    (1, NULL, 'PURCHASE',   120.50,  '2026-11-03 12:30:00', 'Supermercado',         'FOOD',        'APPROVED', NULL,                         '2026-11-03 12:30:00'),
    (1, NULL, 'PURCHASE',   89.90,   '2026-11-05 19:00:00', 'Farmácia',             'HEALTH',      'APPROVED', NULL,                         '2026-11-05 19:00:00'),
    (1, NULL, 'WITHDRAWAL', 200.00,  '2026-11-07 15:00:00', 'Saque caixa eletrônico', NULL,        'APPROVED', NULL,                         '2026-11-07 15:00:00'),
    (1, 2,    'TRANSFER',   3000.00, '2026-11-09 10:00:00', 'Transferência acima do limite', NULL, 'REJECTED', 'TRANSACTION_LIMIT_EXCEEDED', '2026-11-09 10:00:00');

-- Usuário 2 (Bruno Costa)
INSERT INTO tb_transaction (user_id, destination_user_id, type, amount, occurred_at, description, category, status, rejection_reason, created_at) VALUES
    (2, NULL, 'DEPOSIT',    2500.00, '2026-11-01 08:30:00', 'Salário',              NULL,          'APPROVED', NULL,                      '2026-11-01 08:30:00'),
    (2, NULL, 'PURCHASE',   65.00,   '2026-11-02 13:00:00', 'Restaurante',          'FOOD',        'APPROVED', NULL,                      '2026-11-02 13:00:00'),
    (2, NULL, 'PURCHASE',   45.00,   '2026-11-04 08:00:00', 'Uber',                 'TRANSPORT',   'APPROVED', NULL,                      '2026-11-04 08:00:00'),
    (2, 1,    'TRANSFER',   300.00,  '2026-11-06 17:00:00', 'Pagamento aluguel',    NULL,          'APPROVED', NULL,                      '2026-11-06 17:00:00'),
    (2, NULL, 'WITHDRAWAL', 5000.00, '2026-11-08 11:00:00', 'Saque acima do saldo', NULL,          'REJECTED', 'INSUFFICIENT_BALANCE',    '2026-11-08 11:00:00');

-- Usuário 3 (Carla Souza)
INSERT INTO tb_transaction (user_id, destination_user_id, type, amount, occurred_at, description, category, status, rejection_reason, created_at) VALUES
    (3, NULL, 'DEPOSIT',    1800.00, '2026-11-01 10:00:00', 'Salário',              NULL,          'APPROVED', NULL, '2026-11-01 10:00:00'),
    (3, NULL, 'PURCHASE',   250.00,  '2026-11-03 20:00:00', 'Cinema e jantar',      'ENTERTAINMENT','APPROVED', NULL, '2026-11-03 20:00:00'),
    (3, NULL, 'PURCHASE',   150.00,  '2026-11-05 14:00:00', 'Material escolar',     'EDUCATION',   'APPROVED', NULL, '2026-11-05 14:00:00'),
    (3, NULL, 'PURCHASE',   400.00,  '2026-11-07 09:00:00', 'Aluguel parcial',      'HOUSING',     'APPROVED', NULL, '2026-11-07 09:00:00'),
    (3, 4,    'TRANSFER',   100.00,  '2026-11-10 16:00:00', 'Divisão de conta',     NULL,          'APPROVED', NULL, '2026-11-10 16:00:00');

-- Usuário 4 (Diego Pereira)
INSERT INTO tb_transaction (user_id, destination_user_id, type, amount, occurred_at, description, category, status, rejection_reason, created_at) VALUES
    (4, NULL, 'DEPOSIT',    3200.00, '2026-11-01 07:45:00', 'Salário',              NULL,          'APPROVED', NULL, '2026-11-01 07:45:00'),
    (4, NULL, 'PURCHASE',   95.00,   '2026-11-02 19:30:00', 'Mercado',              'FOOD',        'APPROVED', NULL, '2026-11-02 19:30:00'),
    (4, NULL, 'PURCHASE',   60.00,   '2026-11-04 07:00:00', 'Combustível',          'TRANSPORT',   'APPROVED', NULL, '2026-11-04 07:00:00'),
    (4, NULL, 'WITHDRAWAL', 150.00,  '2026-11-06 12:00:00', 'Saque',                NULL,          'APPROVED', NULL, '2026-11-06 12:00:00'),
    (4, 3,    'TRANSFER',   250.00,  '2026-11-09 18:00:00', 'Empréstimo entre amigos', NULL,       'APPROVED', NULL, '2026-11-09 18:00:00');

-- Usuário 5 (Elisa Martins)
INSERT INTO tb_transaction (user_id, destination_user_id, type, amount, occurred_at, description, category, status, rejection_reason, created_at) VALUES
    (5, NULL, 'DEPOSIT',    2100.00, '2026-11-01 11:00:00', 'Salário',              NULL,          'APPROVED', NULL, '2026-11-01 11:00:00'),
    (5, NULL, 'PURCHASE',   180.00,  '2026-11-03 16:00:00', 'Consulta médica',      'HEALTH',      'APPROVED', NULL, '2026-11-03 16:00:00'),
    (5, NULL, 'PURCHASE',   75.00,   '2026-11-05 21:00:00', 'Streaming e lazer',    'ENTERTAINMENT','APPROVED', NULL, '2026-11-05 21:00:00'),
    (5, NULL, 'PURCHASE',   500.00,  '2026-11-08 10:30:00', 'Curso online',         'EDUCATION',   'APPROVED', NULL, '2026-11-08 10:30:00'),
    (5, 1,    'TRANSFER',   120.00,  '2026-11-10 09:00:00', 'Presente de aniversário', NULL,       'APPROVED', NULL, '2026-11-10 09:00:00');
