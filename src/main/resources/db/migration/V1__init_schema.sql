CREATE TABLE users (
id CHAR(36) PRIMARY KEY,
name VARCHAR(255) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
role VARCHAR(50),
created_at TIMESTAMP NULL,
updated_at TIMESTAMP NULL
);

CREATE TABLE categories (
id CHAR(36) PRIMARY KEY,
name VARCHAR(255) NOT NULL,
type VARCHAR(50) NOT NULL,
color VARCHAR(20),
icon VARCHAR(50),
is_default BOOLEAN DEFAULT FALSE,
user_id CHAR(36),

CONSTRAINT fk_category_user
    FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE SET NULL
);

CREATE TABLE accounts (
id CHAR(36) PRIMARY KEY,
name VARCHAR(255) NOT NULL,
type VARCHAR(50),
balance DECIMAL(15,2) NOT NULL,
initial_balance DECIMAL(15,2) NOT NULL,
color VARCHAR(20),
icon VARCHAR(50),
is_active BOOLEAN DEFAULT TRUE,
user_id CHAR(36) NOT NULL,
created_at TIMESTAMP NULL,

CONSTRAINT fk_account_user
  FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
);

CREATE TABLE transactions (
id CHAR(36) PRIMARY KEY,
description VARCHAR(255) NOT NULL,
amount DECIMAL(15,2) NOT NULL,
type VARCHAR(50) NOT NULL,
transaction_date DATE NOT NULL,

account_id CHAR(36) NOT NULL,
destination_account_id CHAR(36),
category_id CHAR(36),
user_id CHAR(36) NOT NULL,

notes TEXT,
recurrence VARCHAR(50),
is_paid BOOLEAN DEFAULT TRUE,
created_at TIMESTAMP NULL,

CONSTRAINT fk_transaction_account
  FOREIGN KEY (account_id) REFERENCES accounts(id)
      ON DELETE CASCADE,

CONSTRAINT fk_transaction_destination_account
  FOREIGN KEY (destination_account_id) REFERENCES accounts(id)
      ON DELETE SET NULL,

CONSTRAINT fk_transaction_category
  FOREIGN KEY (category_id) REFERENCES categories(id)
      ON DELETE SET NULL,

CONSTRAINT fk_transaction_user
  FOREIGN KEY (user_id) REFERENCES users(id)
      ON DELETE CASCADE
);

CREATE TABLE budgets (
 id CHAR(36) PRIMARY KEY,
 user_id CHAR(36) NOT NULL,
 category_id CHAR(36) NOT NULL,
 limit_amount DECIMAL(15,2) NOT NULL,
 month INT NOT NULL,
 year INT NOT NULL,

 CONSTRAINT fk_budget_user
     FOREIGN KEY (user_id) REFERENCES users(id)
         ON DELETE CASCADE,

 CONSTRAINT fk_budget_category
     FOREIGN KEY (category_id) REFERENCES categories(id)
         ON DELETE CASCADE
);

CREATE TABLE goals (
id CHAR(36) PRIMARY KEY,
title VARCHAR(255) NOT NULL,
description TEXT,
target_amount DECIMAL(15,2) NOT NULL,
current_amount DECIMAL(15,2) DEFAULT 0,
target_date DATE,
status VARCHAR(50),
color VARCHAR(20),
icon VARCHAR(50),
user_id CHAR(36) NOT NULL,
created_at TIMESTAMP NULL,

CONSTRAINT fk_goal_user
   FOREIGN KEY (user_id) REFERENCES users(id)
       ON DELETE CASCADE
);