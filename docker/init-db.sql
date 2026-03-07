-- Create uuid function extension and devices table with schema provided by user
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    device_uuid UUID UNIQUE NOT NULL,
    device_name VARCHAR(100),
    device_type VARCHAR(50),
    firmware_version VARCHAR(50),
    location VARCHAR(100),
    status VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO devices (device_uuid, device_name, device_type, firmware_version, location, status)
VALUES
(gen_random_uuid(),'Temperature Sensor A','sensor','1.0','Berlin','ACTIVE'),
(gen_random_uuid(),'Humidity Sensor B','sensor','1.1','Munich','ACTIVE'),
(gen_random_uuid(),'Smart Lock C','security','2.0','Hamburg','ACTIVE')
ON CONFLICT DO NOTHING;