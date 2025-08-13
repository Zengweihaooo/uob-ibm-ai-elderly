-- Emotion Companion Database Table Schema
-- Created for IBM AI Elderly Project - Emotion Companion functionality
-- Date: 2025-08-09
-- Purpose: Store emotion companion states, interactions, and user relationships

CREATE TABLE IF NOT EXISTS emotion_companion (
  id                INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id           INTEGER NOT NULL,
  name              TEXT,
  personality       TEXT,         -- friendly / professional / casual / caring
  avatar            TEXT,         -- robot / assistant / companion / helper
  emotion           TEXT,         -- happy / sad / excited / calm / anxious / helpful
  happiness         INTEGER,      -- 0-100
  energy            INTEGER,      -- 0-100
  responsiveness    INTEGER,      -- 0-100
  last_interaction  TEXT,         -- ISO-8601
  last_chat         TEXT,
  last_command      TEXT,
  interaction_count INTEGER,
  chat_count        INTEGER,
  current_location  TEXT,         -- home_screen / chat_mode / ...
  is_active         INTEGER,      -- 0/1
  activity_mode     TEXT,
  current_sound     TEXT,
  visual_expression TEXT,
  is_making_sound   INTEGER,      -- 0/1
  is_expressing_emotion INTEGER,  -- 0/1
  led_color         TEXT,
  last_attention_time TEXT,
  neglect_level     INTEGER,
  needs_attention   INTEGER,      -- 0/1
  is_lonely         INTEGER,      -- 0/1
  current_task      TEXT,
  is_learning       INTEGER,      -- 0/1
  helpfulness       INTEGER,
  created_at        TEXT,
  updated_at        TEXT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_emotion_companion_user ON emotion_companion(user_id);