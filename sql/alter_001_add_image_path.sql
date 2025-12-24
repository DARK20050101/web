-- Migration: Add image_path column to messages table
-- Run this migration after the initial schema is created

USE messageboard;

ALTER TABLE messages 
ADD COLUMN image_path VARCHAR(255) NULL COMMENT 'Filename of uploaded image (without path)' 
AFTER content;

-- Add index for efficient image queries if needed
CREATE INDEX idx_image_path ON messages(image_path);
