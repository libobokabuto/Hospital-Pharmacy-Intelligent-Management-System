#!/usr/bin/env python3
"""
生成BCrypt密码哈希的工具脚本
用于生成密码"123456"的正确BCrypt哈希
"""

import bcrypt

# 要加密的密码
password = "123456"

# 生成BCrypt哈希（使用默认的rounds=12，但Spring Security默认使用10）
# Spring Security的BCryptPasswordEncoder默认使用strength=10
password_bytes = password.encode('utf-8')
salt = bcrypt.gensalt(rounds=10)
hashed = bcrypt.hashpw(password_bytes, salt)
hash_string = hashed.decode('utf-8')

print("=" * 60)
print("密码哈希生成工具")
print("=" * 60)
print(f"原始密码: {password}")
print(f"BCrypt哈希: {hash_string}")
print(f"哈希长度: {len(hash_string)}")
print("=" * 60)

# 验证生成的哈希
if bcrypt.checkpw(password_bytes, hashed):
    print("✓ 哈希验证成功！")
else:
    print("✗ 哈希验证失败！")

print("\nSQL更新语句:")
print("-" * 60)
print(f"UPDATE users SET password = '{hash_string}' WHERE username = 'admin';")
print(f"UPDATE users SET password = '{hash_string}' WHERE username = 'doctor';")
print(f"UPDATE users SET password = '{hash_string}' WHERE username = 'pharmacist';")
print("-" * 60)

