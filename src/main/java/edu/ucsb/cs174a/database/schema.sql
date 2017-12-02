CREATE TABLE Customer (
  tax_id    VARCHAR(20)     NOT NULL UNIQUE,
  cname     VARCHAR(20),
  state     VARCHAR(2),
  phone_num VARCHAR(10)     NOT NULL UNIQUE,
  email_add VARCHAR(30) NOT NULL UNIQUE,
  username  VARCHAR(20) NOT NULL UNIQUE,
  password  VARCHAR(30) NOT NULL,
  PRIMARY KEY (tax_id)
);

CREATE TABLE Market_Account (
  m_acc_id         VARCHAR(20) NOT NULL UNIQUE,
  balance          REAL        NOT NULL,
  thousand_flag INTEGER     NOT NULL,
  PRIMARY KEY (m_acc_id)
);

CREATE TABLE Stock_Account (
  s_acc_id         VARCHAR(20) NOT NULL UNIQUE,
  stock_symbol     VARCHAR(3)  NOT NULL,
  share            REAL        NOT NULL,
  PRIMARY KEY (s_acc_id)
);

CREATE TABLE Has_Account (
  tax_id   VARCHAR(20) NOT NULL,
  m_acc_id VARCHAR(20),
  s_acc_id VARCHAR(20),
  PRIMARY KEY (tax_id, s_acc_id),
  FOREIGN KEY (m_acc_id) REFERENCES Market_Account (m_acc_id)
    ON DELETE NO ACTION,
  FOREIGN KEY (s_acc_id) REFERENCES Stock_Account (s_acc_id)
    ON DELETE NO ACTION
);

CREATE TABLE Stock (
  stock_symbol  VARCHAR(3) NOT NULL UNIQUE,
  closing_price REAL,
  current_price REAL,
  PRIMARY KEY (stock_symbol)
);

CREATE TABLE Actor (
  stock_symbol VARCHAR(3)  NOT NULL UNIQUE,
  aname        VARCHAR(20) NOT NULL,
  DOB          VARCHAR(8)  NOT NULL,
  PRIMARY KEY (stock_symbol, aname)
);

CREATE TABLE Movie_Contract (
  title VARCHAR(50) NOT NULL,
  role  VARCHAR(20) NOT NULL,
  year  VARCHAR(4)  NOT NULL,
  value REAL        NOT NULL,
  PRIMARY KEY (title, year)
);

CREATE TABLE Movie (
  title VARCHAR(50) NOT NULL,
  year VARCHAR(4)  NOT NULL,
  rank_org VARCHAR(20) NOT NULL,
  rank VARCHAR(20) NOT NULL,
  review_aut1 VARCHAR(20),
  review1 VARCHAR(20),
  review_aut2 VARCHAR(20),
  review2 VARCHAR(20),
  review_aut3 VARCHAR(20),
  review3 VARCHAR(20),
  review_aut4 VARCHAR(20),
  review4 VARCHAR(20),
  review_aut5 VARCHAR(20),
  review5 VARCHAR(20),
  PRIMARY KEY (title, year)
);

CREATE TABLE Transaction (
  transaction_id  VARCHAR(20) NOT NULL UNIQUE,
  tax_id          VARCHAR(20) NOT NULL,
  date            VARCHAR(8)  NOT NULL,
  type            VARCHAR(20) NOT NULL,
  m_acc_id        VARCHAR(20),
  s_acc_id        VARCHAR(20),
  stock_symbol    VARCHAR(3),
  amount          REAL        NOT NULL,
  current_balance REAL        NOT NULL,
  PRIMARY KEY (transaction_id),
  FOREIGN KEY (tax_id) REFERENCES Customer (tax_id),
  FOREIGN KEY (m_acc_id) REFERENCES Market_Account (m_acc_id),
  FOREIGN KEY (s_acc_id) REFERENCES Stock_Account (s_acc_id),
  FOREIGN KEY (stock_symbol) REFERENCES Stock (stock_symbol)
);

# CREATE TABLE Associate (
#   stock_symbol VARCHAR(3)  NOT NULL,
#   aname        VARCHAR(20) NOT NULL,
#   PRIMARY KEY (stock_symbol),
#   FOREIGN KEY (stock_symbol, aname) REFERENCES Actor (stock_symbol, aname)
#     ON DELETE NO ACTION
# );

CREATE TABLE Has_Contract (
  stock_symbol VARCHAR(3)  NOT NULL,
  aname        VARCHAR(20) NOT NULL,
  title        VARCHAR(50) NOT NULL,
  year         VARCHAR(4)  NOT NULL,
  PRIMARY KEY (title, year),
  FOREIGN KEY (title, year) REFERENCES Movie_Contract (title, year),
  FOREIGN KEY (stock_symbol, aname) REFERENCES Actor (stock_symbol, aname)
    ON DELETE CASCADE
);

CREATE TABLE Date (
  date VARCHAR(8) NOT NULL,
  PRIMARY KEY (date)
);

CREATE TABLE Daily_Balance (
  m_acc_id VARCHAR(20) NOT NULL,
  date     VARCHAR(8)  NOT NULL,
  month    VARCHAR(8)  NOT NULL,
  balance  REAL        NOT NULL,
  PRIMARY KEY (m_acc_id, date),
  FOREIGN KEY (m_acc_id) REFERENCES Market_Account (m_acc_id)
);