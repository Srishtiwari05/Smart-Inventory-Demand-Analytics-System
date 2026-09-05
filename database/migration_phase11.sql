-- Phase 11: Real User Accounts + RBAC
-- Run this ONCE against smart_inventory. It updates legacy ADMIN roles to OWNER.

USE smart_inventory;

-- Update the existing 'admin' user to have the 'OWNER' role
UPDATE users SET role = 'OWNER' WHERE role = 'ADMIN';
