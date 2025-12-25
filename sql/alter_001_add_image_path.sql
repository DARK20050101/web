-- Migration script: Add image_path column to messages table
-- This script is for adding image support to existing installations
-- For new installations, use schema.sql which already includes this column

USE messageboard;

-- Add image_path column if it doesn't exist
ALTER TABLE messages 
ADD COLUMN IF NOT EXISTS image_path VARCHAR(255) NULL AFTER user_id;

-- Add index for better query performance when filtering by image presence
CREATE INDEX IF NOT EXISTS idx_image_path ON messages(image_path);
