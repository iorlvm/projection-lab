CREATE TABLE order_items (
                             order_id VARCHAR(255) NOT NULL,
                             product_id BIGINT NOT NULL,
                             count INT,
                             PRIMARY KEY (order_id, product_id)
);

CREATE TABLE orders (
                        order_id VARCHAR(255) NOT NULL,
                        user_id VARCHAR(255),
                        PRIMARY KEY (order_id)
);

CREATE TABLE products (
                          product_id BIGINT AUTO_INCREMENT,
                          name VARCHAR(255),
                          price BIGINT,
                          PRIMARY KEY (product_id)
);

ALTER TABLE order_items
    ADD CONSTRAINT FKbioxgbv59vetrxe0ejfubep1w
        FOREIGN KEY (order_id)
            REFERENCES orders(order_id);

ALTER TABLE order_items
    ADD CONSTRAINT FKocimc7dtr037rh4ls4l95nlfi
        FOREIGN KEY (product_id)
            REFERENCES products(product_id);