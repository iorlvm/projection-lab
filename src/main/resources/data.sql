-- products 表
INSERT INTO products (name, price) VALUES ('Product A', 100);
INSERT INTO products (name, price) VALUES ('Product B', 200);
INSERT INTO products (name, price) VALUES ('Product C', 300);

-- orders 表
INSERT INTO orders (order_id, user_id) VALUES ('order_1', 'user_1');
INSERT INTO orders (order_id, user_id) VALUES ('order_2', 'user_2');
INSERT INTO orders (order_id, user_id) VALUES ('order_3', 'user_3');

-- order_items 表
INSERT INTO order_items (order_id, product_id, count) VALUES ('order_1', 1, 2);
INSERT INTO order_items (order_id, product_id, count) VALUES ('order_1', 2, 1);
INSERT INTO order_items (order_id, product_id, count) VALUES ('order_2', 3, 5);
INSERT INTO order_items (order_id, product_id, count) VALUES ('order_3', 2, 3);
INSERT INTO order_items (order_id, product_id, count) VALUES ('order_3', 1, 4);