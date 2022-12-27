INSERT INTO routes VALUES
    ('secure.localhost', '/test', json('[{"scheme":"https","host": "localhost","port": 8001}]')),
    ('api.localhost', '/api', json('[{"host": "localhost","port": 8002}]')),
    ('balanced.localhost', '/', json('[{"host": "localhost","port": 8003}, {"host": "localhost","port": 8004}]'));