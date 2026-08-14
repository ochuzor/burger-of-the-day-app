-- 1. Add it as nullable because existing rows have no value yet.
ALTER TABLE burger_of_the_day
    ADD COLUMN published_at TIMESTAMP WITH TIME ZONE;

-- 2. Give every existing row a publication timestamp.
UPDATE burger_of_the_day
    SET published_at = created_at;

-- 3. It is now safe to require the value.
ALTER TABLE burger_of_the_day
    ALTER COLUMN published_at SET NOT NULL;

-- 4. drop now unnecessary columns
ALTER TABLE burger_of_the_day 
    DROP COLUMN created_at,
    DROP COLUMN publish_date;
